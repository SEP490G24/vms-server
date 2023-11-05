package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IRoomService extends IGenericService<Room, UUID> {

    Room create(IRoomController.RoomDto roomDto);

    Page<Room> filter(Pageable pageable,
                      List<String> names,
                      UUID siteId,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      Boolean enable,
                      String keyword);

    List<Room> filter(
        List<String> names,
        UUID siteId,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        String keyword);

    List<Room> finAllBySiteId(UUID siteId);
}
