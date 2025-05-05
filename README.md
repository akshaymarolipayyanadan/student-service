# Student Service 🧑‍🎓

This is a Spring Boot microservice that handles student registration, login, profile management, course enrollment, and graduation status. It interacts with **Finance** and **Library** microservices using REST APIs.

---

## 🚀 Features

- Student Registration & Login
- Enrollment in courses
- Graduation eligibility check
- Integration with Finance and Library microservices
- Unit and Integration testing
- Dockerized setup

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- MySQL (or H2 for dev)
- RestTemplate (for microservice calls)
- JUnit & Mockito
- Docker

---

## 🐳 Docker Instructions

1. **Build Docker Image:**


docker build -t student-service .

Run Container:
docker run -p 8080:8080 student-service

Testing

    Unit Tests for services and repository logic

    Integration Tests using @SpringBootTest and TestRestTemplate

Run tests with:

./mvnw test

📂 Folder Structure

student-service/
├── src/
│   ├── main/java/com/akshay/student_service/
│   ├── test/java/com/akshay/student_service/
├── Dockerfile
├── pom.xml
└── README.md

📬 API Endpoints

    POST /students/register – Register a new student

    POST /students/login – Student login

    GET /students/profile/{studentId} – Get student profile

    POST /enrollments/enroll – Enroll in a course

    GET /students/graduation-status/{studentId} – Check graduation status

👤 Author

Akshay Maroli Payyanadan
