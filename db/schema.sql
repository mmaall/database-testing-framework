
CREATE DATABASE IF NOT EXISTS orders_db;

USE orders_db;

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
    purchase_time   TIMESTAMP 
);

CREATE TABLE orders_products(
    order_id        BIGINT PRIMARY KEY,
    product_id      BIGINT,
    quantity        INT
);

CREATE INDEX customer_index ON orders (customer_id, order_id); 