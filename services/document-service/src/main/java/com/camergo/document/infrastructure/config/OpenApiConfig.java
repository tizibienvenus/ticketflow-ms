package com.camergo.document.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "TIZI BIENVENUS",
                        email = "tizibienvenus@gmail.com",
                        url = "https://www.linkedin.com/in/bienvenus-tizi-806637241/"
                ),
                description = "Documentation API pour le **service de gestion de documents** de la plateforme Camergo.\n\n"
                        + "Ce service permet :\n\n"
                        + "- Le téléversement et le téléchargement de documents (pièces d'identité, permis, attestations, etc.).\n"
                        + "- La gestion des métadonnées (type, statut, propriétaire, dates).\n"
                        + "- La validation et la signature électronique des documents.\n"
                        + "- L'intégration avec un stockage sécurisé (S3, minIO, etc.).\n"
                        + "- La génération de versions et l'historique des modifications.",
                title = "Camergo Document Services API",
                version = "1.0",
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                termsOfService = "Conditions d'utilisation"
        )
        /*servers = {
                @Server(
                        description = "Gateway ENV",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "https://e-services.camergo.cm"
                ),
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8050"
                )
        }*/
)
public class OpenApiConfig implements WebMvcConfigurer {

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
        registry.addViewController("/swagger-ui/index.html")
                .setViewName("forward:/static/swagger/index.html");
        registry.addViewController("/swagger-ui.html")
                .setViewName("forward:/static/swagger/index.html");
        registry.addViewController("/docs")
                .setViewName("forward:/static/swagger/index.html");
        registry.addViewController("/")
                .setViewName("forward:/static/swagger/index.html");
    }
}