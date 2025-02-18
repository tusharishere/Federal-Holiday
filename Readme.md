# Federal Holidays API

A RESTful API to manage Federal Holidays for the USA ,UK and Canada. This API allows users to add, update, list, and upload holidays via a file.

## Features
- Add and update federal holidays.
- Retrieve holidays by country and year.
- Upload a file containing holiday data.
- Easily extensible architecture.
- Includes Postman collection and Swagger documentation.
- 70%+ test coverage with clean code practices.

## Technologies Used
- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **MySQL** (configurable)
- **Swagger** (API Documentation)
- **JUnit & Mockito** (for testing)

## Installation
### Prerequisites
- Java 17 or later

### Clone the Repository
```sh
git clone https://github.com/tusharishere/Federal-Holiday?tab=readme-ov-file
cd federal-holidays
```

### Run Locally
1. Update `application.properties` for your database configuration.
2. Build and run the application:
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints
| Method | Endpoint                                    | Description                                                              |
|--------|---------------------------------------------|--------------------------------------------------------------------------|
| `GET` | `/api/v1/holidays/{countryCode}`            | Get holidays for specific country using country code                     |
| `GET` | `/api/v1/holidays/by-country-date`          | Get details of a specific holiday by using country code and holiday date |
| `POST` | `/api/v1/holidays`                          | Add a new holiday                                                        |
| `PUT` | `/api/v1/holidays/update`                   | Update an existing holiday                                               |
| `DELETE` | `/api/v1/holidays/by-country-date`          | Delete a holiday using country code and holiday date                     |
| `POST` | `/api/v1/holidays/upload-csvs`              | Upload a csv file containing holidays                                    |
| `GET` | `/api/v1/holidays`                          | Get all holidays                                                         |
| `DELETE` | `/api/v1/holidays/by-country/{countryCode}` | Delete a holidays of a country using country code       |


## Testing
Run tests using:
```sh
mvn test
```

## Documentation
Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```
**CSV Format:**
```
country_code,holiday_name,holiday_date
001,Thanksgiving,25-Jan-2025
```
## Sample Data CSV
You can download the sample CSV file to test the API:
[Download Sample Holiday Data](https://drive.google.com/drive/folders/1jXShb2_zT-jgghP_CarNV-TGOkIPGXwu?usp=sharing)
## Postman Collection
You can test the API using the Postman collection:
[Holiday API Postman Collection](https://www.postman.com/supply-architect-71547258/federal-holiday/collection/nn00fvm/federal-holiday-apis?action=share&creator=38942471)


## Author
**Tushar Sinha**


