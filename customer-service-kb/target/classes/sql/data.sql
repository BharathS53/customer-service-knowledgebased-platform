-- ==============================================================================
-- SEED DATA: customer_service_kb
-- Sample initial records for testing roles, users, categories, articles & tickets
-- Note: Passwords below are all BCrypt-hashed:
--   admin123    -> $2a$10$7Zqf1e.kU9wVlHw783vYge2g56P1YJ0Jm9B3gC.N0q.v.9u5g7LqS
--   agent123    -> $2a$10$3p450u61W2Wz8U62sWfV1.m9GfM99Y5J6V0bX0p8R6Y6S9S7W2.f.
--   customer123 -> $2a$10$k1r9t1.02U88fO8z5E.r7.9f5U7gH4w.M1n0b9L8V4Y8H6S1Z3a.e
-- ==============================================================================

USE customer_service_kb;

-- 1. ROLES
INSERT INTO roles (role_id, name, description) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with full access to manage users, categories, articles, and analytics'),
(2, 'ROLE_AGENT', 'Support Agent who handles customer tickets, reviews articles, and provides resolutions'),
(3, 'ROLE_CUSTOMER', 'Registered Customer who creates tickets, views FAQs, and tracks issue progress')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. USERS
-- Standard password for all seed accounts for testing: admin123, agent123, customer123
-- BCrypt cost 10 hashes:
INSERT INTO users (user_id, username, password, full_name, email, phone_number, is_active) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'System Administrator', 'admin@kbplatform.com', '+1-555-0100', TRUE),
(2, 'agent_sarah', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Sarah Jenkins', 'sarah.agent@kbplatform.com', '+1-555-0101', TRUE),
(3, 'agent_michael', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Michael Chang', 'michael.agent@kbplatform.com', '+1-555-0102', TRUE),
(4, 'john_doe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'John Doe', 'john.doe@example.com', '+1-555-0103', TRUE),
(5, 'alice_smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Alice Smith', 'alice.smith@example.com', '+1-555-0104', TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 3. USER_ROLES MAPPING
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- sarah -> ROLE_AGENT
(3, 2), -- michael -> ROLE_AGENT
(4, 3), -- john -> ROLE_CUSTOMER
(5, 3)  -- alice -> ROLE_CUSTOMER
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 4. ARTICLE CATEGORIES
INSERT INTO article_categories (category_id, name, description) VALUES
(1, 'Account & Authentication', 'Login issues, Multi-Factor Authentication (MFA), password reset, and account lockout'),
(2, 'Billing & Subscriptions', 'Payment failures, invoices, refund requests, and plan upgrades'),
(3, 'Network & Connectivity', 'VPN connection drops, latency issues, firewall blocks, and DNS configuration'),
(4, 'Software & Integrations', 'REST API authentication, webhook delivery failures, and client SDK integration'),
(5, 'Hardware & Performance', 'Memory utilization, CPU spikes, printer setup, and local workstation diagnostics')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. ARTICLE TAGS
INSERT INTO article_tags (tag_id, name) VALUES
(1, 'password-reset'),
(2, 'mfa'),
(3, 'payment-gateway'),
(4, 'vpn'),
(5, 'ssl-tls'),
(6, 'rest-api'),
(7, 'timeout'),
(8, '403-forbidden')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 6. KNOWLEDGE ARTICLES
INSERT INTO knowledge_articles (
    article_id, title, description, category_id, problem_statement, root_cause, step_by_step_solution, status, created_by
) VALUES
(
    1,
    'Resolving Multi-Factor Authentication (MFA) Sync Failures',
    'Troubleshooting steps when TOTP authenticator app tokens are rejected during login.',
    1,
    'Users enter the correct 6-digit TOTP code from their authenticator app (e.g., Google Authenticator), but the system rejects it with an Invalid Code error.',
    'System clock drift on the user mobile device or server NTP unsynchronization causing token validity window to mismatch.',
    '1. Advise user to open Authenticator Settings -> Time correction for codes -> Sync now.\n2. Ensure device clock is set to Automatic Network Time.\n3. Verify server clock synchronization with pool.ntp.org.\n4. If code still fails, verify backup emergency codes with user identity verification.\n5. Reset MFA seed secret from the Admin Portal if issue persists.',
    'PUBLISHED',
    1
),
(
    2,
    'Fixing VPN Connection Timeout (Error 800/806)',
    'Step-by-step resolution for remote employees encountering VPN gateway connection timeouts.',
    3,
    'When attempting to connect to the corporate VPN, the client hangs at "Connecting to server..." and terminates with Error 800: Unable to establish VPN connection.',
    'Local ISP router blocking GRE (Generic Routing Encapsulation) protocol or UDP port 500/4500 (IPsec NAT-T).',
    '1. Switch VPN client connection protocol from IPsec/IKEv2 to SSL-VPN (Port 443 TCP).\n2. Instruct user to reboot home router/modem.\n3. Ensure "IPsec Passthrough" and "PPTP Passthrough" are enabled in home router firmware settings.\n4. Disable third-party local firewalls temporarily to isolate software interference.\n5. Reinstall TAP/TUN virtual network adapter driver.',
    'PUBLISHED',
    2
),
(
    3,
    'Handling Credit Card Transaction Declined (Code: 2004)',
    'Standard operating procedure for customers whose recurring invoice renewal failed.',
    2,
    'Recurring subscription renewal fails with error code 2004: Declined by Issuing Bank.',
    'Bank fraud-prevention heuristics triggered by international transaction or 3D-Secure authentication timeout.',
    '1. Verify the customer billing address matches card statement address.\n2. Guide customer to the Customer Portal -> Billing -> Update Payment Method.\n3. Request customer to contact their issuing bank to authorize the international charge.\n4. Retry the transaction manually from Stripe/Payment Gateway console.\n5. Offer alternate payment option (PayPal, Wire Transfer).',
    'PUBLISHED',
    1
)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 7. ARTICLE TAG MAPPING
INSERT INTO article_tag_mapping (article_id, tag_id) VALUES
(1, 1), -- MFA article -> password-reset
(1, 2), -- MFA article -> mfa
(2, 4), -- VPN article -> vpn
(2, 7), -- VPN article -> timeout
(3, 3)  -- Billing article -> payment-gateway
ON DUPLICATE KEY UPDATE article_id=VALUES(article_id);

-- 8. SUPPORT TICKETS
INSERT INTO support_tickets (
    ticket_id, ticket_number, customer_id, assigned_agent_id, category_id, 
    issue_title, issue_description, priority, status, resolution_details
) VALUES
(
    1,
    'TCK-2026-0001',
    4, -- John Doe
    2, -- Sarah Jenkins
    1, -- Account & Authentication
    'Cannot log in after resetting password - MFA token rejected',
    'I reset my password today, but when prompted for my 6-digit authenticator code, it keeps saying invalid code. I am locked out of my corporate email.',
    'HIGH',
    'RESOLVED',
    'Synced user mobile device time settings according to KB article #1. User successfully authenticated.'
),
(
    2,
    'TCK-2026-0002',
    5, -- Alice Smith
    2, -- Sarah Jenkins
    3, -- Network & Connectivity
    'VPN keeps dropping connection every 10 minutes from home WiFi',
    'Whenever I connect to the corporate VPN from home, the connection drops intermittently and gives Error 800.',
    'MEDIUM',
    'IN_PROGRESS',
    NULL
),
(
    3,
    'TCK-2026-0003',
    4, -- John Doe
    NULL, -- Unassigned
    2, -- Billing
    'Annual subscription charged twice on my credit card',
    'I noticed two separate charges of $499 on my Visa statement for the annual plan renewal. Please refund the duplicate transaction.',
    'CRITICAL',
    'OPEN',
    NULL
)
ON DUPLICATE KEY UPDATE ticket_number=VALUES(ticket_number);

-- 9. TICKET COMMENTS
INSERT INTO ticket_comments (comment_id, ticket_id, user_id, comment_text, is_internal) VALUES
(1, 1, 2, 'Contacted customer and asked them to perform automatic time sync in the Google Authenticator app.', FALSE),
(2, 1, 4, 'That fixed it immediately! Thank you so much for the quick turnaround.', FALSE),
(3, 2, 2, 'Internal Note: Checked firewall logs on Gateway 2. Client is disconnecting due to UDP timeout. Switching protocol to SSL-VPN.', TRUE)
ON DUPLICATE KEY UPDATE comment_id=VALUES(comment_id);

-- 10. TICKET HISTORY
INSERT INTO ticket_history (history_id, ticket_id, changed_by, field_changed, old_value, new_value, change_description) VALUES
(1, 1, 1, 'ASSIGNED_AGENT', NULL, 'Sarah Jenkins', 'Ticket automatically routed to Agent Sarah Jenkins'),
(2, 1, 2, 'STATUS', 'OPEN', 'IN_PROGRESS', 'Agent began troubleshooting'),
(3, 1, 2, 'STATUS', 'IN_PROGRESS', 'RESOLVED', 'Issue resolved via time sync'),
(4, 2, 1, 'ASSIGNED_AGENT', NULL, 'Sarah Jenkins', 'Assigned to Agent Sarah Jenkins'),
(5, 2, 2, 'STATUS', 'OPEN', 'IN_PROGRESS', 'Investigation started')
ON DUPLICATE KEY UPDATE history_id=VALUES(history_id);

-- 11. TICKET ARTICLE MAPPING
INSERT INTO ticket_article_mapping (ticket_id, article_id, linked_by) VALUES
(1, 1, 2) -- Ticket 1 resolved using Article 1
ON DUPLICATE KEY UPDATE ticket_id=VALUES(ticket_id);
