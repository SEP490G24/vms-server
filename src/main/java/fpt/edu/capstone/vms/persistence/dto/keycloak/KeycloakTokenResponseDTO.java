package fpt.edu.capstone.vms.persistence.dto.keycloak;

import fpt.edu.capstone.vms.persistence.dto.OAuth2TokenResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeycloakTokenResponseDTO implements OAuth2TokenResponseDTO {

    private Integer not_before_policy;
    private String access_token;
    private String expires_in;
    private String refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private String session_state;
    private String scope;

}
