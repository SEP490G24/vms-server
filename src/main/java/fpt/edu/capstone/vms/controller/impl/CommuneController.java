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

    /**
     * The function returns a ResponseEntity containing a CommuneDto object mapped from the Commune object with the given
     * id.
     *
     * @param id The parameter "id" is an Integer representing the unique identifier of a commune.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> findById(Integer id) {
        return ResponseEntity.ok(mapper.map(communeService.findById(id), CommuneDto.class));
    }

    /**
     * The function returns a ResponseEntity containing a list of CommuneDto objects.
     *
     * @return The method is returning a ResponseEntity object containing a list of CommuneDto objects.
     */
    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(communeService.findAll(), new TypeToken<List<CommuneDto>>() {}.getType()));
    }

    /**
     * The function returns a ResponseEntity containing a list of CommuneDto objects mapped from a list of Commune objects
     * found by districtId.
     *
     * @param districtId The districtId parameter is an Integer that represents the ID of a district.
     * @return The method is returning a ResponseEntity object containing a list of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAllByDistrictId(Integer districtId) {
        return ResponseEntity.ok(mapper.map(communeService.findAllByDistrictId(districtId), new TypeToken<List<CommuneDto>>() {}.getType()));
    }

}
