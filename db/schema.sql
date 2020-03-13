
CREATE DATABASE IF NOT EXISTS rocks_db_test_db;

USE rocks_db_test_db;

CREATE TABLE customers(
    customer_id     BIGINT PRIMARY KEY,
    name            VARCHAR(50),
    address         VARCHAR(125),
    age             INT
);

CREATE TABLE products(
    product_id      BIGINT PRIMARY KEY,
    name            VARCHAR(75), 
    category        VARCHAR(50),
    price           DECIMAL(15,4),
    description     VARCHAR(1000),
    quantity        INT,
    posting_date    DATE 
);

CREATE TABLE orders(
    order_id        BIGINT PRIMARY KEY,
    customer_id     BIGINT,
    product_id      BIGINT,
    quantity        INT,
    purchase_time   TIMESTAMP 
);
