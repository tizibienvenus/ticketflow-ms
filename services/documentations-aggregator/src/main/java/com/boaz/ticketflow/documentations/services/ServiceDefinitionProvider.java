package com.boaz.ticketflow.documentations.services;

import java.util.List;

import com.boaz.ticketflow.documentations.config.ServiceDefinition;

public interface ServiceDefinitionProvider {
    List<ServiceDefinition> getAvailableServices();
    String getServiceUrl(String serviceId);
}
