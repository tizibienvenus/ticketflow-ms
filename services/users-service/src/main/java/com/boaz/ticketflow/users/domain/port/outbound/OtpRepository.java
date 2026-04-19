package com.boaz.ticketflow.users.domain.port.outbound;

import java.time.LocalDateTime;
import java.util.Optional;

import com.boaz.ticketflow.users.domain.model.OtpEntity;

public interface  OtpRepository {
    OtpEntity save(OtpEntity otp);
    void delete(OtpEntity entity);
    Optional<OtpEntity> findByIdentifier(String identifier);
    void deleteByIdentifier(String identifier);
    int deleteByExpiresAtBefore(LocalDateTime now);
}
