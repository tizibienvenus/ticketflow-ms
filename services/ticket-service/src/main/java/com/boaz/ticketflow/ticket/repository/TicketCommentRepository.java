package com.boaz.ticketflow.ticket.repository;

import com.boaz.ticketflow.ticket.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, String> {

    Page<TicketComment> findAllByTicketId(String ticketId, Pageable pageable);
}