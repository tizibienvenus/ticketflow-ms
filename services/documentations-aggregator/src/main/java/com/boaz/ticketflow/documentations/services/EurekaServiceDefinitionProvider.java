package com.boaz.ticketflow.documentations.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.documentations.config.DocumentationProperties;
import com.boaz.ticketflow.documentations.config.ServiceDefinition;

@Component
@ConditionalOnProperty(
    name = "eureka.client.enabled", 
    havingValue = "true"
)
public class EurekaServiceDefinitionProvider implements ServiceDefinitionProvider {

    private final DiscoveryClient discoveryClient;
    private final DocumentationProperties properties;

    @Value("${aggregator.dynamic.api-docs-path:/api/v3/api-docs}")
    private String apiDocsPath;

    public EurekaServiceDefinitionProvider(
        DiscoveryClient discoveryClient,
        DocumentationProperties properties
    ) {
        this.discoveryClient = discoveryClient;
        this.properties = properties;
    }

    @Override
    public List<ServiceDefinition> getAvailableServices() {
        return discoveryClient.getServices().stream()
            .filter(serviceId -> !properties.getExcludedServices().contains(serviceId))
            .flatMap(serviceId -> discoveryClient.getInstances(serviceId).stream())
            .map(instance -> {
                    String url = instance.getUri().toString() + apiDocsPath;
                    return new ServiceDefinition(instance.getServiceId(), url);
                })
            .collect(Collectors.toList());
    }

    @Override
    public String getServiceUrl(String serviceId) {
        var instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) return null;
        // On prend la première instance ; on pourrait aussi faire du load balancing
        var instance = instances.get(0);
        return instance.getUri().toString();
    }
}