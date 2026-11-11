CREATE DATABASE IF NOT EXISTS monitoring_db;

USE monitoring_db;

CREATE TABLE IF NOT EXISTS sensor_measurement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    measurement_value DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS hourly_consumption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    total_consumption DOUBLE NOT NULL
);
