-- -----------------------------------------------------------------------------
-- COMPLETE EnviroTrack Database Setup (complete_db_setup.sql)
--
-- This script creates the database and all required tables (sensors, alerts, users).
--
-- IMPORTANT: Run this script in your MySQL client (Workbench or CLI)
-- using a user with administrative rights (like 'root').
-- -----------------------------------------------------------------------------

-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS envirotrack_db;

-- 2. Select the new database
USE envirotrack_db;

-- 3. Drop tables if they already exist to ensure a clean start (optional)
DROP TABLE IF EXISTS sensors;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS users;

-- 4. Create the sensors table (stores sensor readings)
CREATE TABLE sensors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    value DOUBLE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Create the alerts table (stores generated alerts)
CREATE TABLE alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Create the users table (for login)
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL -- IMPORTANT: This stores plaintext passwords for demo only.
);

-- 7. Insert initial test data into sensors
INSERT INTO sensors (name, location, value) VALUES
('Temperature Sensor 1', 'Warehouse Floor', 45.2),
('Humidity Sensor A', 'Server Room', 20.0), -- Low value for alert test
('Air Quality Sensor C', 'Main Hall', 80.5); -- High value for alert test

-- 8. Insert the default user for login (admin/1234)
INSERT INTO users (username, password) VALUES ('admin', '1234');

-- 9. OPTIONAL: If you want to use the hardcoded 'enviro' user in DatabaseHelper.java,
--    you need to create it and grant privileges.
--    If you are using 'root' in the Java file, skip this step.
-- CREATE USER IF NOT EXISTS 'enviro'@'localhost' IDENTIFIED BY 'enviro123';
-- GRANT ALL PRIVILEGES ON envirotrack_db.* TO 'enviro'@'localhost';
-- FLUSH PRIVILEGES;
