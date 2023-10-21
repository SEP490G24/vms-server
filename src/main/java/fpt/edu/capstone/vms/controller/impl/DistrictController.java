package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDistrictController;
import fpt.edu.capstone.vms.persistence.service.IDistrictService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class DistrictController implements IDistrictController {

    private final IDistrictService districtService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(Integer id) {
        return ResponseEntity.ok(mapper.map(districtService.findById(id), DistrictDto.class));
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(districtService.findAll(), new TypeToken<List<DistrictDto>>() {}.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllByProvinceId(Integer provinceId) {
        return ResponseEntity.ok(mapper.map(districtService.findAllByProvinceId(provinceId), new TypeToken<List<DistrictDto>>() {}.getType()));
    }
}
