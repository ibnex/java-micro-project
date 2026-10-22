CREATE DATABASE IF NOT EXISTS vehicle_parking;
USE vehicle_parking;

CREATE TABLE IF NOT EXISTS parking_slots (
    slot_id INT PRIMARY KEY,
    occupied BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS parking_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    registration_number VARCHAR(30) NOT NULL,
    vehicle_type VARCHAR(10) NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    fee INT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
);

INSERT IGNORE INTO parking_slots (slot_id, occupied) VALUES
    (1, FALSE), (2, FALSE), (3, FALSE), (4, FALSE), (5, FALSE);
