package com.camergo.notification.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.camergo.notification.documentation.NotificationDocumentationGuideService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
public class OpenApiConfig implements WebMvcConfigurer { 

    @Bean
    public OpenAPI notificationServiceOpenApi(
        NotificationDocumentationGuideService documentationGuideService
    ) {
        return new OpenAPI()
            .info(new Info()
                .title("Camergo Notification Services API")
                .version("2.0")
                .description(documentationGuideService.getGuideMarkdown())
                .contact(new Contact()
                    .name("TIZI BIENVENUS")
                    .email("tizibienvenus@gmail.com")
                    .url("https://www.linkedin.com/in/bienvenus-tizi-806637241/"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"))
                .termsOfService("Terms of service"))
            .servers(List.of(
                new Server()
                    .description("Gateway ENV")
                    .url("http://localhost:8080"),
                new Server()
                    .description("PROD ENV")
                    .url("e-services.camergo.cm"),
                new Server()
                    .description("Local ENV")
                    .url("http://localhost:8040")
            ));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
    }
}
