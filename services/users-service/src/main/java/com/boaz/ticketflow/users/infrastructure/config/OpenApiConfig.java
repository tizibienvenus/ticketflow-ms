
package com.boaz.ticketflow.users.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.boaz.ticketflow.users.documentation.IdentityDocumentationGuideService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
public class OpenApiConfig implements WebMvcConfigurer{ 

        @Bean
        public OpenAPI identityServiceOpenApi(
                IdentityDocumentationGuideService documentationGuideService
        ) {
                return new OpenAPI()
                        .info(new Info()
                                .title("CamerGo Identity Service API")
                                .version("1.0")
                                .description(documentationGuideService.getGuideMarkdown())
                                .contact(new Contact()
                                        .name("TIZI BIENVENUS")
                                        .email("tizibienvenus@gmail.com")
                                        .url("https://www.linkedin.com/in/bienvenus-tizi-806637241/"))
                                .license(new License()
                                        .name("Apache 2.0")
                                        .url("https://www.apache.org/licenses/LICENSE-2.0"))
                                .termsOfService("https://camergo.com/terms"));
        }
        
        @Override
        public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/swagger-ui/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                    .resourceChain(false);
            
            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/static/css/");
        }
}
