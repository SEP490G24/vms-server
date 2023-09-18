package fpt.edu.capstone.vms.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenAPI30Config {
    @Bean
    public OpenAPI myOpenAPI() {
        Info info = new Info()
                .title("Visitors management system")
                .version("1.0")
                .description("This API exposes endpoints to manage.");

        return new OpenAPI().info(info);
    }
}
