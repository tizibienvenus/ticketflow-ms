package com.boaz.ticketflow.ticket.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.boaz.ticketflow.ticket.enums.TicketPriority;
import com.boaz.ticketflow.ticket.enums.TicketStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.experimental.SuperBuilder;


/**
 * Core domain entity representing a support ticket.
 */
@Entity
@Table(name = "tickets")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Ticket extends AuditableEntity{

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "assignee_id")
    private String assigneeId;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt ASC")
    private List<TicketComment> comments = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "ticket_documents",
        joinColumns = @JoinColumn(name = "ticket_id")
    )
    @Column(name = "document_id")
    @Builder.Default
    private Set<String> documentIds = new HashSet<>();

    // ── Domain methods ──────────────────────────────────────────────────────

    public void addComment(TicketComment comment) {
        comment.setTicket(this);
        this.comments.add(comment);
    }

    public void linkDocument(String documentId) {
        this.documentIds.add(documentId);
    }

    public void updateStatus(TicketStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: %s → %s".formatted(this.status, newStatus)
            );
        }
        this.status = newStatus;
        if (newStatus == TicketStatus.RESOLVED) {
            this.resolvedAt = OffsetDateTime.now();
        } else if (newStatus == TicketStatus.CLOSED) {
            this.closedAt = OffsetDateTime.now();
        }
    }

    @Override
    protected String getPrefix() {
        return "TIKET";
    }
}