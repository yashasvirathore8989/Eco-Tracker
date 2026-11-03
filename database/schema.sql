-- ====================================================================
-- ECO-TRACKER: Smart E-Waste & Dry-Waste Collection Management System
-- MySQL 8.x Database Schema DDL
-- ====================================================================

-- Create Database if not exists
CREATE DATABASE IF NOT EXISTS eco_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE eco_tracker;

-- Drop existing tables in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reward_transactions;
DROP TABLE IF EXISTS user_streaks;
DROP TABLE IF EXISTS recycling_records;
DROP TABLE IF EXISTS request_status_history;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS collectors;
DROP TABLE IF EXISTS pickup_requests;
DROP TABLE IF EXISTS waste_types;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. User Roles Join Table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Waste Types Table
CREATE TABLE waste_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_waste_types_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Collectors Table
CREATE TABLE collectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_area VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_collectors_user_id UNIQUE (user_id),
    CONSTRAINT fk_collectors_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Pickup Requests Table
CREATE TABLE pickup_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    waste_type_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'kg',
    address VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NULL,
    preferred_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pickup_requests_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_pickup_requests_waste_type FOREIGN KEY (waste_type_id) REFERENCES waste_types (id),
    INDEX idx_pickup_requests_user_created (user_id, created_at),
    INDEX idx_pickup_requests_status_preferred (status, preferred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Collector Assignments Table
CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    collector_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_assignments_request FOREIGN KEY (request_id) REFERENCES pickup_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_collector FOREIGN KEY (collector_id) REFERENCES collectors (id),
    CONSTRAINT fk_assignments_admin FOREIGN KEY (assigned_by) REFERENCES users (id),
    INDEX idx_assignments_collector_active (collector_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Request Status History Table
CREATE TABLE request_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_by BIGINT NULL,
    note VARCHAR(500) NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_status_history_request FOREIGN KEY (request_id) REFERENCES pickup_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_user FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_status_history_request (request_id, changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Recycling Records Table
CREATE TABLE recycling_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    recycler_name VARCHAR(150) NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    material_weight DECIMAL(10, 2) NOT NULL,
    notes VARCHAR(500) NULL,
    CONSTRAINT uk_recycling_records_request UNIQUE (request_id),
    CONSTRAINT fk_recycling_records_request FOREIGN KEY (request_id) REFERENCES pickup_requests (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Reward Transactions Table
CREATE TABLE reward_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    points INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reward_transactions_request UNIQUE (request_id),
    CONSTRAINT fk_reward_transactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reward_transactions_request FOREIGN KEY (request_id) REFERENCES pickup_requests (id) ON DELETE CASCADE,
    INDEX idx_reward_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. User Streaks Table
CREATE TABLE user_streaks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_qualifying_date DATE NULL,
    CONSTRAINT uk_user_streaks_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_streaks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
