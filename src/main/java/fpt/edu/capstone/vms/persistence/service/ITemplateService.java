package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface ITemplateService extends IGenericService<Template, UUID> {

    Template create(ITemplateController.TemplateDto templateDto);

    Page<Template> filter(Pageable pageable,
                          List<String> names,
                          List<String> siteId,
                          LocalDateTime createdOnStart,
                          LocalDateTime createdOnEnd,
                          Boolean enable,
                          Constants.TemplateType type,
                          String keyword);

    List<Template> filter(
        List<String> names,
        List<String> siteId,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        Constants.TemplateType type,
        String keyword);

    List<Template> finAllBySiteId(String siteId);

    List<Template> finAllBySiteIdAndType(String siteId, Constants.TemplateType type);

    void deleteTemplate(UUID id);
}
