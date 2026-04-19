package com.boaz.ticketflow.users.application.dtos;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

import com.boaz.ticketflow.users.domain.model.Address;
import com.boaz.ticketflow.users.domain.model.AuthProvider;
import com.boaz.ticketflow.users.domain.model.UserRole;

import lombok.Builder;

@Data
@Builder
public class UserResponse {
    private String id;

    private String email;
    private String phone;

    private String firstName;
    private String lastName;
    private String identifier;
    private String profileImage;
    private String coverImage;
    private String fcmToken;

    private boolean emailVerified;
    private boolean phoneVerified;

    private String countryCode;
    private String city;
    private String cityId;

    private AuthProvider provider;

    private Set<UserRole> roles;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private Set<Address> addresses;
}
