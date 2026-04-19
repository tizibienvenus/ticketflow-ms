package com.boaz.ticketflow.users.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.boaz.ticketflow.users.domain.model.OtpEntity;
import com.boaz.ticketflow.users.domain.port.outbound.OtpRepository;

public interface  JpaOtpRepository extends JpaRepository<OtpEntity, String>, OtpRepository {
    
    @Override
    Optional<OtpEntity> findByIdentifier(String identifier);

    @Override
    void deleteByIdentifier(String identifier);

    @Modifying
    @Query("DELETE FROM OtpEntity o WHERE o.expiresAt < :now")
    @Override
    int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
}
