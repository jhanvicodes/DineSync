-- ============================================================
-- DineSync - Database Schema
-- Run this in Supabase SQL Editor to create all tables
-- ============================================================

-- Drop tables in reverse order of dependencies (for clean re-runs)
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS menu_items CASCADE;
DROP TABLE IF EXISTS restaurant_tables CASCADE;
DROP TABLE IF EXISTS restaurants CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- TABLE: users
-- Stores both CUSTOMER and ADMIN accounts
-- ============================================================
CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,          -- UNIQUE: no duplicate emails
    phone       VARCHAR(20),
    password    VARCHAR(255) NOT NULL,                 -- Stored as BCrypt hash
    role        VARCHAR(10) NOT NULL DEFAULT 'CUSTOMER'
                CHECK (role IN ('CUSTOMER', 'ADMIN')), -- Only valid roles allowed
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE: restaurants
-- Each restaurant is one row in this table
-- ============================================================
CREATE TABLE restaurants (
    restaurant_id   SERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    location        VARCHAR(200) NOT NULL,
    cuisine         VARCHAR(100) NOT NULL,
    price_range     VARCHAR(10) NOT NULL DEFAULT '$$'
                    CHECK (price_range IN ('$', '$$', '$$$', '$$$$')),
    rating          NUMERIC(2,1) NOT NULL DEFAULT 0.0
                    CHECK (rating >= 0 AND rating <= 5),
    phone           VARCHAR(20),
    opening_time    TIME NOT NULL,
    closing_time    TIME NOT NULL,
    image_url       VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE: restaurant_tables
-- Each physical table inside a restaurant
-- ============================================================
CREATE TABLE restaurant_tables (
    table_id        SERIAL PRIMARY KEY,
    restaurant_id   INT NOT NULL REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    table_number    VARCHAR(10) NOT NULL,               -- e.g. 'T01', 'T02'
    capacity        INT NOT NULL CHECK (capacity > 0),  -- How many guests can sit
    location        VARCHAR(50) DEFAULT 'Main Hall',    -- e.g. 'Window', 'Patio'
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Each table number must be unique within the same restaurant
    UNIQUE (restaurant_id, table_number)
);

-- ============================================================
-- TABLE: menu_items
-- Food/drinks offered by each restaurant
-- ============================================================
CREATE TABLE menu_items (
    menu_id         SERIAL PRIMARY KEY,
    restaurant_id   INT NOT NULL REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    category        VARCHAR(50) NOT NULL,               -- e.g. 'Starter', 'Main', 'Dessert'
    price           NUMERIC(8,2) NOT NULL CHECK (price >= 0),
    image_url       VARCHAR(500),
    available       BOOLEAN NOT NULL DEFAULT TRUE       -- Can be marked unavailable
);

-- ============================================================
-- TABLE: reservations
-- Core table: links user + restaurant + table for a date/time
-- ============================================================
CREATE TABLE reservations (
    reservation_id  SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(user_id),
    restaurant_id   INT NOT NULL REFERENCES restaurants(restaurant_id),
    table_id        INT NOT NULL REFERENCES restaurant_tables(table_id),
    reservation_date DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    guests          INT NOT NULL CHECK (guests > 0),
    status          VARCHAR(15) NOT NULL DEFAULT 'CONFIRMED'
                    CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index for fast availability lookups (the core reservation logic query)
CREATE INDEX idx_reservations_table_date
    ON reservations(table_id, reservation_date, status);

-- ============================================================
-- TABLE: reviews
-- Customer reviews for restaurants (1–5 star rating)
-- ============================================================
CREATE TABLE reviews (
    review_id       SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(user_id),
    restaurant_id   INT NOT NULL REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    rating          INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
