SET NAMES utf8mb4;

DROP DATABASE IF EXISTS microtransaction_engine;
CREATE DATABASE microtransaction_engine
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE microtransaction_engine;


-- A player or admin account
CREATE TABLE `USER` (
    user_id          VARCHAR(20)    PRIMARY KEY,
    username         VARCHAR(50)    NOT NULL UNIQUE,
    password_hash    VARCHAR(64)    NOT NULL,
    role             ENUM('PLAYER','ADMIN') NOT NULL DEFAULT 'PLAYER',
    currency_balance DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_balance_not_negative CHECK (currency_balance >= 0)
) ENGINE=InnoDB;


-- Items for sale in the store
CREATE TABLE PRODUCT (
    product_id   VARCHAR(20)    PRIMARY KEY,
    name         VARCHAR(100)   NOT NULL,
    type         VARCHAR(30)    NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    description  VARCHAR(255),
    icon_symbol  VARCHAR(10),
    CONSTRAINT chk_price_not_negative CHECK (price >= 0)
) ENGINE=InnoDB;


-- Discount codes
CREATE TABLE PROMO_CODE (
    promo_code_id    INT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(30)    NOT NULL UNIQUE,
    discount_percent DECIMAL(5,2)   NOT NULL,
    expires_at       DATETIME       NOT NULL,
    max_uses         INT            NOT NULL DEFAULT 100,
    used_count       INT            NOT NULL DEFAULT 0
) ENGINE=InnoDB;


-- Every purchase attempt, successful or not.
-- base_price, discount_amount and final_amount are copied in at the
-- moment of sale, so changing a product price later never changes history.
CREATE TABLE PURCHASE (
    purchase_id     VARCHAR(40)    PRIMARY KEY,
    user_id         VARCHAR(20)    NOT NULL,
    product_id      VARCHAR(20)    NOT NULL,
    promo_code_id   INT            NULL,
    base_price      DECIMAL(10,2)  NOT NULL,
    discount_amount DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    final_amount    DECIMAL(10,2)  NOT NULL,
    status          ENUM('PENDING','VALIDATED','COMPLETED','FAILED','FLAGGED')
                        NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)       REFERENCES `USER`(user_id),
    FOREIGN KEY (product_id)    REFERENCES PRODUCT(product_id),
    FOREIGN KEY (promo_code_id) REFERENCES PROMO_CODE(promo_code_id)
) ENGINE=InnoDB;


-- Signed receipts from the simulated app store.
-- receipt_token is UNIQUE so the same receipt can never be used twice.
CREATE TABLE PAYMENT_RECEIPT (
    receipt_id     INT AUTO_INCREMENT PRIMARY KEY,
    receipt_token  VARCHAR(128)  NOT NULL UNIQUE,
    purchase_id    VARCHAR(40)   NOT NULL,
    signed_data    VARCHAR(64),
    validated      BOOLEAN       NOT NULL DEFAULT FALSE,
    validated_at   DATETIME      NULL,
    FOREIGN KEY (purchase_id) REFERENCES PURCHASE(purchase_id)
) ENGINE=InnoDB;


-- What each player owns. The UNIQUE rule makes the same item
-- stack into one row instead of creating duplicates.
CREATE TABLE INVENTORY (
    inventory_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id       VARCHAR(20)  NOT NULL,
    product_id    VARCHAR(20)  NOT NULL,
    quantity      INT          NOT NULL DEFAULT 1,
    granted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_product (user_id, product_id),
    FOREIGN KEY (user_id)    REFERENCES `USER`(user_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
) ENGINE=InnoDB;


-- A record of every purchase decision
CREATE TABLE AUDIT_LOG (
    audit_id     INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id  VARCHAR(40)   NOT NULL,
    metadata     VARCHAR(500),
    decision     VARCHAR(30)   NOT NULL,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (purchase_id) REFERENCES PURCHASE(purchase_id)
) ENGINE=InnoDB;


-- Suspicious activity flagged by the anti-cheat engine
CREATE TABLE FRAUD_LOG (
    fraud_id     INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id  VARCHAR(40)   NULL,
    user_id      VARCHAR(20)   NOT NULL,
    reason       VARCHAR(255)  NOT NULL,
    logged_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (purchase_id) REFERENCES PURCHASE(purchase_id),
    FOREIGN KEY (user_id)     REFERENCES `USER`(user_id)
) ENGINE=InnoDB;


-- ============================ SAMPLE DATA ============================

INSERT INTO `USER` (user_id, username, password_hash, role, currency_balance) VALUES
('2026100123', 'player1', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'PLAYER', 150.00),
('SUSPECT999', 'suspect', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'PLAYER', 500.00),
('ADMIN001',   'admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 1000.00);

INSERT INTO PRODUCT (product_id, name, type, price, description, icon_symbol) VALUES
('IAP101', '6,480 Virtual Crystals',  'Currency', 99.99, 'Premium currency bundle',           '💎'),
('IAP102', 'Cyberpunk Phantom Skin',  'Cosmetic', 24.99, 'Legendary character skin',          '🎭'),
('IAP103', 'Battle Pass (Season 8)',  'Pass',      9.99, 'Unlocks the Season 8 reward track', '⚡'),
('IAP104', '500 Points Starter Pack', 'Currency',  4.99, 'Starter currency pack',             '🪙');

INSERT INTO PROMO_CODE (code, discount_percent, expires_at, max_uses) VALUES
('WELCOME10', 10.00, '2027-01-01 00:00:00', 500);