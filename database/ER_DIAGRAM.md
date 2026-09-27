# DineSync — Entity-Relationship Diagram

## Entities & Attributes

### users
| Column | Type | Notes |
|--------|------|-------|
| user_id (PK) | INT (SERIAL) | Auto-increment primary key |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| phone | VARCHAR(20) | Optional |
| password | VARCHAR(255) | BCrypt hashed, NOT NULL |
| role | VARCHAR(10) | 'CUSTOMER' or 'ADMIN', CHECK constraint |
| created_at | TIMESTAMP | Default: NOW() |

### restaurants
| Column | Type | Notes |
|--------|------|-------|
| restaurant_id (PK) | INT (SERIAL) | Auto-increment primary key |
| name | VARCHAR(150) | NOT NULL |
| description | TEXT | Optional |
| location | VARCHAR(200) | NOT NULL |
| cuisine | VARCHAR(100) | NOT NULL |
| price_range | VARCHAR(10) | '$', '$$', '$$$', '$$$$' |
| rating | NUMERIC(2,1) | 0.0–5.0, CHECK constraint |
| phone | VARCHAR(20) | Optional |
| opening_time | TIME | NOT NULL |
| closing_time | TIME | NOT NULL |
| image_url | VARCHAR(500) | URL to restaurant photo |
| created_at | TIMESTAMP | Default: NOW() |

### restaurant_tables
| Column | Type | Notes |
|--------|------|-------|
| table_id (PK) | INT (SERIAL) | Auto-increment primary key |
| restaurant_id (FK) | INT | References restaurants(restaurant_id) |
| table_number | VARCHAR(10) | e.g. 'T01', UNIQUE within restaurant |
| capacity | INT | CHECK: > 0 |
| location | VARCHAR(50) | e.g. 'Window', 'Patio' |
| created_at | TIMESTAMP | Default: NOW() |

### menu_items
| Column | Type | Notes |
|--------|------|-------|
| menu_id (PK) | INT (SERIAL) | Auto-increment primary key |
| restaurant_id (FK) | INT | References restaurants(restaurant_id) |
| name | VARCHAR(150) | NOT NULL |
| description | TEXT | Optional |
| category | VARCHAR(50) | e.g. 'Starter', 'Main', 'Dessert' |
| price | NUMERIC(8,2) | CHECK: >= 0 |
| image_url | VARCHAR(500) | Optional photo URL |
| available | BOOLEAN | Default: TRUE |

### reservations
| Column | Type | Notes |
|--------|------|-------|
| reservation_id (PK) | INT (SERIAL) | Auto-increment primary key |
| user_id (FK) | INT | References users(user_id) |
| restaurant_id (FK) | INT | References restaurants(restaurant_id) |
| table_id (FK) | INT | References restaurant_tables(table_id) |
| reservation_date | DATE | NOT NULL |
| start_time | TIME | NOT NULL |
| end_time | TIME | NOT NULL |
| guests | INT | CHECK: > 0 |
| status | VARCHAR(15) | 'CONFIRMED', 'CANCELLED', 'COMPLETED' |
| created_at | TIMESTAMP | Default: NOW() |

### reviews
| Column | Type | Notes |
|--------|------|-------|
| review_id (PK) | INT (SERIAL) | Auto-increment primary key |
| user_id (FK) | INT | References users(user_id) |
| restaurant_id (FK) | INT | References restaurants(restaurant_id) |
| rating | INT | CHECK: 1–5 |
| comment | TEXT | Optional |
| created_at | TIMESTAMP | Default: NOW() |

---

## Relationships

```
users ────────────────── reservations
  (1)                        (M)
  user_id ──── FK ──── user_id

restaurants ─────────── reservations
     (1)                    (M)
  restaurant_id ─── FK ─── restaurant_id

restaurant_tables ────── reservations
        (1)                    (M)
     table_id ──── FK ──── table_id

restaurants ─────────── restaurant_tables
     (1)                        (M)
  restaurant_id ─── FK ─── restaurant_id

restaurants ─────────── menu_items
     (1)                    (M)
  restaurant_id ─── FK ─── restaurant_id

restaurants ─────────── reviews
     (1)                  (M)
  restaurant_id ─── FK ─── restaurant_id

users ─────────────────── reviews
  (1)                       (M)
  user_id ──── FK ──── user_id
```

## Relationship Summary

| Relationship | Type | Description |
|---|---|---|
| users → reservations | One-to-Many | One user can have many reservations |
| restaurants → reservations | One-to-Many | One restaurant has many reservations |
| restaurant_tables → reservations | One-to-Many | One table can be reserved multiple times (different dates) |
| restaurants → restaurant_tables | One-to-Many | One restaurant has many tables |
| restaurants → menu_items | One-to-Many | One restaurant has many menu items |
| restaurants → reviews | One-to-Many | One restaurant has many reviews |
| users → reviews | One-to-Many | One user can write many reviews |

## DBMS Concepts Demonstrated

| Concept | Where Used |
|---|---|
| Primary Keys | All 6 tables — SERIAL primary key |
| Foreign Keys | reservations.user_id → users, reservations.table_id → restaurant_tables, etc. |
| UNIQUE Constraint | users.email, (restaurant_id, table_number) composite |
| CHECK Constraint | rating 1–5, role IN ('CUSTOMER','ADMIN'), price >= 0, capacity > 0 |
| NOT NULL | All mandatory fields |
| JOIN | Availability query joins reservations + tables |
| COUNT | Admin dashboard: total reservations, customers |
| AVG | Average rating, average guests |
| GROUP BY | Reservations per day, most booked restaurant |
| ORDER BY | Sorting reservations by date |
| WHERE | Filtering by date, status, user |
| CRUD | Full create/read/update/delete on all entities |
