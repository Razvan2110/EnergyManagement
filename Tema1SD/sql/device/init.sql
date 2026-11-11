-- -----------------------------------------------------
-- Database initialization for DeviceService
-- -----------------------------------------------------

CREATE DATABASE IF NOT EXISTS `device_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `device_db`;

CREATE TABLE IF NOT EXISTS `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `power` INT NOT NULL,
  PRIMARY KEY (`id`)
);

-- Optional: insert sample data
INSERT INTO `device` (user_id, name, power) VALUES
(1, 'Smart Plug', 15),
(2, 'Heater', 2000),
(3, 'Lamp', 60),
(1, 'Fan', 75),
(1, 'Air Conditioner', 1800),
(1, 'TV', 150),
(1, 'Computer', 500),
(1, 'Phone Charger', 25),
(1, 'Microwave', 1200),
(1, 'Toaster', 800),
(2, 'Vacuum Cleaner', 1400),
(2, 'Fridge', 700),
(2, 'Washing Machine', 1600),
(2, 'Dryer', 1500),
(2, 'Dishwasher', 1300),
(2, 'Coffee Maker', 900),
(2, 'Iron', 1000),
(2, 'Speaker', 200),
(2, 'Projector', 300),
(2, 'Router', 50),
(3, 'Monitor', 100),
(4, 'Printer', 400),
(4, 'Blender', 500),
(4, 'Kettle', 1500),
(4, 'Hair Dryer', 1600),
(4, 'Gaming Console', 180),
(3, 'Smart Light', 10),
(3, 'Heated Blanket', 150),
(3, 'Security Camera', 20),
(3, 'Smart Thermostat', 12);
