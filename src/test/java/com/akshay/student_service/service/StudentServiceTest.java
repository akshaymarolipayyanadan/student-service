package com.akshay.student_service.service;

import com.akshay.student_service.dto.GraduationStatusDTO;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.repository.EnrollmentRepository;
import com.akshay.student_service.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private StudentService studentService;

    @Test
    void testLoginStudent_success() {
        Student s = new Student();
        s.setUsername("akshay");
        s.setPassword("encoded-password");

        when(studentRepository.findByUsername("akshay")).thenReturn(s);
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        Student result = studentService.loginStudent("akshay", "raw-password");

        assertEquals("akshay", result.getUsername());
        System.out.println("✅ testLoginStudent_success passed. Logged in student: " + result.getUsername());
    }

    @Test
    void testLoginStudent_failure_wrongPassword() {
        Student s = new Student();
        s.setUsername("akshay");
        s.setPassword("encoded-password");

        when(studentRepository.findByUsername("akshay")).thenReturn(s);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            studentService.loginStudent("akshay", "wrong-password");
        });
        System.out.println("❌ testLoginStudent_failure_wrongPassword triggered expected exception.");
    }

    @Test
    void testLoginStudent_failure_userNotFound() {
        when(studentRepository.findByUsername("akshay")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            studentService.loginStudent("akshay", "any-password");
        });
        System.out.println("❌ testLoginStudent_failure_userNotFound triggered expected exception.");
    }

    @Test
    void testRegisterStudent_success() {
        Student s = new Student();
        s.setFirstName("Akshay");
        s.setLastName("MP");
        s.setUsername("akshay");
        s.setEmail("akshay@example.com");
        s.setPassword("raw-password");

        // Manually inject the @Value properties
        ReflectionTestUtils.setField(studentService, "financeServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(studentService, "libraryServiceUrl", "http://localhost:80");

        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.postForEntity(contains("/accounts"), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Finance OK"));

        when(restTemplate.postForEntity(contains("/api/register"), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Library OK"));

        Student result = studentService.registerStudent(s);

        assertEquals("akshay", result.getUsername());
        assertEquals("encoded-password", result.getPassword());

        System.out.println("✅ testRegisterStudent_success passed.");
    }

    @Test
    void testRegisterStudent_financeServiceFails() {
        Student s = new Student();
        s.setFirstName("Fail");
        s.setLastName("Finance");
        s.setUsername("failuser");
        s.setEmail("fail@fin.com");
        s.setPassword("pw");

        ReflectionTestUtils.setField(studentService, "financeServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(studentService, "libraryServiceUrl", "http://localhost:80");

        when(passwordEncoder.encode("pw")).thenReturn("encodedPw");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simulate finance service failure
        doThrow(new RuntimeException("Finance Service Down")).when(restTemplate)
                .postForEntity(contains("/accounts"), any(), eq(String.class));

        when(restTemplate.postForEntity(contains("/api/register"), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Library OK"));

        Student result = studentService.registerStudent(s);

        assertEquals("failuser", result.getUsername());
        System.out.println("⚠️ testRegisterStudent_financeServiceFails passed — handled finance failure gracefully.");
    }

    @Test
    void testCheckGraduationEligibility_noOutstandingInvoices() {
        Student s = new Student();
        s.setStudentId("c123456");
        when(studentRepository.findByStudentId("c123456")).thenReturn(Optional.of(s));

        Map<String, Object> financeMock = new HashMap<>();
        financeMock.put("hasOutstandingInvoices", false);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(financeMock);

        Map<String, Object> result = studentService.checkGraduationEligibility("c123456");

        assertTrue((Boolean) result.get("eligible"));
        assertEquals("Student is clear for graduation.", result.get("reason"));
        System.out.println("✅ testCheckGraduationEligibility_noOutstandingInvoices passed.");
    }

    @Test
    void testCheckGraduationEligibility_withOutstandingInvoices() {
        Student s = new Student();
        s.setStudentId("c123456");
        when(studentRepository.findByStudentId("c123456")).thenReturn(Optional.of(s));

        Map<String, Object> financeMock = new HashMap<>();
        financeMock.put("hasOutstandingInvoices", true);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(financeMock);

        Map<String, Object> result = studentService.checkGraduationEligibility("c123456");

        assertFalse((Boolean) result.get("eligible"));
        assertEquals("Student has outstanding invoices.", result.get("reason"));
        System.out.println("✅ testCheckGraduationEligibility_withOutstandingInvoices passed.");
    }

    @Test
    void testCheckGraduationEligibility_restTemplateFailure() {
        Student s = new Student();
        s.setStudentId("c123456");
        when(studentRepository.findByStudentId("c123456"))
                .thenReturn(Optional.of(s));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Finance service error"));

        Map<String, Object> result = studentService.checkGraduationEligibility("c123456");

        assertFalse((Boolean) result.get("eligible"));
        assertTrue(((String) result.get("reason")).contains("Could not verify"));
        System.out.println("✅ testCheckGraduationEligibility_restTemplateFailure passed.");
    }

    @Test
    void testGetGraduationStatus_success_noOutstandingBalance() {
        String studentId = "c123456";
        Student student = new Student();
        student.setStudentId(studentId);

        Enrollment e1 = new Enrollment();
        e1.setCourseCode("CS101");
        e1.setCompleted(true);

        Enrollment e2 = new Enrollment();
        e2.setCourseCode("CS102");
        e2.setCompleted(false);

        when(studentRepository.findByStudentId(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdString(studentId)).thenReturn(List.of(e1, e2));

        Map<String, Object> financeResponse = new HashMap<>();
        financeResponse.put("hasOutstandingBalance", false);

        when(restTemplate.getForObject(contains(studentId), eq(Map.class))).thenReturn(financeResponse);

        GraduationStatusDTO result = studentService.getGraduationStatus(studentId);

        assertEquals(1, result.getCompletedCourses().size());
        assertEquals(1, result.getPendingCourses().size());
        assertFalse(result.isHasOutstandingInvoices());
        System.out.println("✅ testGetGraduationStatus_success_noOutstandingBalance passed.");
    }

    @Test
    void testGetGraduationStatus_success_withOutstandingBalance() {
        String studentId = "c123456";
        Student student = new Student();
        student.setStudentId(studentId);

        Enrollment e1 = new Enrollment();
        e1.setCourseCode("CS101");
        e1.setCompleted(true);

        when(studentRepository.findByStudentId(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdString(studentId)).thenReturn(List.of(e1));

        Map<String, Object> financeResponse = new HashMap<>();
        financeResponse.put("hasOutstandingBalance", true);

        when(restTemplate.getForObject(contains(studentId), eq(Map.class))).thenReturn(financeResponse);

        GraduationStatusDTO result = studentService.getGraduationStatus(studentId);

        assertEquals(1, result.getCompletedCourses().size());
        assertTrue(result.isHasOutstandingInvoices());
        System.out.println("✅ testGetGraduationStatus_success_withOutstandingBalance passed.");
    }

    @Test
    void testGetGraduationStatus_financeServiceFails() {
        String studentId = "c123456";
        Student student = new Student();
        student.setStudentId(studentId);

        Enrollment e1 = new Enrollment();
        e1.setCourseCode("CS101");
        e1.setCompleted(true);

        when(studentRepository.findByStudentId(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdString(studentId)).thenReturn(List.of(e1));

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Finance MS down"));

        GraduationStatusDTO result = studentService.getGraduationStatus(studentId);

        assertEquals(1, result.getCompletedCourses().size());
        assertTrue(result.getPendingCourses().isEmpty());
        assertFalse(result.isHasOutstandingInvoices()); // Defaults to false if exception
        System.out.println("✅ testGetGraduationStatus_financeServiceFails handled gracefully.");
    }



}
