package com.akshay.student_service.controller;

import com.akshay.student_service.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegisterStudent() {
        // Prepare request
        Student student = new Student();
        student.setFirstName("Akshay");
        student.setLastName("MP");
        student.setUsername("akshay123");
        student.setEmail("akshay@example.com");
        student.setPassword("secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Student> request = new HttpEntity<>(student, headers);

        // Make POST request
        ResponseEntity<Student> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/students/register",
                request,
                Student.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStudentId()).startsWith("c");
    }
    @Test
    void testGetStudentById_success() {
        // Register a student
        Student student = new Student();
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setUsername("testuser_" + System.currentTimeMillis());
        student.setEmail("test@example.com");
        student.setPassword("pass");

        ResponseEntity<Student> registerResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/students/register",
                new HttpEntity<>(student),
                Student.class
        );

        Long id = registerResp.getBody().getId();
        ResponseEntity<Student> getResp = restTemplate.getForEntity(
                "http://localhost:" + port + "/students/" + id,
                Student.class
        );

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getUsername()).isEqualTo(student.getUsername());
    }

    @Test
    void testLoginStudent_wrongPassword_shouldFail() {
        // Register a student
        Student student = new Student();
        String uniqueUsername = "wrongpass_" + System.currentTimeMillis();
        student.setFirstName("Fail");
        student.setLastName("Login");
        student.setUsername(uniqueUsername);
        student.setEmail("fail@example.com");
        student.setPassword("correctpassword");

        restTemplate.postForEntity(
                "http://localhost:" + port + "/students/register",
                new HttpEntity<>(student),
                Student.class
        );

        // Try login with incorrect password
        Map<String, String> loginMap = new HashMap<>();
        loginMap.put("username", uniqueUsername);
        loginMap.put("password", "wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginMap, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/students/login",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid username or password");

    }

    @Test
    void testGetStudentProfile_success() {
        // Register student
        Student student = new Student();
        student.setFirstName("Profile");
        student.setLastName("User");
        student.setUsername("profile_" + System.currentTimeMillis());
        student.setEmail("profile@example.com");
        student.setPassword("profilepass");

        ResponseEntity<Student> registerResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/students/register",
                new HttpEntity<>(student),
                Student.class
        );

        String studentId = registerResp.getBody().getStudentId();

        // Request profile
        ResponseEntity<String> profileResp = restTemplate.getForEntity(
                "http://localhost:" + port + "/students/profile/" + studentId,
                String.class
        );

        assertThat(profileResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileResp.getBody()).contains("Profile"); // Check firstName
        assertThat(profileResp.getBody()).contains(studentId); // Check studentId presence
    }







}
