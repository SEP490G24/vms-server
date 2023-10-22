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

    /**
     * The function returns a ResponseEntity containing a DistrictDto object mapped from the District object with the given
     * id.
     *
     * @param id The parameter "id" is an Integer representing the unique identifier of a district.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> findById(Integer id) {
        return ResponseEntity.ok(mapper.map(districtService.findById(id), DistrictDto.class));
    }

    /**
     * The function returns a ResponseEntity containing a list of DistrictDto objects.
     *
     * @return The method is returning a ResponseEntity object containing a list of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(districtService.findAll(), new TypeToken<List<DistrictDto>>() {}.getType()));
    }

    /**
     * The function returns a ResponseEntity containing a list of DistrictDto objects mapped from a list of districts found
     * by provinceId.
     *
     * @param provinceId The provinceId parameter is an Integer that represents the ID of a province.
     * @return The method is returning a ResponseEntity object containing a List of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAllByProvinceId(Integer provinceId) {
        return ResponseEntity.ok(mapper.map(districtService.findAllByProvinceId(provinceId), new TypeToken<List<DistrictDto>>() {}.getType()));
    }
}
