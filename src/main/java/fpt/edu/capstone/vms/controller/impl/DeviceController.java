package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDeviceService;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
public class DeviceController implements IDeviceController {
    private final IDeviceService deviceService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(Integer id) {
        try {
            var device = deviceService.findById(id);
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return ResponseEntity.ok(device);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> delete(Integer id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseUtils.getResponseEntityStatus(true);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> create(DeviceDto roomDto) {
        try {
            var device = deviceService.create(roomDto);
            return ResponseUtils.getResponseEntityStatus(device);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateDeviceDto deviceDto, Integer id) {
        try {
            var device = deviceService.update(mapper.map(deviceDto, Device.class), id);
            return ResponseUtils.getResponseEntityStatus(device);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> filter(DeviceFilterDTO filter, boolean isPageable, Pageable pageable) {
        var deviceEntity = deviceService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getDeviceType(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword(),
            filter.getCreateBy());

        var deviceEntityPageable = deviceService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getDeviceType(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword(),
            filter.getCreateBy());

        List<DeviceFilterResponse> deviceFilterResponses = mapper.map(deviceEntityPageable.getContent(), new TypeToken<List<DeviceFilterResponse>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(deviceFilterResponses, pageable, deviceEntityPageable.getTotalElements()))
            : ResponseEntity.ok(mapper.map(deviceEntity, new TypeToken<List<DeviceFilterResponse>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<?> findAllWithNotUseInSite(String siteId) {
        try {
            List<String> sites = new ArrayList<>();
            if (siteId != null) {
                sites.add(siteId);
            }
            return ResponseEntity.ok(deviceService.findAllWithNotUseInSite(sites));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
