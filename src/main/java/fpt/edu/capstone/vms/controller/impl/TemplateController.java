package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ITemplateService;
import fpt.edu.capstone.vms.util.SecurityUtils;
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
public class TemplateController implements ITemplateController {
    private final ITemplateService templateService;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    @Override
    public ResponseEntity<Template> findById(UUID id) {
        var template = templateService.findById(id);
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())) {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.FORBIDDEN, "Not permission");
        }
        return ResponseEntity.ok(template);
    }

    @Override
    public ResponseEntity<Template> delete(UUID id) {
        var template = templateService.findById(id);
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())) {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.FORBIDDEN, "Not permission");
        }
        return templateService.delete(id);
    }

    @Override
    public ResponseEntity<?> create(TemplateDto templateDto) {
        try {
            var room = templateService.create(templateDto);
            return ResponseEntity.ok(room);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateTemplateDto templateDto, UUID id) {
        try {
            var template = templateService.update(mapper.map(templateDto, Template.class), id);
            return ResponseEntity.ok(template);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(TemplateFilterDTO filter, boolean isPageable, Pageable pageable) {
        var templateEntity = templateService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword());

        var templateEntityPageable = templateService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getKeyword());

        List<TemplateFilter> templateDtos = mapper.map(templateEntityPageable.getContent(), new TypeToken<List<TemplateFilter>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(templateDtos, pageable, templateEntityPageable.getTotalElements()))
            : ResponseEntity.ok(mapper.map(templateEntity, new TypeToken<List<TemplateFilter>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteId(String siteId) {
        return ResponseEntity.ok(templateService.finAllBySiteId(siteId));
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteIdAndType(String siteId, Constants.TemplateType type) {
        return ResponseEntity.ok(templateService.finAllBySiteIdAndType(siteId, type));
    }

}
