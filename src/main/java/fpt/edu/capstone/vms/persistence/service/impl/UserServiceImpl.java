package fpt.edu.capstone.vms.persistence.service.impl;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final IUserResource userResource;
    private final ModelMapper mapper;


    @Override
    public Page<User> filter(int pageNumber, List<String> usernames, List<Constants.UserRole> roles, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Constants.UserState state) {
        return userRepository.filter(
                PageRequest.of(pageNumber, Constants.PAGE_SIZE),
                usernames,
                roles,
                createdOnStart,
                createdOnEnd,
                state);
    }

    @Override
    public List<User> availableUsers() {
        return userRepository.findByState(Constants.UserState.AVAILABLE);
    }

    @Override
    public List<User> filterAvailableUsers(List<String> usernames) {
        return userRepository.findByStateAndUsernameIn(Constants.UserState.AVAILABLE, usernames);
    }

    @Override
    public User createUser(IUserResource.UserDto userDto) {
        User userEntity = null;

        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                userRepository.save(userEntity);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (null != kcUserId) {
                userResource.deleteUser(kcUserId);
            }
        }
        return userEntity;
    }

    @Override
    public User updateUser(IUserResource.UserDto userDto) throws NotFoundException {
        var userEntity = userRepository.findByUsername(userDto.getUsername()).orElse(null);
        if (userEntity == null) throw new NotFoundException();
        if (userResource.update(userDto.setOpenid(userEntity.getOpenid()))) {
            var value = mapper.map(userDto, User.class);
            userEntity = userEntity.update(value);
            userRepository.save(userEntity);
        }
        return userEntity;
    }

    @Override
    public int updateState(Constants.UserState state, String username) {
        return userRepository.updateStateByUsername(state, username);
    }

    @Override
    public void handleAuthSuccess(String username) {
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            var userEntity = userOptional.get();
            userEntity.setState(Constants.UserState.AVAILABLE);
            userEntity.setLastLoginTime(LocalDateTime.now());
            userRepository.save(userEntity);
        }
    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username);
    }

    @Override
    public void synAccountFromKeycloak() {
        List<IUserResource.UserDto> users = userResource.users();

        for (IUserResource.UserDto userDto : users) {
            if (null != userDto.getRole()) {
                User userEntity = userRepository.findFirstByUsername(userDto.getUsername());
                if (null == userEntity) {
                    userEntity = mapper.map(userDto, User.class);
                    userRepository.save(userEntity);
                    log.info("Create user {}", userDto.getUsername());
                }
            }
        }
    }
}
