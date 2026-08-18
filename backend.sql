-- -----------------------------------------------------------------------------
-- EnviroTrack — complete database setup
--
-- Creates the database and all four required tables:
--   sensors      — raw sensor readings written by simulator.py
--   predictions  — per-sensor forecast + anomaly flag, written by the Python
--                  layer and read by the Java dashboard
--   alerts       — generated threshold alerts
--   users        — login accounts
--
-- Run this in MySQL Workbench or the CLI as a user with admin rights (root):
--     mysql -u root -p < backend.sql
-- -----------------------------------------------------------------------------

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS envirotrack_db;

-- 2. Select it
USE envirotrack_db;

-- 3. Drop existing tables for a clean start
DROP TABLE IF EXISTS predictions;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS sensors;
DROP TABLE IF EXISTS users;

-- 4. sensors — one row per reading
CREATE TABLE sensors (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    location  VARCHAR(100) NOT NULL,
    value     DOUBLE       NOT NULL,
    timestamp TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    -- Readings are almost always queried per sensor, newest first
    -- (see Main.loadTableData and simulator.run_anomaly_check).
    INDEX idx_name_time (name, timestamp)
);

-- 5. predictions — at most one current row per sensor
--
--    simulator.py and analyze.py both DELETE the sensor's row and re-INSERT it,
--    so sensor_name is the natural primary key. Written by Python, read by
--    Main.loadPredictions().
CREATE TABLE predictions (
    sensor_name     VARCHAR(100) PRIMARY KEY,
    predicted_value DOUBLE       NOT NULL,
    anomaly_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
    anomaly_reason  VARCHAR(255) DEFAULT NULL,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP
);

-- 6. alerts — generated when a reading crosses a threshold
CREATE TABLE alerts (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sensor_name VARCHAR(100) DEFAULT NULL,
    message     VARCHAR(255) NOT NULL,
    timestamp   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 7. users — login accounts
--
--    NOTE: passwords are stored in plaintext. This is a known limitation and is
--    on the roadmap in the README. Do not reuse a real password here.
CREATE TABLE users (
    username VARCHAR(50)  PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- 8. Seed sensor readings, so the dashboard is not empty on first run
INSERT INTO sensors (name, location, value) VALUES
('Temperature Sensor', 'Server Room', 45.2),
('Humidity Sensor',    'Warehouse',   20.0),   -- deliberately low, triggers an alert
('Air Quality Sensor', 'Lab',         80.5);   -- deliberately high, triggers an alert

-- 9. Default login: admin / 1234
INSERT INTO users (username, password) VALUES ('admin', '1234');