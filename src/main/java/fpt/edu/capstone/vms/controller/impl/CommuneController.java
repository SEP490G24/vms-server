package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICommuneController;
import fpt.edu.capstone.vms.controller.IDistrictController;
import fpt.edu.capstone.vms.persistence.service.ICommuneService;
import fpt.edu.capstone.vms.persistence.service.IDistrictService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class CommuneController implements ICommuneController {

    private final ICommuneService communeService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(Integer id) {
        return ResponseEntity.ok(mapper.map(communeService.findById(id), CommuneDto.class));
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(communeService.findAll(), new TypeToken<List<CommuneDto>>() {}.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllByDistrictId(Integer districtId) {
        return ResponseEntity.ok(mapper.map(communeService.findAllByDistrictId(districtId), new TypeToken<List<CommuneDto>>() {}.getType()));
    }

}
