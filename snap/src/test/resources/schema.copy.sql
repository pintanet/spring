DROP TABLE source_table_1;
DROP TABLE destination_table_1;
DROP TABLE source_table_2;
DROP TABLE destination_table_2;
DROP TABLE source_table_3;
DROP TABLE destination_table_3;
DROP TABLE source_table_4;
DROP TABLE destination_table_4;

-- Tabella di configurazione
CREATE TABLE table_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    source_schema VARCHAR(255) NOT NULL,
    source_table VARCHAR(255) NOT NULL,
    destination_schema VARCHAR(255) NOT NULL,
    destination_table VARCHAR(255) NOT NULL
);

-- Coppia di tabelle 1
CREATE TABLE source_table_1 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    valore INT
);

CREATE TABLE destination_table_1 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    valore INT,
    job_id INT
);

-- Coppia di tabelle 2
CREATE TABLE source_table_2 (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255),
    price DECIMAL(10, 2)
);

CREATE TABLE destination_table_2 (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255),
    price DECIMAL(10, 2),
    job_id INT
);

-- Coppia di tabelle 3
CREATE TABLE source_table_3 (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255),
    city VARCHAR(255)
);

CREATE TABLE destination_table_3 (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255),
    city VARCHAR(255),
    job_id INT
);

-- Coppia di tabelle 4
CREATE TABLE source_table_4 (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_date DATE,
    quantity INT
);

CREATE TABLE destination_table_4 (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_date DATE,
    quantity INT,
    job_id INT
);

