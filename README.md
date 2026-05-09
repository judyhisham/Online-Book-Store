# Online Book Store

Spring Boot web app for a small online bookstore with catalog browsing,
cart, checkout, and admin management.

## Requirements

- Java 17 (JDK)
- Maven 3.8+
- MySQL 8+
- Eclipse IDE (Java Developers or Enterprise)

## Quick Start (Eclipse)

1) Import the project
	- File -> Import... -> Maven -> Existing Maven Projects
	- Select the project root (the folder with `pom.xml`)
	- Finish, then wait for Maven to download dependencies

2) Set up the database
	- Create a MySQL database named `bookstore_db`
	- Run the schema file: `sql/bookstore_schema.sql`

3) Update database credentials
	- Edit `src/main/resources/application.properties`
	- Update `spring.datasource.username` and `spring.datasource.password`

4) Run the application
	- In Eclipse, right-click the project
	- Run As -> Spring Boot App (or Java Application)

## Default Accounts (from schema)

- Admin login: `admin` / `admin123`
- User login: `testuser` / `password123`

## URLs

- Home / catalog: http://localhost:8080/
- User login: http://localhost:8080/login
- Admin login: http://localhost:8080/admin/login

## Project Structure

- `src/main/java/com/example` - Spring Boot application and packages
- `controller` - Web controllers (routes and views)
- `service` - Business logic
- `repository` - Data access (Spring Data JPA)
- `model` - JPA entities
- `util` - Utility classes (validation, password hashing, constants)
- `src/main/resources/templates` - Thymeleaf HTML templates
- `src/main/resources/static` - CSS and JavaScript
- `sql/bookstore_schema.sql` - Database schema + seed data

## Common Issues

- If startup fails with missing tables, re-run `sql/bookstore_schema.sql` and
	verify `spring.jpa.hibernate.ddl-auto=validate` in `application.properties`.
- If MySQL connection fails, confirm host, port, username, and password.