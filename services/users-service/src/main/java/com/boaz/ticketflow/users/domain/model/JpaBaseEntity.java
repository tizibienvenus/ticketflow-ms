package com.boaz.ticketflow.users.domain.model;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.boaz.ticketflow.common.domain.BaseEntity;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class JpaBaseEntity extends BaseEntity {

    @Id
    @Column(length = 100, updatable = false, nullable = false)
    @Override
    public String getId() { return super.getId(); }

    @Override
    public void setId(String id) { super.setId(id); }

    @PrePersist
    protected void onPrePersist() {
        generateId();   // génère l'ID personnalisé si nécessaire
    }
}
