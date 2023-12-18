package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.constants.I18n;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.dto.common.Option;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class SettingServiceImpl extends GenericServiceImpl<Setting, Long> implements ISettingService {

    private final SettingRepository settingRepository;
    private final TemplateServiceImpl templateService;

    public SettingServiceImpl(SettingRepository settingRepository, TemplateServiceImpl templateService) {
        this.settingRepository = settingRepository;
        this.templateService = templateService;
        this.init(settingRepository);
    }

    /**
     * The `update` function in Java updates a `Setting` entity with the provided data and throws exceptions if the code
     * already exists or if the entity or setting cannot be found.
     *
     * @param entity The entity parameter is an object of type Setting, which represents the updated setting information
     *               that needs to be saved.
     * @param id     The `id` parameter is the unique identifier of the `Setting` entity that needs to be updated.
     * @return The method is returning a Setting object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Setting update(Setting entity, Long id) {
        if (ObjectUtils.isEmpty(entity)) throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        if (!StringUtils.isEmpty(entity.getCode())) {
            if (settingRepository.existsByCode(entity.getCode())) {
                throw new CustomException(ErrorApp.SETTING_CODE_EXIST);
            }
        }

        var settingEntity = settingRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(settingEntity))
            throw new CustomException(ErrorApp.SETTING_NOT_FOUND);

        return settingRepository.save(settingEntity.update(entity));
    }

    @Override
    public List<Setting> findAllByGroupIdAndSiteId(Integer groupId, String siteId) {
        var settings = settingRepository.findAllByGroupId(groupId.longValue());
        settings.forEach(setting -> {
            setting.setName(I18n.getMessage(setting.getCode()));
            if (setting.getType().equals(Constants.SettingType.API)) {
                switch (setting.getCode()) {
                    case Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL ->
                        setting.setValueList(JacksonUtils.getJson(
                            templateService.finAllBySiteIdAndType(siteId, Constants.TemplateType.CONFIRM_MEETING_EMAIL)
                                .stream().map(template -> Option.builder().label(template.getName()).value(template.getId()).build())));
                    case Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL ->
                        setting.setValueList(JacksonUtils.getJson(
                            templateService.finAllBySiteIdAndType(siteId, Constants.TemplateType.CANCEL_MEETING_EMAIL)
                                .stream().map(template -> Option.builder().label(template.getName()).value(template.getId()).build())));
                    case Constants.SettingCode.TICKET_TEMPLATE_UPCOMING_EMAIL ->
                        setting.setValueList(JacksonUtils.getJson(
                            templateService.finAllBySiteIdAndType(siteId, Constants.TemplateType.UPCOMING_MEETING_EMAIL)
                                .stream().map(template -> Option.builder().label(template.getName()).value(template.getId()).build())));
                }
            }
            if (setting.getType().equals(Constants.SettingType.INPUT)) {
                switch (setting.getCode()) {
                    case Constants.SettingCode.MAIL_PASSWORD:
                        setting.setDefaultValue("");
                }
            }
        });
        return settings;
    }
}
