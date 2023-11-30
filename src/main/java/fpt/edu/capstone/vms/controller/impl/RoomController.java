package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IRoomService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController implements IRoomController {
    private final IRoomService roomService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(UUID id) {
        try {
            var room = roomService.findById(id);
            if (room == null) {
                throw new CustomException(ErrorApp.ROOM_NOT_FOUND);
            }
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, room.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return ResponseUtils.getResponseEntityStatus(room);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> delete(UUID id) {
        try {
            roomService.deleteRoom(id);
            return ResponseUtils.getResponseEntityStatus(true);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> create(RoomDto roomDto) {
        try {
            var room = roomService.create(roomDto);
            return ResponseUtils.getResponseEntityStatus(room);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateRoomDto roomDto, UUID id) {
        try {
            var room = roomService.update(mapper.map(roomDto, Room.class), id);
            return ResponseUtils.getResponseEntityStatus(room);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> filter(RoomFilterDTO filter, boolean isPageable, Pageable pageable) {
        var roomEntity = roomService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword(),
            filter.getCreateBy());

        var roomEntityPageable = roomService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword(),
            filter.getCreateBy());

        List<RoomFilterResponse> roomDtos = mapper.map(roomEntityPageable.getContent(), new TypeToken<List<RoomFilterResponse>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(roomDtos, pageable, roomEntityPageable.getTotalElements()))
            : ResponseEntity.ok(mapper.map(roomEntity, new TypeToken<List<RoomFilterResponse>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<?> findAllBySiteId(String siteId) {
        try {
            var room = roomService.finAllBySiteId(siteId);
            return ResponseUtils.getResponseEntityStatus(room);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
