package fpt.edu.capstone.vms.config.mapper;


import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // UserEntity => IUserResource.UserDto
        modelMapper.createTypeMap(User.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(User::getPhoneNumber, IUserResource.UserDto::setPhone));

        // UserRepresentation => IUserResource.UserDto
        modelMapper.createTypeMap(UserRepresentation.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(UserRepresentation::getId, IUserResource.UserDto::setOpenid));

        // IUserResource.UserDto => UserEntity
        modelMapper.createTypeMap(IUserResource.UserDto.class, User.class)
                .addMappings(mapping -> mapping.map(IUserResource.UserDto::getPhone, User::setPhoneNumber));

        // CreateUserInfo => IUserResource.UserDto
        modelMapper.createTypeMap(IUserController.CreateUserInfo.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(IUserController.CreateUserInfo::getPhoneNumber, IUserResource.UserDto::setPhone));

        // UpdateUserInfo => IUserResource.UserDto
        modelMapper.createTypeMap(IUserController.UpdateUserInfo.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(IUserController.UpdateUserInfo::getPhoneNumber, IUserResource.UserDto::setPhone));

        return modelMapper;
    }
}
