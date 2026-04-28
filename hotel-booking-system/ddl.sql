-- Hotel Booking Management & Analytics — schema
-- Run against MySQL before loading data.sql / generated booking inserts.

CREATE DATABASE IF NOT EXISTS hotel_booking_db;
USE hotel_booking_db;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS hotels;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME NOT NULL
);

CREATE TABLE hotels (
    hotel_id INT AUTO_INCREMENT PRIMARY KEY,
    hotel_name VARCHAR(100) NOT NULL,
    hotel_type VARCHAR(50) NOT NULL,
    city VARCHAR(80),
    country_code VARCHAR(10),
    description TEXT
);

CREATE TABLE room_types (
    room_type_id INT AUTO_INCREMENT PRIMARY KEY,
    hotel_id INT NOT NULL,
    room_name VARCHAR(80) NOT NULL,
    max_adults INT NOT NULL,
    max_children INT NOT NULL,
    bed_type VARCHAR(50),
    price_per_night DECIMAL(10,2) NOT NULL,
    total_rooms INT NOT NULL,
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id)
);

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    hotel_id INT NOT NULL,
    room_type_id INT NOT NULL,
    booking_date DATETIME NOT NULL,
    arrival_date DATE NOT NULL,
    checkout_date DATE NOT NULL,
    adults INT NOT NULL,
    children INT DEFAULT 0,
    babies INT DEFAULT 0,
    meal_plan VARCHAR(50),
    market_segment VARCHAR(50),
    distribution_channel VARCHAR(50),
    is_repeated_guest BOOLEAN DEFAULT FALSE,
    previous_cancellations INT DEFAULT 0,
    previous_bookings_not_canceled INT DEFAULT 0,
    booking_status VARCHAR(30) NOT NULL,
    canceled BOOLEAN DEFAULT FALSE,
    adr DECIMAL(10,2),
    special_requests INT DEFAULT 0,
    parking_spaces INT DEFAULT 0,
    total_nights INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(30) NOT NULL,
    paid_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_hotel_id_arrival_date ON bookings(hotel_id, arrival_date);
CREATE INDEX idx_bookings_room_type_id ON bookings(room_type_id);
CREATE INDEX idx_users_email ON users(email);
