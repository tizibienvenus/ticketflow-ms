package com.camergo.document.interfaces.rest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Hidden
@RestController
public class WebJarExplorerController {
    
    private static final String WEBJARS_PATH = "classpath:META-INF/resources/webjars/";
    
    @GetMapping("/webjars/explore")
    public Map<String, Object> exploreWebJars() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        // Récupérer TOUS les fichiers webjars
        Resource[] allResources = resolver.getResources(WEBJARS_PATH + "**/*");
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // Grouper par webjar
        Map<String, List<Map<String, String>>> webjars = new LinkedHashMap<>();
        
        for (Resource resource : allResources) {
            try {
                String fullPath = resource.getURL().toString();
                String relativePath = extractRelativePath(fullPath);
                
                if (relativePath != null && !relativePath.isEmpty()) {
                    String webjarName = extractWebjarName(relativePath);
                    String fileName = extractFileName(relativePath);
                    String version = extractVersion(relativePath);
                    
                    if (fileName != null && !fileName.isEmpty()) {
                        Map<String, String> fileInfo = new LinkedHashMap<>();
                        fileInfo.put("name", fileName);
                        fileInfo.put("relativePath", relativePath);
                        // URL sans préfixe ajouté manuellement
                        fileInfo.put("url", "/webjars/" + relativePath);
                        fileInfo.put("version", version);
                        
                        webjars.computeIfAbsent(webjarName, k -> new ArrayList<>()).add(fileInfo);
                    }
                }
            } catch (Exception e) {
                // Ignorer
            }
        }
        
        result.put("webjars", webjars);
        
        // URLs dynamiques pour Swagger UI
        Map<String, String> swaggerUiUrls = extractSwaggerUiUrls(webjars);
        result.put("swaggerUiUrls", swaggerUiUrls);
        
        // Toutes les URLs accessibles
        List<String> allUrls = extractAllUrls(webjars);
        result.put("allAccessibleUrls", allUrls);
        
        return result;
    }
    
    @GetMapping("/webjars/swagger-ui/urls")
    public Map<String, Object> getSwaggerUiUrls() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(WEBJARS_PATH + "swagger-ui/**/*");
        
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> files = new ArrayList<>();
        
        for (Resource resource : resources) {
            try {
                String fullPath = resource.getURL().toString();
                String relativePath = extractRelativePath(fullPath);
                
                if (relativePath != null) {
                    Map<String, String> fileInfo = new LinkedHashMap<>();
                    fileInfo.put("name", resource.getFilename());
                    fileInfo.put("path", relativePath);
                    fileInfo.put("url", "/webjars/" + relativePath);
                    files.add(fileInfo);
                }
            } catch (Exception e) {
                // Ignorer
            }
        }
        
        // Grouper par dossier
        Map<String, List<Map<String, String>>> groupedByFolder = files.stream()
                .collect(Collectors.groupingBy(
                    f -> f.get("path").contains("/") ? 
                         f.get("path").substring(0, f.get("path").lastIndexOf("/")) : 
                         "root",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        
        result.put("files", groupedByFolder);
        
        // URLs de base détectées automatiquement
        result.put("baseUrls", extractBaseUrls(files));
        
        return result;
    }
    
    private Map<String, String> extractSwaggerUiUrls(Map<String, List<Map<String, String>>> webjars) {
        Map<String, String> urls = new LinkedHashMap<>();
        
        List<Map<String, String>> swaggerFiles = webjars.get("swagger-ui");
        if (swaggerFiles != null) {
            for (Map<String, String> file : swaggerFiles) {
                String fileName = file.get("name");
                String url = file.get("url");
                
                if (fileName.endsWith(".html")) {
                    urls.put(fileName.replace(".html", ""), url);
                } else if (fileName.endsWith(".css")) {
                    urls.put("css_" + fileName, url);
                } else if (fileName.endsWith(".js")) {
                    urls.put("js_" + fileName, url);
                } else if (fileName.contains("favicon")) {
                    urls.put("favicon", url);
                }
            }
        }
        
        return urls;
    }
    
    private List<String> extractAllUrls(Map<String, List<Map<String, String>>> webjars) {
        List<String> urls = new ArrayList<>();
        
        for (List<Map<String, String>> files : webjars.values()) {
            for (Map<String, String> file : files) {
                urls.add(file.get("url"));
            }
        }
        
        return urls.stream().sorted().collect(Collectors.toList());
    }
    
    private Map<String, List<String>> extractBaseUrls(List<Map<String, String>> files) {
        Map<String, List<String>> baseUrls = new LinkedHashMap<>();
        
        for (Map<String, String> file : files) {
            String path = file.get("path");
            if (path.contains("/")) {
                String basePath = path.substring(0, path.lastIndexOf("/") + 1);
                String fileName = file.get("name");
                baseUrls.computeIfAbsent("/webjars/" + basePath, k -> new ArrayList<>()).add(fileName);
            }
        }
        
        return baseUrls;
    }
    
    private String extractRelativePath(String fullPath) {
        try {
            String search = "/webjars/";
            int index = fullPath.indexOf(search);
            if (index != -1) {
                return fullPath.substring(index + search.length());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
    private String extractWebjarName(String relativePath) {
        if (relativePath != null && relativePath.contains("/")) {
            return relativePath.substring(0, relativePath.indexOf("/"));
        }
        return "unknown";
    }
    
    private String extractVersion(String relativePath) {
        if (relativePath != null) {
            String[] parts = relativePath.split("/");
            if (parts.length >= 2 && parts[1].matches("\\d+\\.\\d+\\.\\d+")) {
                return parts[1];
            }
        }
        return "unknown";
    }
    
    private String extractFileName(String relativePath) {
        if (relativePath != null && relativePath.contains("/")) {
            return relativePath.substring(relativePath.lastIndexOf("/") + 1);
        }
        return relativePath;
    }
}