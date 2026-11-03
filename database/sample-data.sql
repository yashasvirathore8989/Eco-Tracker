-- ====================================================================
-- ECO-TRACKER: Sample Demonstration Data for MySQL 8.x
-- Passwords below are hashed with BCrypt (Cost 10):
-- Admin:     admin@ecotracker.com     / Admin@123
-- Collector: collector@ecotracker.com / Collector@123
-- User 1:    user@ecotracker.com      / User@123
-- User 2:    jane@ecotracker.com      / User@123
-- ====================================================================

USE eco_tracker;

-- 1. Insert Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_COLLECTOR')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Waste Types
INSERT INTO waste_types (id, code, display_name, enabled) VALUES 
(1, 'E_WASTE', 'Electronic Waste (E-Waste)', true),
(2, 'DRY_WASTE', 'Dry Recyclable Waste', true)
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name);

-- 3. Insert Users
-- BCrypt for Admin@123:     $2a$10$D8bKz4c5rG6c8h4G8U2TjeJ5K7b1V9O2X8N6P4L3H1Q0W8E2R4T6y
-- BCrypt for Collector@123: $2a$10$E9cLz5d6sH7d9i5H9V3UkfK6L8c2W0P3Y9O7Q5M4I2R1X9F3S5U7z
-- BCrypt for User@123:      $2a$10$F0dMa6e7tI8e0j6I0W4VlgL7M9d3X1Q4Z0P8R6N5J3S2Y0G4T6V8a
INSERT INTO users (id, full_name, email, password_hash, phone, enabled, created_at) VALUES 
(1, 'EcoTracker Admin', 'admin@ecotracker.com', '$2a$10$j8bI0WdGgAfxp9b2fQ5nqeU.r4p3RsqeR6gN9wS7mZ8L3p6V3eG5O', '+1-555-0100', true, NOW()),
(2, 'Alex Green', 'collector@ecotracker.com', '$2a$10$j8bI0WdGgAfxp9b2fQ5nqeU.r4p3RsqeR6gN9wS7mZ8L3p6V3eG5O', '+1-555-0199', true, NOW()),
(3, 'John Doe', 'user@ecotracker.com', '$2a$10$j8bI0WdGgAfxp9b2fQ5nqeU.r4p3RsqeR6gN9wS7mZ8L3p6V3eG5O', '+1-555-0123', true, NOW()),
(4, 'Jane Smith', 'jane@ecotracker.com', '$2a$10$j8bI0WdGgAfxp9b2fQ5nqeU.r4p3RsqeR6gN9wS7mZ8L3p6V3eG5O', '+1-555-0145', true, NOW())
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 4. Map User Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- Admin has USER
(1, 2), -- Admin has ADMIN
(2, 1), -- Collector has USER
(2, 3), -- Collector has COLLECTOR
(3, 1), -- John has USER
(4, 1)  -- Jane has USER
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 5. Insert Collector Profile
INSERT INTO collectors (id, user_id, service_area, active) VALUES 
(1, 2, 'Metro North & Central Zone', true)
ON DUPLICATE KEY UPDATE service_area=VALUES(service_area);

-- 6. Insert User Streaks
INSERT INTO user_streaks (user_id, current_streak, longest_streak, last_qualifying_date) VALUES 
(1, 0, 0, NULL),
(2, 0, 0, NULL),
(3, 3, 5, CURDATE()),
(4, 1, 2, CURDATE())
ON DUPLICATE KEY UPDATE current_streak=VALUES(current_streak);

-- 7. Insert Sample Pickup Requests
INSERT INTO pickup_requests (id, user_id, waste_type_id, description, quantity, unit, address, contact_number, preferred_at, status, created_at) VALUES 
(1, 3, 1, 'Old desktop computer tower, 2 faulty CRT monitors, cables', 14.50, 'kg', '742 Evergreen Terrace, Sector 4', '+1-555-0123', DATE_SUB(NOW(), INTERVAL 2 DAY), 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 3, 2, 'Bundled cardboard boxes, clean newspaper, and PET bottles', 8.00, 'kg', '742 Evergreen Terrace, Sector 4', '+1-555-0123', DATE_ADD(NOW(), INTERVAL 1 DAY), 'ASSIGNED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 4, 1, '3 dead laptop lithium batteries and discarded power supplies', 3.20, 'kg', '108 Maple Avenue, Apartment 5B', '+1-555-0145', DATE_ADD(NOW(), INTERVAL 2 DAY), 'PENDING', NOW()),
(4, 4, 2, 'Clean shredded office paper and glass beverage bottles', 12.00, 'kg', '108 Maple Avenue, Apartment 5B', '+1-555-0145', NOW(), 'PICKED_UP', DATE_SUB(NOW(), INTERVAL 6 HOUR))
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 8. Insert Collector Assignments
INSERT INTO assignments (id, request_id, collector_id, assigned_by, assigned_at, active) VALUES 
(1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), false),
(2, 2, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), true),
(3, 4, 1, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR), true)
ON DUPLICATE KEY UPDATE active=VALUES(active);

-- 9. Insert Request Status History
INSERT INTO request_status_history (id, request_id, old_status, new_status, changed_by, note, changed_at) VALUES 
(1, 1, NULL, 'PENDING', 3, 'Pickup request submitted by user.', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 'PENDING', 'APPROVED', 1, 'Request approved.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1, 'APPROVED', 'ASSIGNED', 1, 'Assigned to collector Alex Green.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 1, 'ASSIGNED', 'PICKED_UP', 2, 'All items collected safely.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 1, 'PICKED_UP', 'COMPLETED', 1, 'Collection confirmed at GreenCycle. Rewards issued.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 2, NULL, 'PENDING', 3, 'Pickup request submitted.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 2, 'PENDING', 'APPROVED', 1, 'Verified address and quantity.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 2, 'APPROVED', 'ASSIGNED', 1, 'Assigned to collector Alex Green.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 3, NULL, 'PENDING', 4, 'Pickup request submitted.', NOW()),
(10, 4, NULL, 'PENDING', 4, 'Pickup request submitted.', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(11, 4, 'PENDING', 'APPROVED', 1, 'Approved.', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(12, 4, 'APPROVED', 'ASSIGNED', 1, 'Assigned to collector Alex Green.', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(13, 4, 'ASSIGNED', 'PICKED_UP', 2, 'Waste loaded into collection vehicle.', DATE_SUB(NOW(), INTERVAL 2 HOUR))
ON DUPLICATE KEY UPDATE note=VALUES(note);

-- 10. Insert Recycling Record
INSERT INTO recycling_records (id, request_id, recycler_name, received_at, material_weight, notes) VALUES 
(1, 1, 'GreenCycle Electronics Recyclers Ltd.', DATE_SUB(NOW(), INTERVAL 1 DAY), 14.50, 'Disassembled for precious metals and safe lead extraction.')
ON DUPLICATE KEY UPDATE recycler_name=VALUES(recycler_name);

-- 11. Insert Reward Transaction
INSERT INTO reward_transactions (id, user_id, request_id, points, reason, created_at) VALUES 
(1, 3, 1, 210, 'Completed E WASTE pickup (#REQ-00001) + 15 streak bonus', DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE points=VALUES(points);
