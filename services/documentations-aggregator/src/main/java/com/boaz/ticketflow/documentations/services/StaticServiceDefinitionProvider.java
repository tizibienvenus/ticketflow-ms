package com.boaz.ticketflow.documentations.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.boaz.ticketflow.documentations.config.ServiceDefinition;


@Component
@ConfigurationProperties(prefix = "aggregator")
@ConditionalOnProperty(
    name = "eureka.client.enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class StaticServiceDefinitionProvider implements ServiceDefinitionProvider {

    private List<ServiceDefinition> services = new ArrayList<>();

    @Override
    public List<ServiceDefinition> getAvailableServices() {
        return services;
    }


    public void setServices(List<ServiceDefinition> services) {
        this.services = services;
    }

    @Override
    public String getServiceUrl(String serviceId) {
        return services.stream()
            .filter(s -> s.name().equals(serviceId))
            .map(ServiceDefinition::url)
            .findFirst()
            .orElse(null);
    }
}
