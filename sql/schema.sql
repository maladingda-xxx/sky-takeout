CREATE DATABASE IF NOT EXISTS sky_takeout
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE sky_takeout;

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    sex TINYINT NOT NULL DEFAULT 1,
    id_number VARCHAR(18) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_username (username),
    UNIQUE KEY uk_employee_id_number (id_number),
    KEY idx_employee_name (name),
    KEY idx_employee_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO employee (
    id,
    name,
    username,
    password,
    phone,
    sex,
    id_number,
    status,
    create_user,
    update_user
) VALUES
    (1, 'Admin', 'admin', '$2a$10$ruVkHLSTd394.6B77k7UrOJSPZsqJHQ5llLFgM48qpiHkMdDvSU7u', '13800000001', 1, '110101199001010011', 1, 1, 1),
    (2, 'Operator', 'operator', '$2a$10$ruVkHLSTd394.6B77k7UrOJSPZsqJHQ5llLFgM48qpiHkMdDvSU7u', '13800000002', 2, '110101199002020022', 1, 1, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    password = VALUES(password),
    phone = VALUES(phone),
    sex = VALUES(sex),
    status = VALUES(status),
    update_user = VALUES(update_user);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    type TINYINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_type_name (type, name),
    KEY idx_category_type_sort (type, sort),
    KEY idx_category_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO category (
    id,
    type,
    name,
    sort,
    status,
    create_user,
    update_user
) VALUES
    (1, 1, 'Hot Dishes', 10, 1, 1, 1),
    (2, 2, 'Set Meals', 10, 1, 1, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort = VALUES(sort),
    status = 1,
    update_user = 1;

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dish_category_id (category_id),
    KEY idx_dish_name (name),
    KEY idx_dish_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    dish_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(32) NOT NULL,
    value_json VARCHAR(1000) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dish_flavor_dish_id (dish_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    description VARCHAR(255) DEFAULT NULL,
    image VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setmeal_category_name (category_id, name),
    KEY idx_setmeal_category_id (category_id),
    KEY idx_setmeal_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    setmeal_id BIGINT UNSIGNED NOT NULL,
    dish_id BIGINT UNSIGNED NOT NULL,
    copies INT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setmeal_dish (setmeal_id, dish_id),
    KEY idx_setmeal_dish_dish_id (dish_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    openid VARCHAR(64) NOT NULL,
    name VARCHAR(32) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    sex TINYINT DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_openid (openid)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    dish_id BIGINT UNSIGNED DEFAULT NULL,
    setmeal_id BIGINT UNSIGNED DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 1,
    flavor VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shopping_cart_user_dish (user_id, dish_id),
    UNIQUE KEY uk_shopping_cart_user_setmeal (user_id, setmeal_id),
    KEY idx_shopping_cart_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS address_book (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    consignee VARCHAR(32) NOT NULL,
    sex TINYINT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province_name VARCHAR(32) NOT NULL,
    city_name VARCHAR(32) NOT NULL,
    district_name VARCHAR(32) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    label VARCHAR(32) DEFAULT NULL,
    is_default TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_address_book_user_id (user_id),
    KEY idx_address_book_user_default (user_id, is_default)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    number VARCHAR(32) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    user_id BIGINT UNSIGNED NOT NULL,
    address_book_id BIGINT UNSIGNED NOT NULL,
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checkout_time DATETIME DEFAULT NULL,
    pay_method TINYINT NOT NULL,
    pay_status TINYINT NOT NULL DEFAULT 0,
    amount DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    consignee VARCHAR(32) NOT NULL,
    cancel_reason VARCHAR(255) DEFAULT NULL,
    cancel_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_number (number),
    KEY idx_orders_user_id (user_id),
    KEY idx_orders_status (status),
    KEY idx_orders_order_time (order_time)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(32) NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    dish_id BIGINT UNSIGNED DEFAULT NULL,
    setmeal_id BIGINT UNSIGNED DEFAULT NULL,
    dish_flavor VARCHAR(255) DEFAULT NULL,
    number INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_detail_order_id (order_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
