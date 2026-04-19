package com.camergo.notification.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class NotificationDocumentationGuideService {

    private static final Resource GUIDE_RESOURCE =
        new ClassPathResource("docs/notification-service.md");

    private volatile String cachedGuide;

    public String getGuideMarkdown() {
        if (cachedGuide == null) {
            synchronized (this) {
                if (cachedGuide == null) {
                    cachedGuide = loadGuide();
                }
            }
        }
        return cachedGuide;
    }

    private String loadGuide() {
        try (InputStream inputStream = GUIDE_RESOURCE.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Unable to load Markdown guide for notification-service",
                exception
            );
        }
    }
}
