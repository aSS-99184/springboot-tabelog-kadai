CREATE TABLE IF NOT EXISTS restaurants(
	restaurant_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	image VARCHAR(255),
	description TEXT, 
	price_range VARCHAR(50),
	lunch_opening_time TIME,
	lunch_closing_time TIME,
	dinner_opening_time TIME,
	dinner_closing_time TIME,
	postal_code VARCHAR(50),
	address VARCHAR(255),
	phone_number VARCHAR(50),
	close_days VARCHAR(100),
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
	update_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);