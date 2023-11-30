package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;


public interface IDeviceService extends IGenericService<Device, Integer> {

    Device create(IDeviceController.DeviceDto deviceDto);

    Page<Device> filter(Pageable pageable,
                        List<String> names,
                        List<String> siteIds,
                        Constants.DeviceType type,
                        LocalDateTime createdOnStart,
                        LocalDateTime createdOnEnd,
                        Boolean enable,
                        String keyword,
                        String createBy);

    List<Device> filter(
        List<String> names,
        List<String> siteIds,
        Constants.DeviceType type,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        String keyword,
        String createBy);

    List<Device> findAllWithNotUseInSite(List<String> siteIds);

    void deleteDevice(Integer id);
}
