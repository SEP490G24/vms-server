package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IReasonController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ReasonController implements IReasonController {
    private final IReasonService reasonService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Reason> findById(UUID id) {
        return ResponseEntity.ok(reasonService.findById(id));
    }

    @Override
    public ResponseEntity<Reason> delete(UUID id) {
        return reasonService.delete(id);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(reasonService.findAll());
    }

    @Override
    public ResponseEntity<?> create(ReasonDto reasonDto) {
        try {
            var reason = reasonService.create(reasonDto);
            return ResponseEntity.ok(reason);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> update(ReasonDto reasonDto, UUID id) {
        try {
            var reason = reasonService.update(mapper.map(reasonDto, Reason.class), id);
            return ResponseEntity.ok(reason);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(ReasonFilterDTO filter, boolean isPageable, Pageable pageable) {
        var reasonEntity = reasonService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword());

        var reasonEntityPageable = reasonService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword());

        List<ReasonFilterResponse> reasonDtos = mapper.map(reasonEntityPageable.getContent(), new TypeToken<List<ReasonFilterResponse>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(reasonDtos, pageable, reasonDtos.size()))
            : ResponseEntity.ok(mapper.map(reasonEntity, new TypeToken<List<ReasonFilterResponse>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteId(UUID siteId) {
        return ResponseEntity.ok(reasonService.finAllBySiteId(siteId));
    }

}
