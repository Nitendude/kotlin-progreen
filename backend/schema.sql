CREATE DATABASE IF NOT EXISTS progreen CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE progreen;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'LGU', 'COMPANY', 'ADMIN') NOT NULL DEFAULT 'USER',
    points INT NOT NULL DEFAULT 0,
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    points_per_kg INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS donations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    lgu_user_id BIGINT UNSIGNED NOT NULL,
    category_id VARCHAR(50) NOT NULL,
    weight_kg DECIMAL(10,2) NOT NULL,
    points_earned INT NOT NULL,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_donations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_donations_lgu FOREIGN KEY (lgu_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_donations_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS rewards (
    id VARCHAR(50) PRIMARY KEY,
    provider_user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(150) NOT NULL,
    cost_points INT NOT NULL,
    reward_type VARCHAR(50) NOT NULL DEFAULT 'Item',
    description TEXT NOT NULL,
    image_base64 LONGTEXT NULL,
    redeem_code VARCHAR(120) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rewards_provider FOREIGN KEY (provider_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reward_redemptions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    reward_id VARCHAR(50) NOT NULL,
    redeem_code VARCHAR(120) NULL,
    points_spent INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_redemptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_redemptions_reward FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS lgu_sites (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL DEFAULT 0,
    map_query VARCHAR(255) NOT NULL
);

INSERT INTO categories (id, name, description, points_per_kg, sort_order) VALUES
('pet_white', 'PET - WHITE', 'Clear PET bottles and clean transparent containers', 6, 1),
('pet_colored', 'PET - COLORED', 'Colored PET bottles sorted by type', 3, 2),
('hdpe', 'HDPE', 'Detergent, shampoo, and milk jugs', 10, 3),
('ldpe', 'LDPE', 'Plastic bags and flexible plastic wraps', 1, 4),
('pe', 'PE', 'General polyethylene packaging', 5, 5),
('tin_cans', 'TIN CANS', 'Clean empty tin cans', 5, 6),
('cartons', 'CARTONS', 'Clean food and drink cartons', 1, 7)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description),
points_per_kg = VALUES(points_per_kg),
sort_order = VALUES(sort_order);

INSERT INTO lgu_sites (name, address, distance_km, map_query) VALUES
('Santo Tomas Eco Hub', 'Poblacion, Santo Tomas, Batangas', 1.20, 'Santo Tomas Eco Hub'),
('Batangas City Materials Recovery', 'Gov. Carpio Rd, Batangas City', 7.40, 'Batangas City Materials Recovery'),
('Tanauan Recycling Point', 'A. Mabini Ave, Tanauan', 9.30, 'Tanauan Recycling Point');
