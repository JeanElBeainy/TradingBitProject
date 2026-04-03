CREATE TABLE users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(30)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    creation_date DATETIME     NULL,
    role          VARCHAR(25)  NOT NULL
);

CREATE TABLE balance
(
    id             BIGINT PRIMARY KEY,
    usd_balance    DECIMAL(30, 8) NOT NULL,
    crypto_balance DECIMAL(30, 8) NOT NULL,
    stock_balance  DECIMAL(30, 8) NOT NULL,
    total_balance  DECIMAL(30, 8) NOT NULL,
    total_volume   DECIMAL(30, 8) NOT NULL,
    CONSTRAINT balance_users_id_fk
        FOREIGN KEY (id) REFERENCES users (id)
);

CREATE TABLE portfolio
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_type VARCHAR(15)    NOT NULL,
    symbol        VARCHAR(10)    NOT NULL,
    quantity      DECIMAL(30, 8) NOT NULL,
    user_id       BIGINT         NOT NULL,
    name          VARCHAR(50)    NOT NULL
);

CREATE TABLE history
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_symbol   VARCHAR(10)    NOT NULL,
    from_name     VARCHAR(50)    NOT NULL,
    from_quantity DECIMAL(30, 8) NOT NULL,
    from_price    DECIMAL(15, 8) NOT NULL,
    to_symbol     VARCHAR(10)    NOT NULL,
    to_name       VARCHAR(50)    NOT NULL,
    to_quantity   DECIMAL(30, 8) NOT NULL,
    to_price      DECIMAL(15, 8) NOT NULL,
    user_id       BIGINT         NOT NULL,
    volume        DECIMAL(15, 8) NOT NULL,
    fee           DECIMAL(15, 8) NOT NULL,
    time          VARCHAR(30)    NULL
);
