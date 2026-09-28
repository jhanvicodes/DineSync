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
),
(
  'Bella Roma',
  'Classic Italian comfort food with handmade pasta, wood-fired pizzas, and an intimate candlelit atmosphere.',
  'Lower Parel, Mumbai',
  'Italian',
  '$$$',
  4.8,
  '+91-22-4101-1001',
  '12:30',
  '23:30',
  'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop'
),
(
  'Sakura Bay',
  'Fresh sushi, omakase platters, and elevated Japanese flavors in a minimal waterfront setting.',
  'Marine Drive, Mumbai',
  'Japanese',
  '$$$$',
  4.9,
  '+91-22-4101-2002',
  '13:00',
  '00:00',
  'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800&auto=format&fit=crop'
),
(
  'Spice Route',
  'A vibrant Indian dining experience rooted in regional recipes, smoky tandoor classics, and modern plating.',
  'Powai, Mumbai',
  'Indian',
  '$$',
  4.6,
  '+91-22-4101-3003',
  '12:00',
  '23:00',
  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop'
),
(
  'El Sol Cantina',
  'Bright Mexican flavors, handcrafted margaritas, and a lively social dining atmosphere for every night out.',
  'Andheri West, Mumbai',
  'Mexican',
  '$$$',
  4.5,
  '+91-22-4101-4004',
  '17:00',
  '23:30',
  'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800&auto=format&fit=crop'
),
(
  'Olive & Cedar',
  'Mediterranean-inspired plates with grilled seafood, fresh mezze, and a polished rooftop backdrop.',
  'Colaba, Mumbai',
  'Mediterranean',
  '$$$',
  4.7,
  '+91-22-4101-5005',
  '12:00',
  '23:30',
  'https://images.unsplash.com/photo-1544025162-d76694265947?w=800&auto=format&fit=crop'
),
(
  'Nori & Co.',
  'Pan-Asian small plates, sushi rolls, and contemporary wok dishes designed for shared dining.',
  'Bandra, Mumbai',
  'Pan-Asian',
  '$$$',
  4.6,
  '+91-22-4101-6006',
  '11:30',
  '22:30',
  'https://images.unsplash.com/photo-1526318896980-cf78c088247c?w=800&auto=format&fit=crop'
),
(
  'The Bean & Bloom',
  'A relaxed cafe-bistro favorite with artisanal coffee, brunch plates, and all-day seasonal comfort food.',
  'Juhu, Mumbai',
  'Cafe/Bistro',
  '$$',
  4.4,
  '+91-22-4101-7007',
  '08:00',
  '22:00',
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&auto=format&fit=crop'
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

-- Bella Roma (restaurant_id = 4) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (4, 'T01', 2, 'Window Side'),
  (4, 'T02', 4, 'Main Dining'),
  (4, 'T03', 4, 'Terrace'),
  (4, 'T04', 6, 'Private Room');

-- Sakura Bay (restaurant_id = 5) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (5, 'T01', 2, 'Harbor View'),
  (5, 'T02', 2, 'Harbor View'),
  (5, 'T03', 4, 'Counter'),
  (5, 'T04', 6, 'Private Room');

-- Spice Route (restaurant_id = 6) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (6, 'T01', 2, 'Courtyard'),
  (6, 'T02', 2, 'Courtyard'),
  (6, 'T03', 4, 'Main Hall'),
  (6, 'T04', 6, 'Family Booth');

-- El Sol Cantina (restaurant_id = 7) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (7, 'T01', 2, 'Patio'),
  (7, 'T02', 4, 'Patio'),
  (7, 'T03', 4, 'Main Dining'),
  (7, 'T04', 6, 'Party Booth');

-- Olive & Cedar (restaurant_id = 8) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (8, 'T01', 2, 'Rooftop'),
  (8, 'T02', 2, 'Rooftop'),
  (8, 'T03', 4, 'Sea View'),
  (8, 'T04', 6, 'Private Lounge');

-- Nori & Co. (restaurant_id = 9) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (9, 'T01', 2, 'Bar'),
  (9, 'T02', 4, 'Dining Room'),
  (9, 'T03', 6, 'Private Room');

-- The Bean & Bloom (restaurant_id = 10) tables
INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES
  (10, 'T01', 2, 'Window'),
  (10, 'T02', 2, 'Window'),
  (10, 'T03', 4, 'Cafe Corner'),
  (10, 'T04', 6, 'Patio');

-- ============================================================
-- MENU ITEMS
-- The seven requested restaurants only
-- ============================================================

-- Sakura Bay Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 5, 'Truffle Salmon Aburi Roll', 'Seared Norwegian salmon, black truffle oil, avocado, Japanese cucumber, unagi reduction', 'Starter', 950.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 5 AND name = 'Truffle Salmon Aburi Roll');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 5, 'Bluefin Tuna & Hamachi Nigiri Platter', '6-piece chef''s selection of fresh sashimi-grade tuna and yellowtail with fresh wasabi', 'Main', 1450.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 5 AND name = 'Bluefin Tuna & Hamachi Nigiri Platter');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 5, 'Miso Glazed Black Cod', 'Marinated in sweet saikyo miso for 48 hours, served with pickled ginger shoot', 'Main', 1850.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 5 AND name = 'Miso Glazed Black Cod');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 5, 'Tonkotsu Pork Belly Ramen', '16-hour simmered rich pork broth, handmade ramen noodles, ajitama nitamago egg, bamboo shoots', 'Main', 820.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 5 AND name = 'Tonkotsu Pork Belly Ramen');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 5, 'Matcha Lava Cake', 'Uji matcha molten cake with white chocolate center, paired with black sesame gelato', 'Dessert', 480.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 5 AND name = 'Matcha Lava Cake');

-- Bella Roma Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 4, 'Handcrafted Burrata Caprese', 'Fresh artisanal burrata, heirloom tomatoes, basil oil, aged balsamic reduction', 'Starter', 680.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 4 AND name = 'Handcrafted Burrata Caprese');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 4, 'Truffle Wild Mushroom Fettuccine', 'Fresh handmade fettuccine, black truffle butter cream, porcini mushrooms, parmigiano reggiano', 'Main', 850.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 4 AND name = 'Truffle Wild Mushroom Fettuccine');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 4, 'Wood-Fired Neapolitan Diavola Pizza', 'San Marzano tomato sauce, fresh mozzarella, spicy artisan pepperoni, fresh basil', 'Main', 890.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 4 AND name = 'Wood-Fired Neapolitan Diavola Pizza');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 4, 'Classic Slow-Cooked Ossobuco', 'Braised veal shank with saffron risotto alla Milanese', 'Main', 1250.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 4 AND name = 'Classic Slow-Cooked Ossobuco');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 4, 'Authentic Espresso Tiramisu', 'Savoiardi ladyfingers, freshly brewed dark espresso, mascarpone cream, dark cocoa dust', 'Dessert', 420.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 4 AND name = 'Authentic Espresso Tiramisu');

-- Olive & Cedar Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 8, 'Signature Hummus & Warm Pita Board', 'Creamy chickpea hummus topped with roasted pine nuts, za''atar, sumac, served with house-baked woodfired pita', 'Starter', 520.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 8 AND name = 'Signature Hummus & Warm Pita Board');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 8, 'Greek Grilled Octopus', 'Charred octopus tentacles, lemon-oregano olive oil vinaigrette, fava bean puree, crispy capers', 'Starter', 980.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 8 AND name = 'Greek Grilled Octopus');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 8, 'Levantine Spiced Lamb Tagine', 'Slow-cooked tender lamb shank, apricots, toasted almonds, served over saffron couscous', 'Main', 1150.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 8 AND name = 'Levantine Spiced Lamb Tagine');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 8, 'Falafel & Halloumi Mezze Bowl', 'Herbed chickpea falafels, pan-seared halloumi cheese, tzatziki, pickled turnip, tabbouleh', 'Main', 720.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 8 AND name = 'Falafel & Halloumi Mezze Bowl');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 8, 'Pistachio Baklava with Fig Gelato', 'Layers of crisp phyllo pastry, roasted pistachios, honey syrup, served with spiced fig gelato', 'Dessert', 450.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 8 AND name = 'Pistachio Baklava with Fig Gelato');

-- Nori & Co. Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 9, 'Edamame & Truffle Crystal Dumplings', 'Steamed translucent dumplings packed with crushed edamame, shiitake, and truffle emulsion', 'Starter', 580.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 9 AND name = 'Edamame & Truffle Crystal Dumplings');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 9, 'Korean Crispy Glazed Gochujang Wings', 'Double-fried chicken wings tossed in sticky sweet-and-spicy gochujang, sesame, scallions', 'Starter', 620.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 9 AND name = 'Korean Crispy Glazed Gochujang Wings');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 9, 'Pork Belly Baos', 'Fluffy steamed bao buns, glazed braised pork belly, pickled cucumber, crushed peanuts, hoisin sauce', 'Main', 650.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 9 AND name = 'Pork Belly Baos');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 9, 'Thai Green Curry with Jasmine Rice', 'Aromatic coconut green curry with bamboo shoots, Thai basil, water chestnuts, choice of chicken or tofu', 'Main', 780.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 9 AND name = 'Thai Green Curry with Jasmine Rice');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 9, 'Boba Boba Coconut Mango Kakigori', 'Shaved ice, fresh Alphonso mango puree, coconut cream, chewy tapioca pearls', 'Dessert', 420.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 9 AND name = 'Boba Boba Coconut Mango Kakigori');

-- Spice Route Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 6, 'Old Delhi Butter Chicken', 'Tandoor-roasted chicken tikka simmered in a velvety tomato, cashew, and fenugreek butter gravy', 'Main', 750.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 6 AND name = 'Old Delhi Butter Chicken');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 6, 'Chettinad Pepper Mutton Curry', 'Fiery Tamil Nadu style slow-cooked mutton with roasted spices, curry leaves, and black pepper', 'Main', 820.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 6 AND name = 'Chettinad Pepper Mutton Curry');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 6, 'Dal Bukhara', 'Black lentils simmered overnight for 18 hours with tomato, cream, and white butter', 'Main', 580.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 6 AND name = 'Dal Bukhara');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 6, 'Hydrabadi Dum Prawn Biryani', 'Fragrant long-grain basmati rice cooked with spiced prawns, saffron, fried onions under a sealed dough crust', 'Main', 920.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 6 AND name = 'Hydrabadi Dum Prawn Biryani');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 6, 'Shahi Tukda with Rabri', 'Deep-fried ghee bread topped with cardamom-infused rabri and silver leaf', 'Dessert', 380.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 6 AND name = 'Shahi Tukda with Rabri');

-- El Sol Cantina Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 7, 'Baja Crispy Fish Tacos', 'Beer-batter fried local catch, chipotle mayo, purple cabbage slaw, avocado on handmade corn tortillas', 'Main', 620.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 7 AND name = 'Baja Crispy Fish Tacos');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 7, 'Birria Beef Tacos with Consomé', 'Slow-braised shredded beef, melted Oaxaca cheese, served with rich dipping broth', 'Main', 680.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 7 AND name = 'Birria Beef Tacos with Consomé');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 7, 'Loaded Guacamole Tableside', 'Fresh Hass avocado smashed tableside with lime, jalapeño, cilantro, topped with queso fresco and house tortilla chips', 'Starter', 550.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 7 AND name = 'Loaded Guacamole Tableside');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 7, 'Sizzling Chicken Fajitas', 'Seared spiced chicken breast, bell peppers, onions on a hot skillet, served with warm flour tortillas & sour cream', 'Main', 750.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 7 AND name = 'Sizzling Chicken Fajitas');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 7, 'Churros con Chocolate', 'Golden fried cinnamon sugar churros with thick dark Mexican chocolate dipping sauce', 'Dessert', 390.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 7 AND name = 'Churros con Chocolate');

-- The Bean & Bloom Menu
INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 10, 'Avocado & Poached Egg Toast', 'Sourdough toast, smashed Hass avocado, free-range poached eggs, chili flakes, microgreens', 'Starter', 520.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 10 AND name = 'Avocado & Poached Egg Toast');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 10, 'Truffle Mushroom Brioche Melt', 'Sautéed wild mushrooms, aged Gruyère cheese, truffle oil melted in toasted brioche', 'Main', 580.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 10 AND name = 'Truffle Mushroom Brioche Melt');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 10, 'Spinach & Ricotta Ravioli in Sage Butter', 'Handmade pasta pockets filled with spinach and creamy ricotta in light sage butter', 'Main', 680.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 10 AND name = 'Spinach & Ricotta Ravioli in Sage Butter');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 10, 'Rose & Lavender Iced Latte', 'Cold-brewed espresso shaken with oat milk and house-made floral botanical syrup', 'Drink', 320.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 10 AND name = 'Rose & Lavender Iced Latte');

INSERT INTO menu_items (restaurant_id, name, description, category, price, available)
SELECT 10, 'Salted Caramel French Toast', 'Thick-cut brioche soaked in vanilla custard, seared golden, topped with caramelized bananas and pecans', 'Dessert', 450.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = 10 AND name = 'Salted Caramel French Toast');

-- ============================================================
-- GUEST REVIEWS
-- ============================================================
INSERT INTO users (name, email, phone, password, role)
SELECT 'Rohan Mehta', 'rohan.mehta@dinesync.com', '+91-98765-1001', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'rohan.mehta@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Ananya Iyer', 'ananya.iyer@dinesync.com', '+91-98765-1002', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ananya.iyer@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Vikram Sengupta', 'vikram.sengupta@dinesync.com', '+91-98765-1003', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vikram.sengupta@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Priya Kulkarni', 'priya.kulkarni@dinesync.com', '+91-98765-1004', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'priya.kulkarni@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Karan Sharma', 'karan.sharma@dinesync.com', '+91-98765-1005', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'karan.sharma@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Simran Bhatia', 'simran.bhatia@dinesync.com', '+91-98765-1006', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'simran.bhatia@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Devina Kapoor', 'devina.kapoor@dinesync.com', '+91-98765-1007', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'devina.kapoor@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Aditya Rao', 'aditya.rao@dinesync.com', '+91-98765-1008', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'aditya.rao@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Siddharth Verma', 'siddharth.verma@dinesync.com', '+91-98765-1009', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'siddharth.verma@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Meera Nair', 'meera.nair@dinesync.com', '+91-98765-1010', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'meera.nair@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Varun Joshi', 'varun.joshi@dinesync.com', '+91-98765-1011', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'varun.joshi@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Neha Fernandes', 'neha.fernandes@dinesync.com', '+91-98765-1012', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'neha.fernandes@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Tanvi Shah', 'tanvi.shah@dinesync.com', '+91-98765-1013', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'tanvi.shah@dinesync.com');

INSERT INTO users (name, email, phone, password, role)
SELECT 'Kabir Merchant', 'kabir.merchant@dinesync.com', '+91-98765-1014', '$2a$10$2TrREUDKo44Nf5d4mZt87OYs.3xS9R2lroydDiR1jMssE7edTJ/S6', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'kabir.merchant@dinesync.com');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'rohan.mehta@dinesync.com'), 5, 5, 'The Truffle Salmon Aburi roll literally melts in your mouth! Incredible view of Marine Drive paired with world-class sushi.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 5 AND comment = 'The Truffle Salmon Aburi roll literally melts in your mouth! Incredible view of Marine Drive paired with world-class sushi.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'ananya.iyer@dinesync.com'), 5, 5, 'Authentic Japanese flavors. The Miso Black Cod was cooked to absolute perfection. High pricing but worth every rupee.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 5 AND comment = 'Authentic Japanese flavors. The Miso Black Cod was cooked to absolute perfection. High pricing but worth every rupee.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'vikram.sengupta@dinesync.com'), 4, 5, 'Best wood-fired Neapolitan pizza in Mumbai! The sourdough crust has the perfect char and lightness.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 4 AND comment = 'Best wood-fired Neapolitan pizza in Mumbai! The sourdough crust has the perfect char and lightness.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'priya.kulkarni@dinesync.com'), 4, 5, 'Warm ambiance and heavenly handmade pastas. Reminds me of cozy trattorias in Florence.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 4 AND comment = 'Warm ambiance and heavenly handmade pastas. Reminds me of cozy trattorias in Florence.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'karan.sharma@dinesync.com'), 8, 5, 'The lamb tagine is deeply aromatic and ridiculously tender. Great alfresco seating area!'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 8 AND comment = 'The lamb tagine is deeply aromatic and ridiculously tender. Great alfresco seating area!');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'simran.bhatia@dinesync.com'), 8, 5, 'Outstanding hummus and fresh warm pita. The cocktail pairings elevated the whole dinner experience.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 8 AND comment = 'Outstanding hummus and fresh warm pita. The cocktail pairings elevated the whole dinner experience.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'devina.kapoor@dinesync.com'), 9, 5, 'The crystal truffle dumplings are an absolute must-order! Perfect post-work dinner spot in BKC.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 9 AND comment = 'The crystal truffle dumplings are an absolute must-order! Perfect post-work dinner spot in BKC.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'aditya.rao@dinesync.com'), 9, 5, 'Love the Izakaya vibe and pan-Asian selection. Quick service and excellent Gochujang wings.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 9 AND comment = 'Love the Izakaya vibe and pan-Asian selection. Quick service and excellent Gochujang wings.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'siddharth.verma@dinesync.com'), 6, 5, 'The Dal Bukhara and Hydrabadi Biryani taste straight out of a royal kitchen. True heritage fine dining.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 6 AND comment = 'The Dal Bukhara and Hydrabadi Biryani taste straight out of a royal kitchen. True heritage fine dining.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'meera.nair@dinesync.com'), 6, 5, 'Incredible depth of flavor in the Chettinad curry! Authentic spices and rich royal ambiance.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 6 AND comment = 'Incredible depth of flavor in the Chettinad curry! Authentic spices and rich royal ambiance.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'varun.joshi@dinesync.com'), 7, 5, 'The Birria Tacos are rich, savory, and cooked to perfection! Fun, vibrant Mexican vibes.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 7 AND comment = 'The Birria Tacos are rich, savory, and cooked to perfection! Fun, vibrant Mexican vibes.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'neha.fernandes@dinesync.com'), 7, 5, 'Tableside guacamole and great churros. Perfect spot for weekend dinner with friends.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 7 AND comment = 'Tableside guacamole and great churros. Perfect spot for weekend dinner with friends.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'tanvi.shah@dinesync.com'), 10, 5, 'Charming aesthetic with lush greenery! The Truffle Brioche melt and Rose Latte made for a delightful brunch.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 10 AND comment = 'Charming aesthetic with lush greenery! The Truffle Brioche melt and Rose Latte made for a delightful brunch.');

INSERT INTO reviews (user_id, restaurant_id, rating, comment)
SELECT (SELECT user_id FROM users WHERE email = 'kabir.merchant@dinesync.com'), 10, 5, 'Very aesthetic decor, pleasant outdoor seating, and high-quality artisanal coffee.'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE restaurant_id = 10 AND comment = 'Very aesthetic decor, pleasant outdoor seating, and high-quality artisanal coffee.');

UPDATE restaurants SET rating = (
    SELECT ROUND(AVG(rating)::NUMERIC, 1) FROM reviews WHERE reviews.restaurant_id = restaurants.restaurant_id
);
