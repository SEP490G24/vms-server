package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.exception.CustomException;
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
            throw new CustomException(ErrorApp.DEVICE_NOT_FOUND);
        var site = siteRepository.findById(device.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new CustomException(ErrorApp.SITE_NOT_NULL);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
            throw new CustomException(ErrorApp.DEVICE_NOT_BELONG_SITE);
        }
        if (!deviceUpdate.getMacIp().equals(device.getMacIp())) {
            if (deviceRepository.existsByMacIpAndSiteId(deviceUpdate.getMacIp(), device.getSiteId())) {
                throw new CustomException(ErrorApp.DEVICE_MAC_IP_IS_EXIST_IN_THIS_SITE);
            }
        }
        if (device.getDeviceType().equals(Constants.DeviceType.DOOR)) {
            if (deviceUpdate.getDeviceType().equals(Constants.DeviceType.SCAN_CARD)) {
                if (roomRepository.existsByDeviceId(device.getId())) {
                    throw new CustomException(ErrorApp.DEVICE_CAN_NOT_CHANGE_TYPE);
                }
            }
        }
        if (!deviceUpdate.getEnable()) {
            if (roomRepository.existsByDeviceId(device.getId())) {
                throw new CustomException(ErrorApp.DEVICE_CAN_NOT_DISABLE);
            }
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
            throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, deviceDto.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        } else {
            deviceDto.setSiteId(UUID.fromString(SecurityUtils.getSiteId()));
        }
        if (deviceDto.getMacIp() != null) {
            if (deviceRepository.existsByMacIpAndSiteId(deviceDto.getMacIp(), deviceDto.getSiteId())) {
                throw new CustomException(ErrorApp.DEVICE_MAC_IP_IS_EXIST_IN_THIS_SITE);
            }
        }
        var site = siteRepository.findById(deviceDto.getSiteId()).orElse(null);
        if (ObjectUtils.isEmpty(site)) {
            throw new CustomException(ErrorApp.SITE_NOT_NULL);
        }

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            if (deviceRepository.existsByCodeAndSiteId(deviceDto.getCode(), deviceDto.getSiteId())) {
                throw new CustomException(ErrorApp.DEVICE_DUPLICATE);
            }
        } else if (SecurityUtils.getUserDetails().isSiteAdmin()) {
            if (deviceRepository.existsByCodeAndSiteId(deviceDto.getCode(), UUID.fromString(SecurityUtils.getSiteId()))) {
                throw new CustomException(ErrorApp.DEVICE_DUPLICATE);
            }
        }

        var device = mapper.map(deviceDto, Device.class);
        device.setEnable(true);
        var deviceSave = deviceRepository.save(device);
        auditLogRepository.save(new AuditLog(site.getId().toString()
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

    @Override
    public List<Device> findAllWithNotUseInSite(List<String> siteIds) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteIds);
        return deviceRepository.findAllWithNotUseInSite(sites, Constants.DeviceType.DOOR);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public void deleteDevice(Integer id) {
        var device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            throw new CustomException(ErrorApp.DEVICE_ERROR_IN_PROCESS_DELETE);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        if (roomRepository.existsByDeviceId(device.getId())) {
            throw new CustomException(ErrorApp.DEVICE_IS_USING_CAN_NOT_DELETE);
        }
        var site = siteRepository.findById(device.getSiteId()).orElse(null);
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , device.getId().toString()
            , DEVICE_TABLE_NAME
            , Constants.AuditType.DELETE
            , device.toString()
            , null));
        deviceRepository.deleteById(id);
    }

}
