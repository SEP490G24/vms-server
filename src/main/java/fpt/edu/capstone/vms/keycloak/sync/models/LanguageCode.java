package fpt.edu.capstone.vms.keycloak.sync.models;

import lombok.Getter;

@Getter
public enum LanguageCode {
    VN("vi"),
    EN("en"),
    KO("ko");

    private final String value;

    LanguageCode(String language) {
        this.value = language;
    }

}
