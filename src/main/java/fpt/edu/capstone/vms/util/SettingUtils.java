package fpt.edu.capstone.vms.util;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SettingUtils {

    private final SettingRepository settingRepository;
    private final SettingSiteMapRepository settingSiteMapRepository;
    private final Map<String, String> settingMap;

    @Autowired
    public SettingUtils(SettingRepository settingRepository, SettingSiteMapRepository settingSiteMapRepository) {
        this.settingRepository = settingRepository;
        this.settingSiteMapRepository = settingSiteMapRepository;
        this.settingMap = new HashMap<>();
    }

    @PostConstruct
    public void loadSettings() {
        List<Setting> settings = settingRepository.findAll();
        settings.forEach(o -> {
            settingMap.put(o.getCode(), o.getDefaultValue());
        });
    }

    public void loadSettingsSite(String siteId) {
        List<SettingSiteMap> settingSiteMaps = settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(UUID.fromString(siteId));
        settingSiteMaps.forEach(o -> {
            Setting setting = settingRepository.findById(o.getId().getSettingId()).orElse(null);
            if (setting != null) {
                if (o.getValue() != null) {
                    settingMap.put(setting.getCode(), o.getValue());
                } else {
                    settingMap.put(setting.getCode(), setting.getDefaultValue());
                }
            }
        });
    }

    public String getOrDefault(String code) {
        if (settingMap != null) return settingMap.get(code);
        return null;
    }

    public Boolean getBoolean(String code) {
        String val = getOrDefault(code);
        return Boolean.parseBoolean(val);
    }

    public Integer getInteger(String code, Integer defaultVal) {
        try {
            return Integer.valueOf(getOrDefault(code));
        } catch (NumberFormatException e) {

        }
        return defaultVal;
    }

    public Long getLong(String code) {
        String value = getOrDefault(code);
        if (null == value) return null;
        return Long.valueOf(value);
    }
}
