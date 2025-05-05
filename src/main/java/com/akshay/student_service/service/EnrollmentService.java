package com.akshay.student_service.service;

import com.akshay.student_service.model.Course;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.repository.CourseRepository;
import com.akshay.student_service.repository.EnrollmentRepository;
import com.akshay.student_service.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    @Value("${finance.service.url}")
    private String financeServiceUrl;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             StudentRepository studentRepository,
                             RestTemplate restTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
    }


    public Map<String, Object> enrollStudent(String studentId, String courseCode) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));

        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found with courseCode: " + courseCode));

        boolean alreadyEnrolled = enrollmentRepository.existsByStudentAndCourse(student, course);
        if (alreadyEnrolled) {
            throw new RuntimeException("Student already enrolled in this course");
        }


        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStudentIdString(studentId);
        enrollment.setCourseCode(courseCode);

        enrollment = enrollmentRepository.save(enrollment);

        String reference = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate dueDate = LocalDate.now().plusWeeks(2);

        Map<String, Object> invoicePayload = new HashMap<>();
        invoicePayload.put("reference", reference);
        invoicePayload.put("amount", course.getCourseFee());
        invoicePayload.put("dueDate", dueDate.toString());
        invoicePayload.put("type", "TUITION_FEES");

        Map<String, Object> account = new HashMap<>();
        account.put("studentId", studentId);
        invoicePayload.put("account", account);

        String financeEndpoint = financeServiceUrl + "/invoices/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(invoicePayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(financeEndpoint, requestEntity, String.class);

        Map<String, Object> result = new HashMap<>();
        result.put("enrollment", enrollment);
        result.put("invoiceReference", reference);
        result.put("financeResponseStatus", response.getStatusCodeValue());

        return result;
    }

    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        return enrollmentRepository.findByStudentIdString(studentId);
    }
    public String markCourseAsCompleted(String studentId, String courseCode) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdStringAndCourseCode(studentId, courseCode)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (enrollment.isCompleted()) {
            return "Course already marked as completed.";
        }

        enrollment.setCompleted(true);
        enrollmentRepository.save(enrollment);
        return "Course marked as completed successfully.";
    }

}
