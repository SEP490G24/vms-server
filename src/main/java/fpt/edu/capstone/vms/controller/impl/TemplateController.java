package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ITemplateService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class TemplateController implements ITemplateController {
    private final ITemplateService templateService;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    @Override
    public ResponseEntity<?> findById(UUID id) {
        var template = templateService.findById(id);
        if (template == null) {
            return ResponseUtils.getResponseEntity(ErrorApp.TEMPLATE_NOT_FOUND);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())) {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION);
        }
        return ResponseEntity.ok(template);
    }

    @Override
    public ResponseEntity<?> delete(UUID id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseUtils.getResponseEntityStatus(true);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> create(TemplateDto templateDto) {
        try {
            var room = templateService.create(templateDto);
            return ResponseUtils.getResponseEntityStatus(room);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(UpdateTemplateDto templateDto, UUID id) {
        try {
            var template = templateService.update(mapper.map(templateDto, Template.class), id);
            return ResponseUtils.getResponseEntityStatus(template);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
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
            filter.getType(),
            filter.getKeyword());

        var templateEntityPageable = templateService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getEnable(),
            filter.getType(),
            filter.getKeyword());

        List<TemplateFilter> templateDtos = mapper.map(templateEntityPageable.getContent(), new TypeToken<List<TemplateFilter>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(templateDtos, pageable, templateEntityPageable.getTotalElements()))
            : ResponseEntity.ok(mapper.map(templateEntity, new TypeToken<List<TemplateFilter>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<?> findAllBySiteId(String siteId) {
        try {
            return ResponseUtils.getResponseEntityStatus(templateService.finAllBySiteId(siteId));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> findAllBySiteIdAndType(String siteId, Constants.TemplateType type) {
        try {
            return ResponseUtils.getResponseEntityStatus(templateService.finAllBySiteIdAndType(siteId, type));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
