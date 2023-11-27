package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DeviceRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDeviceService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class DeviceServiceImpl extends GenericServiceImpl<Device, Integer> implements IDeviceService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final ModelMapper mapper;
    private static final String DEVICE_TABLE_NAME = "Device";
    private final AuditLogRepository auditLogRepository;
    private final SiteRepository siteRepository;


    public DeviceServiceImpl(RoomRepository roomRepository, DeviceRepository deviceRepository, ModelMapper mapper, AuditLogRepository auditLogRepository, SiteRepository siteRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.mapper = mapper;
        this.auditLogRepository = auditLogRepository;
        this.siteRepository = siteRepository;
        this.init(roomRepository);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Device update(Device deviceUpdate, Integer id) {
        var device = deviceRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(device))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found device");
        var site = siteRepository.findById(device.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Site is null");
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
        }
        var updateDevice = deviceRepository.save(device.update(deviceUpdate));
        auditLogRepository.save(new AuditLog(device.getSiteId().toString()
            , site.getOrganizationId().toString()
            , device.getId().toString()
            , DEVICE_TABLE_NAME
            , Constants.AuditType.UPDATE
            , device.toString()
            , updateDevice.toString()));
        return updateDevice;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Device create(IDeviceController.DeviceDto deviceDto) {
        if (ObjectUtils.isEmpty(deviceDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(deviceDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var site = siteRepository.findById(deviceDto.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Site is null");
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, deviceDto.getSiteId().toString())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
        }
        var device = mapper.map(deviceDto, Device.class);
        device.setEnable(true);
        var deviceSave = deviceRepository.save(device);
        auditLogRepository.save(new AuditLog(deviceDto.getSiteId().toString()
            , site.getOrganizationId().toString()
            , deviceSave.getId().toString()
            , DEVICE_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , deviceSave.toString()));
        return deviceSave;
    }

    @Override
    public Page<Device> filter(Pageable pageable, List<String> names, List<String> siteId, Constants.DeviceType deviceType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String createBy) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return deviceRepository.filter(
            pageableSort,
            names,
            sites,
            deviceType,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null, createBy);
    }

    @Override
    public List<Device> filter(List<String> names, List<String> siteId, Constants.DeviceType deviceType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String createBy) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        return deviceRepository.filter(
            names,
            sites,
            deviceType,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null, createBy);
    }

}
