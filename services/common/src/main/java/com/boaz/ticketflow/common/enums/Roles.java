package com.boaz.ticketflow.common.enums;

public enum Roles {
    ROLE_USER,
    ROLE_MANAGER,
    ROLE_ADMIN;

    public static Roles fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        for (Roles r : Roles.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }

        throw new IllegalArgumentException("No constant with text " + role + " found");
    }
}
