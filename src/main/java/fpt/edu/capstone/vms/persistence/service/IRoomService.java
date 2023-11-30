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
                      List<String> siteIds,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      Boolean enable,
                      String keyword,
                      String createBy);

    List<Room> filter(
        List<String> names,
        List<String> siteIds,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        String keyword,
        String createBy);

    List<Room> finAllBySiteId(String siteId);

    void deleteRoom(UUID id);
}
