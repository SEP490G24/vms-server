package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DeviceRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.IRoomService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImpl extends GenericServiceImpl<Room, UUID> implements IRoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper mapper;
    private static final String ROOM_TABLE_NAME = "Room";
    private final AuditLogRepository auditLogRepository;
    private final SiteRepository siteRepository;
    private final DeviceRepository deviceRepository;
    private final TicketRepository ticketRepository;


    public RoomServiceImpl(RoomRepository roomRepository, ModelMapper mapper, AuditLogRepository auditLogRepository, SiteRepository siteRepository, DeviceRepository deviceRepository, TicketRepository ticketRepository) {
        this.roomRepository = roomRepository;
        this.mapper = mapper;
        this.auditLogRepository = auditLogRepository;
        this.siteRepository = siteRepository;
        this.deviceRepository = deviceRepository;
        this.ticketRepository = ticketRepository;
        this.init(roomRepository);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Room update(Room roomInfo, UUID id) {
        var room = roomRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(room))
            throw new CustomException(ErrorApp.ROOM_NOT_FOUND);
        var site = siteRepository.findById(room.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, room.getSiteId().toString())) {
            throw new CustomException(ErrorApp.ROOM_NOT_BELONG_SITE);
        }
        if (roomInfo.getDeviceId() != null && roomInfo.getDeviceId() != room.getDeviceId()) {
            var device = deviceRepository.findById(roomInfo.getDeviceId()).orElse(null);
            if (ObjectUtils.isEmpty(device)) {
                throw new CustomException(ErrorApp.DEVICE_NOT_FOUND);
            }
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
                throw new CustomException(ErrorApp.DEVICE_NOT_BELONG_SITE);
            }
            if (!device.getSiteId().equals(room.getSiteId())) {
                throw new CustomException(ErrorApp.DEVICE_NOT_BELONG_SITE);
            }
            if (device.getDeviceType().equals(Constants.DeviceType.SCAN_CARD)) {
                throw new CustomException(ErrorApp.DEVICE_TYPE_SCAN_CARD);
            }
            if (roomRepository.existsByDeviceId(device.getId())) {
                throw new CustomException(ErrorApp.DEVICE_IS_EXIST_IN_ROOM);
            }
            room.setSecurity(true);
        }

        if (roomInfo.getDeviceId() == null) {
            room.setDeviceId(null);
            room.setSecurity(false);
        }

        if (!roomInfo.getEnable()) {
            if (ticketRepository.existsByRoomId(room.getId())) {
                throw new CustomException(ErrorApp.ROOM_CAN_NOT_DISABLE);
            }
        }
        var updateRoom = roomRepository.save(room.update(roomInfo));
        auditLogRepository.save(new AuditLog(room.getSiteId().toString()
            , site.getOrganizationId().toString()
            , room.getId().toString()
            , ROOM_TABLE_NAME
            , Constants.AuditType.UPDATE
            , room.toString()
            , updateRoom.toString()));
        return updateRoom;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Room create(IRoomController.RoomDto roomDto) {
        if (ObjectUtils.isEmpty(roomDto) || roomDto == null)
            throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);

        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, roomDto.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        } else {
            roomDto.setSiteId(UUID.fromString(SecurityUtils.getSiteId()));
        }

        var site = siteRepository.findById(roomDto.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, roomDto.getSiteId().toString())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        if (roomDto.getDeviceId() != null) {
            var device = deviceRepository.findById(roomDto.getDeviceId()).orElse(null);
            if (ObjectUtils.isEmpty(device)) {
                throw new CustomException(ErrorApp.DEVICE_NOT_FOUND);
            }
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            if (device.getDeviceType().equals(Constants.DeviceType.SCAN_CARD)) {
                throw new CustomException(ErrorApp.DEVICE_TYPE_SCAN_CARD);
            }
            if (!device.getSiteId().equals(roomDto.getSiteId())) {
                throw new CustomException(ErrorApp.DEVICE_NOT_BELONG_SITE);
            }
            if (roomRepository.existsByDeviceId(device.getId())) {
                throw new CustomException(ErrorApp.DEVICE_IS_EXIST_IN_ROOM);
            }
        }

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            if (roomRepository.existsByCodeAndSiteId(roomDto.getCode(), roomDto.getSiteId())) {
                throw new CustomException(ErrorApp.ROOM_DUPLICATE);
            }
        } else if (SecurityUtils.getUserDetails().isSiteAdmin()) {
            if (roomRepository.existsByCodeAndSiteId(roomDto.getCode(), UUID.fromString(SecurityUtils.getSiteId()))) {
                throw new CustomException(ErrorApp.ROOM_DUPLICATE);
            }
        }
        var room = mapper.map(roomDto, Room.class);
        if (room.getDeviceId() != null) {
            room.setSecurity(true);
        } else {
            room.setSecurity(false);
        }
        room.setEnable(true);
        var roomSave = roomRepository.save(room);
        auditLogRepository.save(new AuditLog(roomDto.getSiteId().toString()
            , site.getOrganizationId().toString()
            , roomSave.getId().toString()
            , ROOM_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , roomSave.toString()));
        return roomSave;
    }

    @Override
    public Page<Room> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String createBy) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return roomRepository.filter(
            pageableSort,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null, createBy);
    }

    @Override
    public List<Room> filter(List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String createBy) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        return roomRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null, createBy);
    }


    @Override
    public List<Room> finAllBySiteId(String siteId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        return roomRepository.findAllBySiteIdAndEnableIsTrue(UUID.fromString(siteId));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public void deleteRoom(UUID id) {
        var room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            throw new CustomException(ErrorApp.ROOM_ERROR_IN_PROCESS_DELETE);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, room.getSiteId().toString())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        if (ticketRepository.existsByRoomId(room.getId())) {
            throw new CustomException(ErrorApp.ROOM_CAN_NOT_DELETE);
        }
        var site = siteRepository.findById(room.getSiteId()).orElse(null);
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , room.getId().toString()
            , ROOM_TABLE_NAME
            , Constants.AuditType.DELETE
            , room.toString()
            , null));
        roomRepository.delete(room);
    }
}
