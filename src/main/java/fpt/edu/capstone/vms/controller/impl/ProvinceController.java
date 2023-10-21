package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IProvinceController;
import fpt.edu.capstone.vms.persistence.service.IProvinceService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ProvinceController implements IProvinceController {

    private final IProvinceService provinceService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(Integer id) {
        return ResponseEntity.ok(mapper.map(provinceService.findById(id), ProvinceDto.class));
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(provinceService.findAll(), new TypeToken<List<ProvinceDto>>() {}.getType()));
    }
}
