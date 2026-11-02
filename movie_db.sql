DROP DATABASE IF EXISTS movie_db;
CREATE DATABASE movie_db;
USE movie_db;

-- movies and ticket price
CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(50),
    price DOUBLE
);

-- shows (time slots) of each movie with seat count
CREATE TABLE shows (
    show_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT,
    show_time VARCHAR(20),
    total_seats INT,
    available_seats INT,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

-- tickets booked by customers
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(50),
    show_id INT,
    seats INT,
    total_amount DOUBLE,
    FOREIGN KEY (show_id) REFERENCES shows(show_id)
);

INSERT INTO movies (title, price) VALUES
('Leo', 150),
('Jawan', 200),
('Kantara', 180),
('Salaar', 220);

-- every show has 50 seats
INSERT INTO shows (movie_id, show_time, total_seats, available_seats) VALUES
(1, '10:00 AM', 50, 50),
(1, '1:30 PM', 50, 50),
(1, '6:00 PM', 50, 50),
(2, '11:00 AM', 50, 50),
(2, '2:30 PM', 50, 50),
(2, '7:00 PM', 50, 50),
(3, '9:30 AM', 50, 50),
(3, '4:00 PM', 50, 50),
(3, '8:00 PM', 50, 50),
(4, '10:30 AM', 50, 50),
(4, '2:00 PM', 50, 50),
(4, '9:30 PM', 50, 50);
