-- Seed users and consumption profiles
INSERT INTO users (id, username, email, subscription_tier, created_at) VALUES
('user-001', 'alice', 'alice@example.com', 'FREE', NOW()),
('user-002', 'bob', 'bob@example.com', 'PRO', NOW()),
('user-003', 'carol', 'carol@example.com', 'ENTERPRISE', NOW());

INSERT INTO consumption_profiles (id, user_id, tokens_consumed, tokens_remaining, requests_this_minute, monthly_reset_date, subscription_tier) VALUES
('prof-001', 'user-001', 0, 50000, 0, DATEADD('MONTH', 1, CURRENT_DATE), 'FREE'),
('prof-002', 'user-002', 0, 500000, 0, DATEADD('MONTH', 1, CURRENT_DATE), 'PRO'),
('prof-003', 'user-003', 0, 999999999, 0, DATEADD('MONTH', 1, CURRENT_DATE), 'ENTERPRISE');
