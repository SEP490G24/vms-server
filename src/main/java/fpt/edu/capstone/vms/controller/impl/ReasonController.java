package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IReasonController;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ReasonController implements IReasonController {
    private final IReasonService reasonService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<List<?>> findAllByType(Constants.Reason type) {
        return ResponseEntity.ok(reasonService.findAllByType(type));
    }

}
