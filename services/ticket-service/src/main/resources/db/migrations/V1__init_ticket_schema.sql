-- ============================================================
-- V1__init_ticket_schema.sql
-- TicketFlow - Ticket Service Initial Schema
-- ============================================================

-- ENUM types
CREATE TYPE ticket_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');
CREATE TYPE ticket_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- Tickets table
CREATE TABLE tickets (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255)    NOT NULL,
    description TEXT            NOT NULL,
    status      ticket_status   NOT NULL DEFAULT 'OPEN',
    priority    ticket_priority NOT NULL DEFAULT 'MEDIUM',
    creator_id  UUID            NOT NULL,
    assignee_id UUID,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at   TIMESTAMP WITH TIME ZONE,
    version     BIGINT          NOT NULL DEFAULT 0
);

-- Comments table
CREATE TABLE ticket_comments (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id  UUID    NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id  UUID    NOT NULL,
    content    TEXT    NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Ticket-Document join table (document IDs stored here, actual docs in document-service)
CREATE TABLE ticket_documents (
    ticket_id   UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    document_id UUID NOT NULL,
    linked_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (ticket_id, document_id)
);

-- Indexes
CREATE INDEX idx_tickets_status       ON tickets(status);
CREATE INDEX idx_tickets_creator_id   ON tickets(creator_id);
CREATE INDEX idx_tickets_assignee_id  ON tickets(assignee_id);
CREATE INDEX idx_tickets_created_at   ON tickets(created_at DESC);
CREATE INDEX idx_comments_ticket_id   ON ticket_comments(ticket_id);

-- Updated_at auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_tickets_updated_at
    BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_comments_updated_at
    BEFORE UPDATE ON ticket_comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();