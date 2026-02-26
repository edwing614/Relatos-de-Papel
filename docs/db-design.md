# Database Design - Books Microservices

## Overview

This microservices architecture uses **database per service** pattern. Each microservice has its own dedicated database.

## MS-BOOKS-CATALOGUE Database

### Books Table

| Column           | Type         | Constraints                    | Description                      |
|------------------|--------------|--------------------------------|----------------------------------|
| id               | BIGINT       | PK, AUTO_INCREMENT             | Unique identifier                |
| title            | VARCHAR(255) | NOT NULL                       | Book title                       |
| author           | VARCHAR(255) | NOT NULL                       | Author name                      |
| publication_date | DATE         | NULLABLE                       | Publication date                 |
| category         | VARCHAR(100) | NULLABLE                       | Book category/genre              |
| isbn             | VARCHAR(20)  | NOT NULL, UNIQUE               | ISBN number                      |
| rating           | INT          | CHECK (1-5)                    | Rating from 1 to 5               |
| visible          | BOOLEAN      | DEFAULT TRUE                   | Visibility flag for public API   |
| stock            | INT          | DEFAULT 0, CHECK (>= 0)        | Available stock quantity         |
| created_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP      | Record creation timestamp        |
| updated_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP      | Last update timestamp            |

### Indexes
- `idx_books_isbn` - Fast lookup by ISBN
- `idx_books_category` - Category filtering
- `idx_books_author` - Author search
- `idx_books_visible` - Public/private filtering

---

## MS-BOOKS-PAYMENTS Database

### Purchases Table

| Column         | Type          | Constraints                | Description                          |
|----------------|---------------|----------------------------|--------------------------------------|
| id             | BIGINT        | PK, AUTO_INCREMENT         | Unique identifier                    |
| book_id        | BIGINT        | NOT NULL                   | Reference to book in catalogue       |
| book_isbn      | VARCHAR(20)   | NULLABLE                   | Denormalized ISBN for reference      |
| book_title     | VARCHAR(255)  | NULLABLE                   | Denormalized title for reference     |
| quantity       | INT           | NOT NULL, CHECK (> 0)      | Quantity purchased                   |
| unit_price     | DECIMAL(10,2) | NULLABLE                   | Price per unit at purchase time      |
| total_amount   | DECIMAL(10,2) | NULLABLE                   | Total purchase amount                |
| status         | VARCHAR(20)   | NOT NULL, DEFAULT 'PENDING'| SUCCESS, FAILED, PENDING             |
| failure_reason | VARCHAR(500)  | NULLABLE                   | Reason if purchase failed            |
| created_at     | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP  | Purchase timestamp                   |
| updated_at     | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP  | Last update timestamp                |

### Indexes
- `idx_purchases_book_id` - Lookup by book
- `idx_purchases_status` - Status filtering
- `idx_purchases_created_at` - Time-based queries

---

## Communication Flow

```
[Client] -> [API Gateway] -> [Eureka Discovery] -> [Microservices]

POST /payments:
1. ms-books-payments receives request
2. Calls ms-books-catalogue via Eureka (by service name)
3. Validates: book exists, visible=true, stock >= quantity
4. If valid: decrements stock, saves SUCCESS purchase
5. If invalid: saves FAILED purchase with reason
```

## Profiles

- **local**: H2 in-memory database
- **prod**: PostgreSQL/MySQL (configure connection in application-prod.yml)
