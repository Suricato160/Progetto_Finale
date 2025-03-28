-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema eCommerce
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema eCommerce
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `eCommerce` DEFAULT CHARACTER SET utf8 ;
USE `eCommerce` ;

-- -----------------------------------------------------
-- Table `eCommerce`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eCommerce`.`users` (
  `id` INT NOT NULL,
  `username` VARCHAR(50) NULL,
  `password` VARCHAR(100) NULL,
  `email` VARCHAR(100) NULL,
  `role` VARCHAR(25) NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eCommerce`.`categories`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eCommerce`.`categories` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eCommerce`.`products`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eCommerce`.`products` (
  `id` INT NOT NULL,
  `name` VARCHAR(100) NULL,
  `desription` TEXT(300) NULL,
  `price` DECIMAL(10,2) NULL,
  `stock` INT NULL,
  `image_url` VARCHAR(255) NULL,
  `categories_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_products_categories1_idx` (`categories_id` ASC) VISIBLE,
  CONSTRAINT `fk_products_categories1`
    FOREIGN KEY (`categories_id`)
    REFERENCES `eCommerce`.`categories` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eCommerce`.`orders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eCommerce`.`orders` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `order_date` DATETIME NULL,
  `total_amount` DECIMAL(10,2) NULL,
  `status` ENUM('PENDING', 'SHIPPED', 'DELIVERED') NULL DEFAULT 'PENDING',
  `users_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_orders_users_idx` (`users_id` ASC) VISIBLE,
  CONSTRAINT `fk_orders_users`
    FOREIGN KEY (`users_id`)
    REFERENCES `eCommerce`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eCommerce`.`order_details`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eCommerce`.`order_details` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `quantity` INT NOT NULL,
  `orders_id` INT NOT NULL,
  `products_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_order_details_orders1_idx` (`orders_id` ASC) VISIBLE,
  INDEX `fk_order_details_products1_idx` (`products_id` ASC) VISIBLE,
  CONSTRAINT `fk_order_details_orders1`
    FOREIGN KEY (`orders_id`)
    REFERENCES `eCommerce`.`orders` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_order_details_products1`
    FOREIGN KEY (`products_id`)
    REFERENCES `eCommerce`.`products` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;



USE eCommerce;


INSERT INTO `eCommerce`.`users` (`id`, `username`, `password`, `email`, `role`) VALUES
(1, 'admin', 'admin123', 'admin@example.com', 'ADMIN'),
(2, 'john_doe', 'password123', 'john.doe@example.com', 'USER')

INSERT INTO `eCommerce`.`categories` (`id`, `name`) VALUES
(1, 'Chitarre Elettriche'),
(2, 'Chitarre Acustiche'),
(3, 'Chitarre Classiche'),
(4, 'Amplificatori'),
(5, 'Accessori');

-- Oppure, senza specificare gli id (se AUTO_INCREMENT è attivo):
INSERT INTO `eCommerce`.`categories` (`name`) VALUES
('Chitarre Elettriche'),
('Chitarre Acustiche'),
('Chitarre Classiche'),
('Amplificatori'),
('Accessori');



INSERT INTO `eCommerce`.`products` (`id`, `name`, `description`, `price`, `stock`, `image_url`, `categories_id`) VALUES
(1, 'Fender Stratocaster', 'Chitarra elettrica classica', 1299.99, 5, 'https://example.com/fender.jpg', 1),
(2, 'Gibson Les Paul', 'Chitarra iconica per rock', 1999.99, 3, 'https://example.com/gibson.jpg', 1),
(3, 'Taylor 214ce', 'Chitarra acustica di qualità', 999.99, 10, 'https://example.com/taylor.jpg', 2);

USE eCommerce;
INSERT INTO `eCommerce`.`products` (`id`, `name`, `description`, `price`, `stock`, `image_url`, `categories_id`) VALUES
(1, 'Fender Stratocaster', 'Chitarra elettrica classica', 1299.99, 5, 'https://thumbs.static-thomann.de/thumb//orig/pics/prod/608524.webp', 1),
(2, 'Gibson Les Paul', 'Chitarra iconica per rock', 1999.99, 3, 'https://thumbs.static-thomann.de/thumb/padthumb600x600/pics/prod/601372.jpg', 1),
(3, 'Taylor 214ce', 'Chitarra acustica di qualità', 999.99, 10, 'https://thumbs.static-thomann.de/thumb/padthumb600x600/pics/bdb/_48/483303/14969165_800.jpg', 2);


USE eCommerce;
INSERT INTO `eCommerce`.`categories` (`id`,`name`) VALUES
('1','Chitarre Elettriche'),
('2','Chitarre Acustiche'),
('3','Chitarre Classiche'),
('4','Amplificatori'),
('5','Accessori');

INSERT INTO `eCommerce`.`products` (`id`, `name`, `description`, `price`, `stock`, `image_url`, `categories_id`) VALUES
(1, 'Fender Stratocaster', 'Chitarra elettrica classica', 1299.99, 5, 'https://thumbs.static-thomann.de/thumb//orig/pics/prod/608524.webp', 1),
(2, 'Gibson Les Paul', 'Chitarra iconica per rock', 1999.99, 3, 'https://thumbs.static-thomann.de/thumb/padthumb600x600/pics/prod/601372.jpg', 1),
(3, 'Taylor 214ce', 'Chitarra acustica di qualità', 999.99, 10, 'https://thumbs.static-thomann.de/thumb/padthumb600x600/pics/bdb/_48/483303/14969165_800.jpg', 2);