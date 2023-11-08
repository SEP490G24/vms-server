package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IRoomService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

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


    public RoomServiceImpl(RoomRepository roomRepository, ModelMapper mapper, AuditLogRepository auditLogRepository, SiteRepository siteRepository) {
        this.roomRepository = roomRepository;
        this.mapper = mapper;
        this.auditLogRepository = auditLogRepository;
        this.siteRepository = siteRepository;
        this.init(roomRepository);
    }

    @Override
    public Room update(Room roomInfo, UUID id) {
        var room = roomRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(room))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found room");
        var site = siteRepository.findById(roomInfo.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Site is null");
        }
        var updateRoom = roomRepository.save(room.update(roomInfo));
        auditLogRepository.save(new AuditLog(roomInfo.getSiteId().toString()
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
        if (SecurityUtils.checkSiteAuthorization(siteRepository, roomDto.getSiteId().toString())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
        }
        if (ObjectUtils.isEmpty(roomDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(roomDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var site = siteRepository.findById(roomDto.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Site is null");
        }
        var room = mapper.map(roomDto, Room.class);
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
    public Page<Room> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        List<UUID> sites = getListSite(siteId);
        return roomRepository.filter(
            pageable,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Room> filter(List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        List<UUID> sites = getListSite(siteId);
        return roomRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    private List<UUID> getListSite(List<String> siteId) {

        if (SecurityUtils.getOrgId() == null && siteId != null) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
        }
        List<UUID> sites = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) {
                siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                    sites.add(o.getId());
                });
            } else {
                siteId.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
                    }
                    sites.add(UUID.fromString(o));
                });
            }
        } else {
            sites.add(UUID.fromString(SecurityUtils.getSiteId()));
        }

        return sites;
    }

    @Override
    public List<Room> finAllBySiteId(String siteId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not permission");
        }
        return roomRepository.findAllBySiteIdAndEnableIsTrue(UUID.fromString(siteId));
    }
}
