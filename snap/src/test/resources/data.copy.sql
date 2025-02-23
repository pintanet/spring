-- Inserimento dati nella tabella di configurazione
INSERT INTO table_config (source_schema, source_table, destination_schema, destination_table) VALUES
('PUBLIC', 'source_table_1', 'PUBLIC', 'destination_table_1'),
('PUBLIC', 'source_table_2', 'PUBLIC', 'destination_table_2'),
('PUBLIC', 'source_table_3', 'PUBLIC', 'destination_table_3'),
('PUBLIC', 'source_table_4', 'PUBLIC', 'destination_table_4');

-- Inserimento dati nella tabella sorgente 1
INSERT INTO source_table_1 (name, valore) VALUES
('Record 1', 10),
('Record 2', 20),
('Record 3', 30);

-- Inserimento dati nella tabella sorgente 2
INSERT INTO source_table_2 (product_name, price) VALUES
('Laptop', 1200.50),
('Mouse', 25.99),
('Keyboard', 79.95);

-- Inserimento dati nella tabella sorgente 3
INSERT INTO source_table_3 (customer_name, city) VALUES
('John Doe', 'New York'),
('Jane Smith', 'London'),
('David Lee', 'Paris');

-- Inserimento dati nella tabella sorgente 4
INSERT INTO source_table_4 (order_date, quantity) VALUES
('2023-10-26', 5),
('2023-10-27', 10),
('2023-10-28', 3);