package fpt.edu.capstone.vms.security.converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@ConditionalOnExpression(value = "'${edu.fpt.capstone.vms.oauth2.provider}'.equals('keycloak')")
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String principalAttribute;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
    public JwtAuthenticationConverter(
            @Value("${edu.fpt.capstone.vms.oauth2.keycloak.principal-attribute}") String principalAttribute,
            JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
        this.principalAttribute = principalAttribute;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        return new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipleClaimName(jwt));
    }

    private String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (!StringUtils.isAllEmpty(principalAttribute)) {
            claimName = principalAttribute;
        }
        return jwt.getClaimAsString(claimName);
    }
}
