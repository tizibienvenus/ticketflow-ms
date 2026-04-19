package com.boaz.ticketflow.documentations.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.boaz.ticketflow.documentations.config.ServiceDefinition;
import com.boaz.ticketflow.documentations.services.ServiceDefinitionProvider;

@RestController
@RequestMapping(
    value = "/documentation",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class DocumentationController implements DocumentationApiDocs {

    private final ServiceDefinitionProvider serviceDefinitionProvider;
    private final RestTemplate restTemplate;

    @Value("${aggregator.dynamic.api-docs-path:/api/v3/api-docs}")
    private String configuredApiDocsPath;

    public DocumentationController(
        ServiceDefinitionProvider serviceDefinitionProvider,
        RestTemplate restTemplate
    ) {
        this.serviceDefinitionProvider = serviceDefinitionProvider;
        this.restTemplate = restTemplate;
    }

    @Override
    @GetMapping("/services")
    public ResponseEntity<List<ServiceDefinition>> getServices() {
        return ResponseEntity.ok(serviceDefinitionProvider.getAvailableServices());
    }

    @GetMapping("/scalar/sources")
    public List<Map<String, Object>> getScalarSources() {
        return serviceDefinitionProvider.getAvailableServices()
            .stream()
            .map(service -> {
                Map<String, Object> result = new HashMap<>();
                result.put("title", service.name());
                result.put("url", "/documentation/api-docs/" + service.name());
                return result;
            })
            .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/api-docs/{serviceId}") // Ici je ne veux pas saisir le nom du service ou son id manuellement, je veux que ce soit dynamique en fonction de la liste des services disponibles 
    public ResponseEntity<Map<String, Object>> getServiceDocs(
        @PathVariable(required = false) String serviceId
    ) {

        String baseUrl = serviceDefinitionProvider.getServiceUrl(serviceId);
        
        if (baseUrl == null) {
            return ResponseEntity.notFound().build();
        }

        Set<String> candidateDocsUrls = buildCandidateDocsUrls(baseUrl);
        RestClientException lastException = null;

        for (String docsUrl : candidateDocsUrls) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> docsJson = restTemplate.getForObject(docsUrl, Map.class);
                if (docsJson == null) {
                    continue;
                }

                rewriteServers(docsJson);
                return ResponseEntity.ok(docsJson);
            } catch (HttpClientErrorException.NotFound ex) {
                lastException = ex;
            } catch (RestClientException ex) {
                lastException = ex;
                break;
            }
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Unable to fetch OpenAPI documentation for service");
        errorBody.put("serviceId", serviceId);
        errorBody.put("attemptedUrls", new ArrayList<>(candidateDocsUrls));
        if (lastException != null) {
            errorBody.put("message", lastException.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody);
    }

    @Override
    @GetMapping("/swagger-config")
    public Map<String, Object> getSwaggerUiConfig() {
        List<ServiceDefinition> services = serviceDefinitionProvider.getAvailableServices();

        List<Map<String, String>> urls = services.stream()
            .map(svc -> Map.of(
                "name", svc.name(),
                "url", "/documentation/api-docs/" + svc.name()
            ))
            .collect(Collectors.toList());

        return Map.of(
            "configUrl", "/documentation/swagger-config",
            "urls", urls,
            "validatorUrl", "none" // Désactiver la validation externe pour éviter les problèmes de CORS
        );
    }

    private void rewriteServers(
        Map<String, Object> docsJson
    ) {

        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> proxyServer = new HashMap<>();

        proxyServer.put("url","/" ); // URL relative au service de documentation
        proxyServer.put("description", "Proxy via documentation service");
        servers.add(proxyServer);

        // Proxy localhost
        Map<String, Object> localhostProxy = new HashMap<>();
        localhostProxy.put("url", "http://localhost:8080");
        localhostProxy.put("description", "Localhost proxy");
        servers.add(localhostProxy);

        Map<String, Object> remoteProxy = new HashMap<>();
        remoteProxy.put("url", "https://dev.4dealx.com");
        remoteProxy.put("description", "Remote proxy");
        servers.add(remoteProxy);

        docsJson.put("servers", servers);
    }

    private Set<String> buildCandidateDocsUrls(String baseUrl) {
        Set<String> candidateDocsUrls = new LinkedHashSet<>();
        candidateDocsUrls.add(baseUrl + normalizePath(configuredApiDocsPath));
        return candidateDocsUrls;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/api/v3/api-docs";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

}
