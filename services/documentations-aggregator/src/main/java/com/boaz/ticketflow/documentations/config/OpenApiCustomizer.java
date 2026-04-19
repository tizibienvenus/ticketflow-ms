package com.boaz.ticketflow.documentations.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boaz.ticketflow.documentations.services.ServiceDefinitionProvider;

@Configuration
public class OpenApiCustomizer {

    private final ServiceDefinitionProvider serviceDefinitionProvider;

    public OpenApiCustomizer(ServiceDefinitionProvider serviceDefinitionProvider) {
        this.serviceDefinitionProvider = serviceDefinitionProvider;
    }

    @Bean
    public OperationCustomizer customizeServiceIdParameter() {
        return (operation, handlerMethod) -> {
            // Vérifier que c'est bien la méthode getServiceDocs
            if (handlerMethod.getMethod().getName().equals("getServiceDocs")) {
                // Récupérer la liste des noms de services
                List<String> serviceNames = serviceDefinitionProvider.getAvailableServices()
                        .stream()
                        .map(ServiceDefinition::name)
                        .collect(Collectors.toList());

                // Chercher le paramètre "serviceId" dans l'opération
                operation.getParameters().stream()
                        .filter(param -> "serviceId".equals(param.getName()))
                        .findFirst()
                        .ifPresent(param -> {
                            // Rendre le paramètre optionnel (required = false)
                            param.setRequired(false);
                            // Ajouter une énumération des valeurs possibles
                            param.setSchema(new io.swagger.v3.oas.models.media.StringSchema()
                                    ._enum(serviceNames)
                                    .description("Identifiant du service (laissez vide pour obtenir la liste)"));
                        });
            }
            return operation;
        };
    }
}