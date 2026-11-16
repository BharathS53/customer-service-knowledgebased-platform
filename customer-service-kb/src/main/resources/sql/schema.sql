-- ==============================================================================
-- DATABASE SCHEMA: customer_service_kb
-- Customer Service Knowledge-Based Resolution Platform
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS customer_service_kb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE customer_service_kb;

-- Drop dependent tables first to allow clean idempotent reruns
DROP TABLE IF EXISTS ticket_article_mapping;
DROP TABLE IF EXISTS ticket_history;
DROP TABLE IF EXISTS ticket_comments;
DROP TABLE IF EXISTS support_tickets;
DROP TABLE IF EXISTS article_tag_mapping;
DROP TABLE IF EXISTS knowledge_articles;
DROP TABLE IF EXISTS article_tags;
DROP TABLE IF EXISTS article_categories;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- ------------------------------------------------------------------------------
-- 1. ROLES TABLE
-- Defines authorization roles: ROLE_ADMIN, ROLE_AGENT, ROLE_CUSTOMER
-- ------------------------------------------------------------------------------
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 2. USERS TABLE
-- Stores administrative staff, support agents, and registered customers
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- BCrypt encrypted hash
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 3. USER_ROLES TABLE (Many-to-Many Join Table)
-- Associates users with one or more security roles
-- ------------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 4. ARTICLE_CATEGORIES TABLE
-- Classifies articles and tickets (e.g., Billing, Network, Hardware, Account)
-- ------------------------------------------------------------------------------
CREATE TABLE article_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 5. ARTICLE_TAGS TABLE
-- Granular keyword tags for indexing and fast smart search
-- ------------------------------------------------------------------------------
CREATE TABLE article_tags (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tag_name (name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 6. KNOWLEDGE_ARTICLES TABLE
-- Structured repository containing resolution guides, problem statements, and solutions
-- ------------------------------------------------------------------------------
CREATE TABLE knowledge_articles (
    article_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    problem_statement LONGTEXT NOT NULL,
    root_cause LONGTEXT NOT NULL,
    step_by_step_solution LONGTEXT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ka_category FOREIGN KEY (category_id) REFERENCES article_categories (category_id),
    CONSTRAINT fk_ka_creator FOREIGN KEY (created_by) REFERENCES users (user_id),
    INDEX idx_ka_category (category_id),
    INDEX idx_ka_status (status),
    FULLTEXT INDEX idx_ka_fulltext (title, description, problem_statement, step_by_step_solution)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 7. ARTICLE_TAG_MAPPING TABLE (Many-to-Many Join Table)
-- Maps knowledge articles to search tags
-- ------------------------------------------------------------------------------
CREATE TABLE article_tag_mapping (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    CONSTRAINT fk_atm_article FOREIGN KEY (article_id) REFERENCES knowledge_articles (article_id) ON DELETE CASCADE,
    CONSTRAINT fk_atm_tag FOREIGN KEY (tag_id) REFERENCES article_tags (tag_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 8. SUPPORT_TICKETS TABLE
-- Customer support requests tracked from inception to resolution
-- ------------------------------------------------------------------------------
CREATE TABLE support_tickets (
    ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE, -- Unique human-readable reference e.g., TCK-2026-0001
    customer_id BIGINT NOT NULL,
    assigned_agent_id BIGINT NULL,
    category_id BIGINT NOT NULL,
    issue_title VARCHAR(255) NOT NULL,
    issue_description LONGTEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('OPEN', 'IN_PROGRESS', 'WAITING', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution_details LONGTEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_st_customer FOREIGN KEY (customer_id) REFERENCES users (user_id),
    CONSTRAINT fk_st_agent FOREIGN KEY (assigned_agent_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_st_category FOREIGN KEY (category_id) REFERENCES article_categories (category_id),
    INDEX idx_st_status (status),
    INDEX idx_st_priority (priority),
    INDEX idx_st_customer (customer_id),
    INDEX idx_st_agent (assigned_agent_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 9. TICKET_COMMENTS TABLE
-- Collaboration thread between customers and agents on a ticket
-- ------------------------------------------------------------------------------
CREATE TABLE ticket_comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE, -- Internal notes visible only to agents/admins
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tc_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets (ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_tc_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_tc_ticket (ticket_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 10. TICKET_HISTORY TABLE
-- Audit log recording state transitions, assignments, and priority changes
-- ------------------------------------------------------------------------------
CREATE TABLE ticket_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    field_changed VARCHAR(50) NOT NULL, -- e.g., 'STATUS', 'ASSIGNED_AGENT', 'PRIORITY'
    old_value VARCHAR(255) NULL,
    new_value VARCHAR(255) NULL,
    change_description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_th_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets (ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_th_user FOREIGN KEY (changed_by) REFERENCES users (user_id),
    INDEX idx_th_ticket (ticket_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 11. TICKET_ARTICLE_MAPPING TABLE (Many-to-Many Join Table)
-- Associates knowledge articles directly to tickets used to resolve them
-- ------------------------------------------------------------------------------
CREATE TABLE ticket_article_mapping (
    ticket_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    linked_by BIGINT NOT NULL,
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ticket_id, article_id),
    CONSTRAINT fk_tam_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets (ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_tam_article FOREIGN KEY (article_id) REFERENCES knowledge_articles (article_id) ON DELETE CASCADE,
    CONSTRAINT fk_tam_user FOREIGN KEY (linked_by) REFERENCES users (user_id)
) ENGINE=InnoDB;
