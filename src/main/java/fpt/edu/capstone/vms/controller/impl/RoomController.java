package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IRoomService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController implements IRoomController {
    private final IRoomService roomService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Room> findById(UUID id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @Override
    public ResponseEntity<Room> delete(UUID id) {
        return roomService.delete(id);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @Override
    public ResponseEntity<?> create(RoomDto roomDto) {
        try {
            var room = roomService.create(roomDto);
            return ResponseEntity.ok(room);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateRoomDto roomDto, UUID id) {
        try {
            var room = roomService.update(mapper.map(roomDto, Room.class), id);
            return ResponseEntity.ok(room);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
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
            filter.getKeyword());

        var roomEntityPageable = roomService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword());

        List<RoomFilterResponse> roomDtos = mapper.map(roomEntityPageable.getContent(), new TypeToken<List<RoomFilterResponse>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(roomDtos, pageable, roomDtos.size()))
            : ResponseEntity.ok(mapper.map(roomEntity, new TypeToken<List<RoomFilterResponse>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteId(String siteId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not permission");
        }
        return ResponseEntity.ok(roomService.finAllBySiteId(siteId));
    }

}
