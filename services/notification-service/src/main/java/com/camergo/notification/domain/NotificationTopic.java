package com.camergo.notification.domain;

import lombok.Getter;

public enum NotificationTopic {

    MARKETING("MARKETING", "marketing-topic"),
    DRIVER("DRIVER", "driver-topic");

    @Getter
    private final String name;
    @Getter
    private final String value;

    NotificationTopic(String name, String value) {
        this.name = name;
        this.value = value;
    }
}