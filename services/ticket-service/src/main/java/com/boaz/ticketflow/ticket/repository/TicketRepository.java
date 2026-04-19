package com.boaz.ticketflow.ticket.repository;

import com.boaz.ticketflow.ticket.entity.Ticket;
import com.boaz.ticketflow.ticket.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, String>, JpaSpecificationExecutor<Ticket> {

    Page<Ticket> findAllByCreatorId(String creatorId, Pageable pageable);

    Page<Ticket> findAllByAssigneeId(String assigneeId, Pageable pageable);

    Page<Ticket> findAllByStatus(TicketStatus status, Pageable pageable);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:creatorId IS NULL OR t.creatorId = :creatorId)
              AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
            """)
    Page<Ticket> findAllWithFilters(
            @Param("status") TicketStatus status,
            @Param("creatorId") String creatorId,
            @Param("assigneeId") String assigneeId,
            Pageable pageable
    );
}