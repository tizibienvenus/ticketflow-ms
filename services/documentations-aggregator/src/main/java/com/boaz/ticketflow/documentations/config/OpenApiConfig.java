
package com.boaz.ticketflow.documentations.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.boaz.ticketflow.documentations.services.DocumentationGuideService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnProperty(name = "docs.enabled", havingValue = "true", matchIfMissing = false)
public class OpenApiConfig implements WebMvcConfigurer{ 

        @Bean
        public OpenAPI documentationAggregatorOpenApi(
                DocumentationGuideService documentationGuideService
        ) {
                return new OpenAPI()
                        .info(new Info()
                                .title("CamerGo Platform API - Documentation Aggregator")
                                .version("1.0")
                                .description(documentationGuideService.getGuideMarkdown())
                                .contact(new Contact()
                                        .name("TIZI BIENVENUS")
                                        .email("tizibienvenus@gmail.com")
                                        .url("https://www.linkedin.com/in/bienvenus-tizi-806637241/"))
                                .license(new License()
                                        .name("Apache 2.0")
                                        .url("https://www.apache.org/licenses/LICENSE-2.0"))
                                .termsOfService("https://camergo.com/terms"))
                        .servers(List.of(
                                new Server()
                                        .description("Gateway Local Environment")
                                        .url("http://localhost:8080"),
                                new Server()
                                        .description("Gateway Development Environment")
                                        .url("https://dev.api.camergo.com"),
                                new Server()
                                        .description("Gateway Production Environment")
                                        .url("https://api.camergo.com")
                        ));
        }
        
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Servir les fichiers Swagger UI personnalisés depuis static/swagger
                registry.addResourceHandler("/swagger/**")
                        .addResourceLocations("classpath:/static/swagger/");
                
                // Ajouter aussi pour la racine
                registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

                registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
                // Rediriger vers votre HTML pour l'URL par défaut
                registry.addViewController("/documentation/swagger-ui/index.html")
                        .setViewName("forward:/static/swagger/index.html");
                registry.addViewController("/swagger-ui.html")
                        .setViewName("forward:/static/swagger/index.html");
                registry.addViewController("/docs")
                        .setViewName("forward:/static/swagger/index.html");
                registry.addViewController("/")
                        .setViewName("forward:/static/swagger/index.html");
                
                        // 🔥 NOUVEAU : Scalar
                registry.addViewController("/documentation/scalar")
                         .setViewName("forward:/static/scalar/index.html");
        }
}
