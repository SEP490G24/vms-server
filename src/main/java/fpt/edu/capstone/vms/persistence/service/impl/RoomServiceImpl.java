package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.service.IRoomService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImpl extends GenericServiceImpl<Room, UUID> implements IRoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper mapper;


    public RoomServiceImpl(RoomRepository roomRepository, ModelMapper mapper) {
        this.roomRepository = roomRepository;
        this.mapper = mapper;
        this.init(roomRepository);
    }

    @Override
    public Room update(Room roomInfo, UUID id) {
        var room = roomRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(room))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found room");
        roomRepository.save(room.update(roomInfo));
        return room;
    }

    @Override
    @Transactional
    public Room create(IRoomController.RoomDto roomDto) {
        if (ObjectUtils.isEmpty(roomDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(roomDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var room = mapper.map(roomDto, Room.class);
        room.setEnable(true);
        roomRepository.save(room);
        return room;
    }

    @Override
    public Page<Room> filter(Pageable pageable, List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        return roomRepository.filter(
            pageable,
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword);
    }

    @Override
    public List<Room> filter(List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        return roomRepository.filter(
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword);
    }

    @Override
    public List<Room> finAllBySiteId(UUID siteId) {
        return roomRepository.findAllBySiteIdAndEnableIsTrue(siteId);
    }
}
