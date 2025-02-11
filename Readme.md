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
git clone https://github.com/yourusername/federal-holidays-api.git
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
| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/holidays?countryCode=US&year=2025` | Get holidays for a country and year |
| `GET` | `/api/holidays/{id}` | Get details of a specific holiday by ID |
| `POST` | `/api/holidays` | Add a new holiday |
| `PUT` | `/api/holidays/{id}` | Update an existing holiday |
| `DELETE` | `/api/holidays/{id}` | Delete a holiday |
| `POST` | `/api/holidays/upload` | Upload a file containing holidays |
| `GET` | `/api/holidays/all` | Get all holidays |

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

## Code Coverage

![Screenshot 2025-02-11 021531.png](../../OneDrive/Pictures/Screenshots/Screenshot%202025-02-11%20021531.png)

## Author
**Tushar Sinha**


