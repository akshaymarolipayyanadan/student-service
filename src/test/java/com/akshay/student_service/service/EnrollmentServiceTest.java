package com.akshay.student_service.service;

import com.akshay.student_service.model.Course;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.repository.CourseRepository;
import com.akshay.student_service.repository.EnrollmentRepository;
import com.akshay.student_service.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setup() {
        student = new Student();
        student.setStudentId("c123456");

        course = new Course();
        course.setCourseCode("CS101");
        course.setCourseFee(BigDecimal.valueOf(1200.0));
    }


    @Test
    void testEnrollStudent_studentNotFound_throwsException() {
        when(studentRepository.findByStudentId("c404")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.enrollStudent("c404", "CS101");
        });

        assertEquals("Student not found with studentId: c404", exception.getMessage());
    }

    @Test
    void testEnrollStudent_courseNotFound_throwsException() {
        when(studentRepository.findByStudentId("c123456")).thenReturn(Optional.of(student));
        when(courseRepository.findByCourseCode("CS404")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.enrollStudent("c123456", "CS404");
        });

        assertEquals("Course not found with courseCode: CS404", exception.getMessage());
    }

    @Test
    void testEnrollStudent_alreadyEnrolled_throwsException() {
        when(studentRepository.findByStudentId("c123456")).thenReturn(Optional.of(student));
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.enrollStudent("c123456", "CS101");
        });

        assertEquals("Student already enrolled in this course", exception.getMessage());
    }
}
