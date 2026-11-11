-- -----------------------------------------------------
-- Database initialization for UserService
-- -----------------------------------------------------

CREATE DATABASE IF NOT EXISTS `user_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `user_db`;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `name` VARCHAR(255),
  `phone` VARCHAR(50),
  `role` ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id`)
);

-- Optional: insert sample data
INSERT INTO `users` (email, name, phone, role) VALUES
('user1@example.com', 'User One', '0711111111', 'USER'),
('user2@example.com', 'User Two', '0722222222', 'USER'),
('user3@example.com', 'User Three', '0733333333', 'ADMIN'),
('user4@example.com', 'User Four', '0744444444', 'USER'),
('user5@example.com', 'User Five', '0755555555', 'USER'),
('user6@example.com', 'User Six', '0766666666', 'USER'),
('user7@example.com', 'User Seven', '0777777777', 'USER'),
('user8@example.com', 'User Eight', '0788888888', 'USER'),
('user9@example.com', 'User Nine', '0799999999', 'ADMIN'),
('user10@example.com', 'User Ten', '0700000000', 'USER');
