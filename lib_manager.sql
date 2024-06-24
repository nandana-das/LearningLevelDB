CREATE DATABASE IF NOT EXISTS library_management;
USE library_management;
CREATE TABLE Books (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  genre VARCHAR(50),
  isbn VARCHAR(13),
  publish_year INT
);
CREATE TABLE Members (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone_number VARCHAR(20),
  joined_date DATE NOT NULL
);
CREATE TABLE Loans (
  id INT PRIMARY KEY AUTO_INCREMENT,
  book_id INT NOT NULL,
  member_id INT NOT NULL,
  loan_date DATE NOT NULL,
  return_date DATE,
  FOREIGN KEY (book_id) REFERENCES lib_data.Books(id),
  FOREIGN KEY (member_id) REFERENCES lib_data.Members(id)
);
INSERT INTO Books (title, author, genre, isbn, publish_year)
VALUES ("The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Science Fiction", "9780345391803", 1979),
       ("Pride and Prejudice", "Jane Austen", "Romance", "9780140439516", 1813),
       ("To Kill a Mockingbird", "Harper Lee", "Fiction", "9780446310727", 1960);
INSERT INTO Members (name, email, phone_number, joined_date)
VALUES ("John Doe", "john.doe@example.com", "123-456-7890", CURDATE()),
       ("Jane Smith", "jane.smith@email.com", NULL, DATE_SUB(CURDATE(), INTERVAL 2 MONTH));
INSERT INTO Loans (book_id, member_id, loan_date, return_date)
VALUES (1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 WEEK)),  -- Book 1 loaned to John Doe for 2 weeks
       (2, 2, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), NULL);     -- Book 2 loaned to Jane Smith 1 month ago (not returned yet)
