# Eco-Tracker Database Design Specification

This document details the relational database design for **Eco-Tracker**, a smart E-Waste & Dry-Waste Collection Management System built with Spring Boot 3.x and MySQL 8.x / Spring Data JPA.

---

## 1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    USERS ||--o{ PICKUP_REQUESTS : creates
    WASTE_TYPES ||--o{ PICKUP_REQUESTS : categorizes
    USERS ||--o| COLLECTORS : is
    COLLECTORS ||--o{ ASSIGNMENTS : receives
    PICKUP_REQUESTS ||--o{ ASSIGNMENTS : assigned_in
    PICKUP_REQUESTS ||--o{ REQUEST_STATUS_HISTORY : tracks
    USERS ||--o{ REQUEST_STATUS_HISTORY : updates
    PICKUP_REQUESTS ||--o| RECYCLING_RECORDS : verified_by
    USERS ||--o{ REWARD_TRANSACTIONS : earns
    PICKUP_REQUESTS ||--o| REWARD_TRANSACTIONS : qualifies
    USERS ||--|| USER_STREAKS : maintains

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password_hash
        varchar phone
        boolean enabled
        datetime created_at
    }

    ROLES {
        bigint id PK
        varchar name UK
    }

    USER_ROLES {
        bigint user_id PK,FK
        bigint role_id PK,FK
    }

    WASTE_TYPES {
        bigint id PK
        varchar code UK
        varchar display_name
        boolean enabled
    }

    COLLECTORS {
        bigint id PK
        bigint user_id UK,FK
        varchar service_area
        boolean active
    }

    PICKUP_REQUESTS {
        bigint id PK
        bigint user_id FK
        bigint waste_type_id FK
        varchar description
        decimal quantity
        varchar unit
        varchar address
        varchar contact_number
        datetime preferred_at
        varchar status
        datetime created_at
    }

    ASSIGNMENTS {
        bigint id PK
        bigint request_id FK
        bigint collector_id FK
        bigint assigned_by FK
        datetime assigned_at
        boolean active
    }

    REQUEST_STATUS_HISTORY {
        bigint id PK
        bigint request_id FK
        varchar old_status
        varchar new_status
        bigint changed_by FK
        varchar note
        datetime changed_at
    }

    RECYCLING_RECORDS {
        bigint id PK
        bigint request_id UK,FK
        varchar recycler_name
        datetime received_at
        decimal material_weight
        varchar notes
    }

    REWARD_TRANSACTIONS {
        bigint id PK
        bigint user_id FK
        bigint request_id UK,FK
        int points
        varchar reason
        datetime created_at
    }

    USER_STREAKS {
        bigint user_id PK,FK
        int current_streak
        int longest_streak
        date last_qualifying_date
    }
```

---

## 2. Table Specifications & Constraints

### 2.1 `users`
Represents application accounts (citizens, collectors, admins).
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `full_name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(100), NOT NULL, UNIQUE, normalized lowercase)
- `password_hash` (VARCHAR(255), NOT NULL, BCrypt encrypted)
- `phone` (VARCHAR(20), NULL)
- `enabled` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)

### 2.2 `roles`
System roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_COLLECTOR`.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `name` (VARCHAR(50), NOT NULL, UNIQUE)

### 2.3 `user_roles`
Composite join table enforcing many-to-many relationship between users and roles.
- `user_id` (BIGINT, FK to `users.id` ON DELETE CASCADE)
- `role_id` (BIGINT, FK to `roles.id` ON DELETE CASCADE)
- PRIMARY KEY (`user_id`, `role_id`)

### 2.4 `waste_types`
Accepted waste streams (`E_WASTE`, `DRY_WASTE`).
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `code` (VARCHAR(50), NOT NULL, UNIQUE)
- `display_name` (VARCHAR(100), NOT NULL)
- `enabled` (BOOLEAN, NOT NULL DEFAULT TRUE)

### 2.5 `collectors`
Field collector profile linked to a user account.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `user_id` (BIGINT, NOT NULL, UNIQUE, FK to `users.id` ON DELETE CASCADE)
- `service_area` (VARCHAR(100), NOT NULL)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)

### 2.6 `pickup_requests`
Core transaction entity representing citizen waste collection bookings.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `user_id` (BIGINT, NOT NULL, FK to `users.id`)
- `waste_type_id` (BIGINT, NOT NULL, FK to `waste_types.id`)
- `description` (VARCHAR(500), NOT NULL)
- `quantity` (DECIMAL(10, 2), NOT NULL, positive)
- `unit` (VARCHAR(20), NOT NULL DEFAULT 'kg')
- `address` (VARCHAR(255), NOT NULL)
- `contact_number` (VARCHAR(20), NULL)
- `preferred_at` (DATETIME, NOT NULL, future service hours 08:00 - 19:00)
- `status` (VARCHAR(30), NOT NULL DEFAULT 'PENDING')
- `created_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)
- **Indexes:**
  - `idx_pickup_requests_user_created (user_id, created_at)`
  - `idx_pickup_requests_status_preferred (status, preferred_at)`

### 2.7 `assignments`
Collector dispatch records.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `request_id` (BIGINT, NOT NULL, FK to `pickup_requests.id` ON DELETE CASCADE)
- `collector_id` (BIGINT, NOT NULL, FK to `collectors.id`)
- `assigned_by` (BIGINT, NOT NULL, FK to `users.id` representing admin)
- `assigned_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- **Indexes:**
  - `idx_assignments_collector_active (collector_id, active)`

### 2.8 `request_status_history`
Full audit trail of all status transitions with actor and timestamp.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `request_id` (BIGINT, NOT NULL, FK to `pickup_requests.id` ON DELETE CASCADE)
- `old_status` (VARCHAR(30), NULL)
- `new_status` (VARCHAR(30), NOT NULL)
- `changed_by` (BIGINT, NULL, FK to `users.id` ON DELETE SET NULL)
- `note` (VARCHAR(500), NULL)
- `changed_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)
- **Indexes:**
  - `idx_status_history_request (request_id, changed_at)`

### 2.9 `recycling_records`
Official outcome certificate generated once processed at authorized facility.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `request_id` (BIGINT, NOT NULL, UNIQUE, FK to `pickup_requests.id` ON DELETE CASCADE)
- `recycler_name` (VARCHAR(150), NOT NULL)
- `received_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)
- `material_weight` (DECIMAL(10, 2), NOT NULL)
- `notes` (VARCHAR(500), NULL)

### 2.10 `reward_transactions`
Idempotent points ledger credited upon completed collection.
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `user_id` (BIGINT, NOT NULL, FK to `users.id` ON DELETE CASCADE)
- `request_id` (BIGINT, NOT NULL, UNIQUE, FK to `pickup_requests.id` ON DELETE CASCADE)
- `points` (INT, NOT NULL)
- `reason` (VARCHAR(255), NOT NULL)
- `created_at` (DATETIME, NOT NULL, default CURRENT_TIMESTAMP)
- **Indexes:**
  - `uk_reward_transactions_request (request_id)` (Unique constraint enforcing idempotency)
  - `idx_reward_user_created (user_id, created_at)`

### 2.11 `user_streaks`
Tracks consecutive days of recycling activity for bonus points and gamification.
- `user_id` (BIGINT, PRIMARY KEY, FK to `users.id` ON DELETE CASCADE)
- `current_streak` (INT, NOT NULL DEFAULT 0)
- `longest_streak` (INT, NOT NULL DEFAULT 0)
- `last_qualifying_date` (DATE, NULL)

---

## 3. Key Design Considerations

1. **Idempotency & Data Integrity:**
   `reward_transactions.request_id` is constrained `UNIQUE`. Even if a user refreshes the page or triggers repeated requests, at most one reward transaction can ever be created for a single request.
2. **Financial/Weight Precision:**
   `DECIMAL(10, 2)` is strictly used for quantities and weights rather than floating-point numbers (`FLOAT` / `DOUBLE`) to prevent rounding errors.
3. **Auditability:**
   Every lifecycle transition (`PENDING` -> `APPROVED` -> `ASSIGNED` -> `PICKED_UP` -> `COMPLETED`) creates an immutable row in `request_status_history` logging the acting user, previous state, new state, timestamp, and optional remarks.
4. **Normalized Emails:**
   Emails are trimmed and converted to lowercase before persistence and lookups to eliminate duplicate account vulnerabilities.
