-- ============================================================
-- DineSync - Seed Data
-- Run AFTER schema.sql to populate sample data
-- ============================================================

-- ============================================================
-- USERS
-- Passwords are BCrypt hashes ($2a$, strength 10) generated with
-- Spring Security BCryptPasswordEncoder — the same encoder DineSync uses.
-- Demo logins:
--   Admin:     admin@dinesync.com / admin123
--   Customers: alice@example.com, bob@example.com, carol@example.com,
--              david@example.com / password123
-- ============================================================
INSERT INTO users (name, email, phone, password, role) VALUES
  ('Admin User',    'admin@dinesync.com',   '+1-555-0100', '$2a$10$aeMQFvvKBXYikz3fbND77O1o2lgkJJGRNW.4eyUp7k2ffY32HF8lW', 'ADMIN'),
  ('Alice Johnson', 'alice@example.com',    '+1-555-0101', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'),
  ('Bob Martinez',  'bob@example.com',      '+1-555-0102', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'),
  ('Carol White',   'carol@example.com',    '+1-555-0103', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'),
  ('David Patel',   'david@example.com',    '+1-555-0104', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER');
-- Admin hash is for plaintext "admin123"
-- Customer hashes are for plaintext "password123"

-- ============================================================
-- RESTAURANTS
-- 3 fictional restaurants with varied cuisines
-- ============================================================
INSERT INTO restaurants (name, description, location, cuisine, price_range, rating, phone, opening_time, closing_time, image_url) VALUES
(
  'Saffron House',
  'An elegant fine-dining experience celebrating the rich spices of the Mediterranean. Award-winning chef curates a seasonal menu inspired by traditional coastal flavors.',
  'Downtown, Mumbai',
  'Mediterranean',
  '$$$',
  4.7,
  '+91-22-4001-1111',
  '12:00',
  '23:00',
  'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&auto=format&fit=crop'
),
(
  'The Bamboo Garden',
  'Authentic Asian fusion cuisine in a serene bamboo-themed setting. From delicate dim sum to bold ramen, every dish tells a story.',
  'Bandra West, Mumbai',
  'Asian Fusion',
  '$$',
  4.5,
  '+91-22-4002-2222',
  '11:30',
  '22:30',
  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop'
),
(
  'Ember & Oak',
  'A contemporary steakhouse where wood-fired techniques meet farm-to-table philosophy. Known for our signature dry-aged cuts and handcrafted cocktails.',
  'Juhu, Mumbai',
  'Contemporary',
  '$$$$',
  4.8,
  '+91-22-4003-3333',
  '18:00',
  '23:30',
  'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800&auto=format&fit=crop'
);

-- ============================================================
-- RESTAURANT TABLES
-- 5-6 tables per restaurant with varying capacities
-- ============================================================

-- Saffron House (restaurant_id = 1) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (1, 'T01', 2,  'Window'),
  (1, 'T02', 2,  'Window'),
  (1, 'T03', 4,  'Main Hall'),
  (1, 'T04', 4,  'Main Hall'),
  (1, 'T05', 6,  'Private'),
  (1, 'T06', 8,  'Private');

-- The Bamboo Garden (restaurant_id = 2) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (2, 'T01', 2,  'Garden View'),
  (2, 'T02', 4,  'Garden View'),
  (2, 'T03', 4,  'Indoor'),
  (2, 'T04', 6,  'Indoor'),
  (2, 'T05', 6,  'Private Room'),
  (2, 'T06', 10, 'Banquet');

-- Ember & Oak (restaurant_id = 3) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (3, 'T01', 2,  'Bar Side'),
  (3, 'T02', 2,  'Bar Side'),
  (3, 'T03', 4,  'Main Dining'),
  (3, 'T04', 4,  'Main Dining'),
  (3, 'T05', 6,  'Patio'),
  (3, 'T06', 8,  'Private Booth');

-- ============================================================
-- MENU ITEMS
-- 6-8 items per restaurant
-- ============================================================

-- Saffron House Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available) VALUES
  (1, 'Mezze Platter',          'Hummus, baba ganoush, olives, pita bread',            'Starter',  650.00, TRUE),
  (1, 'Lamb Kofta',             'Spiced minced lamb on skewers with tzatziki',          'Starter',  850.00, TRUE),
  (1, 'Sea Bass Tagine',        'Slow-cooked sea bass with preserved lemon',            'Main',    1450.00, TRUE),
  (1, 'Saffron Risotto',        'Arborio rice with saffron, parmesan, truffle oil',     'Main',    1250.00, TRUE),
  (1, 'Grilled Lamb Chops',     'French-trimmed lamb with harissa glaze',               'Main',    1850.00, TRUE),
  (1, 'Baklava Cheesecake',     'Pistachio baklava layered with vanilla cheesecake',    'Dessert',  550.00, TRUE),
  (1, 'Rosewater Panna Cotta',  'Italian panna cotta with rosewater and cardamom',      'Dessert',  450.00, TRUE),
  (1, 'Mint Lemonade',          'Fresh mint with hand-squeezed lemon',                  'Drink',    250.00, TRUE);

-- The Bamboo Garden Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available) VALUES
  (2, 'Edamame',                'Steamed salted soybeans',                              'Starter',  250.00, TRUE),
  (2, 'Pork Dim Sum (6 pcs)',   'Steamed pork and prawn dumplings with soy',            'Starter',  550.00, TRUE),
  (2, 'Crispy Spring Rolls',    'Vegetable spring rolls with sweet chilli sauce',        'Starter',  400.00, TRUE),
  (2, 'Tonkotsu Ramen',         'Rich pork bone broth, chashu, soft egg, nori',         'Main',     850.00, TRUE),
  (2, 'Pad Thai',               'Stir-fried rice noodles with prawns, tamarind',        'Main',     750.00, TRUE),
  (2, 'Kung Pao Chicken',       'Wok-tossed chicken with peanuts and Sichuan chilli',   'Main',     800.00, TRUE),
  (2, 'Mango Sticky Rice',      'Thai glutinous rice with fresh mango slices',          'Dessert',  400.00, TRUE),
  (2, 'Jasmine Tea',            'Premium whole-leaf jasmine green tea',                  'Drink',    200.00, TRUE);

-- Ember & Oak Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available) VALUES
  (3, 'Beef Tartare',           'Hand-cut wagyu with capers, quail egg, truffle oil',   'Starter', 1200.00, TRUE),
  (3, 'Burrata Salad',          'Fresh burrata with heirloom tomatoes, basil oil',       'Starter',  850.00, TRUE),
  (3, 'Bone Marrow',            'Roasted bone marrow with sourdough toast',              'Starter',  950.00, TRUE),
  (3, 'Dry-Aged Ribeye 300g',   'Prime dry-aged ribeye, charcoal-grilled to perfection','Main',    3200.00, TRUE),
  (3, 'Wagyu Striploin 250g',   'A5 wagyu with red wine reduction',                     'Main',    4500.00, TRUE),
  (3, 'Pan-Seared Salmon',      'Atlantic salmon with cauliflower puree, dill oil',     'Main',    1850.00, TRUE),
  (3, 'Chocolate Fondant',      'Warm dark chocolate fondant, vanilla ice cream',        'Dessert',  750.00, TRUE),
  (3, 'Old Fashioned',          'Bourbon, orange bitters, demerara sugar',               'Drink',    800.00, TRUE);

-- ============================================================
-- RESERVATIONS
-- Several bookings across dates (some past, some upcoming)
-- ============================================================
INSERT INTO reservations (user_id, restaurant_id, table_id, reservation_date, start_time, end_time, guests, status) VALUES
  -- Past reservations (COMPLETED)
  (2, 1, 3,  '2026-09-10', '19:00', '21:00', 4, 'COMPLETED'),
  (3, 2, 7,  '2026-09-12', '20:00', '22:00', 2, 'COMPLETED'),
  (4, 3, 15, '2026-09-15', '19:30', '21:30', 4, 'COMPLETED'),
  (5, 1, 5,  '2026-09-18', '20:00', '22:00', 6, 'COMPLETED'),
  (2, 3, 16, '2026-09-20', '18:30', '20:30', 4, 'COMPLETED'),
  -- Cancelled
  (3, 1, 4,  '2026-09-22', '19:00', '21:00', 3, 'CANCELLED'),
  -- Upcoming/today reservations (CONFIRMED)
  (2, 1, 3,  '2026-09-28', '19:30', '21:30', 4, 'CONFIRMED'),
  (3, 2, 9,  '2026-09-28', '20:00', '22:00', 4, 'CONFIRMED'),
  (4, 3, 17, '2026-09-29', '19:00', '21:00', 6, 'CONFIRMED'),
  (5, 2, 10, '2026-09-30', '20:30', '22:30', 6, 'CONFIRMED'),
  (2, 3, 18, '2026-10-01', '18:30', '20:30', 8, 'CONFIRMED');

-- ============================================================
-- REVIEWS
-- Customer reviews for restaurants
-- ============================================================
INSERT INTO reviews (user_id, restaurant_id, rating, comment) VALUES
  (2, 1, 5, 'Absolutely stunning. The saffron risotto was divine and the service impeccable. Will definitely return!'),
  (3, 1, 4, 'Beautiful ambience and great food. The lamb chops were perfectly cooked. Slightly pricey but worth it.'),
  (4, 2, 5, 'Best ramen in the city! The bamboo decor is so soothing. Love the dim sum too.'),
  (5, 2, 4, 'Lovely experience. Pad Thai was excellent. Got a bit busy on weekends.'),
  (2, 3, 5, 'The wagyu striploin was life-changing. Perfect char, incredible marbling. The bone marrow starter is a must-try.'),
  (3, 3, 5, 'Premium experience from start to finish. The cocktails are brilliant and the steaks are worth every rupee.'),
  (4, 1, 4, 'Mezze platter is wonderful for sharing. The rosewater panna cotta is a delight.'),
  (5, 3, 4, 'Great steakhouse. The ribeye was cooked exactly as asked. Loved the rustic vibe.');

-- Update restaurant ratings based on average review ratings
UPDATE restaurants SET rating = (
    SELECT ROUND(AVG(rating)::NUMERIC, 1) FROM reviews WHERE reviews.restaurant_id = restaurants.restaurant_id
);
