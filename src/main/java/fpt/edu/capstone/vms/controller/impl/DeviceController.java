package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.service.IDeviceService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
public class DeviceController implements IDeviceController {
    private final IDeviceService deviceService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> create(DeviceDto roomDto) {
        try {
            var device = deviceService.create(roomDto);
            return ResponseEntity.ok(device);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateDeviceDto deviceDto, Integer id) {
        try {
            var device = deviceService.update(mapper.map(deviceDto, Device.class), id);
            return ResponseEntity.ok(device);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
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
        List<String> sites = new ArrayList<>();
        return ResponseEntity.ok(deviceService.findAllWithNotUseInSite(sites));
    }


}
