-- -----------------------------------------------------
-- Database initialization for AuthService
-- -----------------------------------------------------

CREATE DATABASE IF NOT EXISTS `auth_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `auth_db`;

CREATE TABLE IF NOT EXISTS `user_credentials` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(255) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`user_id`)
);

-- Insert default admin and user with pre-hashed BCrypt passwords
INSERT INTO `user_credentials` (`username`, `password`, `role`) VALUES
('admin', '$2a$10$K9WgV1Odtb06m9Bi44v5QO1G7sJ2mpE1pFjkqpdGsaKP/JuAO1/ay', 'ADMIN'),
('user',  '$2a$10$TuFrZ./f1o2uqDq4l4Bj9ePQzPb6YeEZLZIT2UUbZ3h4l44BBpo2W', 'USER');
