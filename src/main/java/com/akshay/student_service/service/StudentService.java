package com.akshay.student_service.service;

import com.akshay.student_service.dto.GraduationStatusDTO;
import com.akshay.student_service.dto.StudentProfileDTO;
import com.akshay.student_service.dto.UpdateStudentNameDTO;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.repository.EnrollmentRepository;
import com.akshay.student_service.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${finance.service.url:http://localhost:80}")
    private String financeServiceUrl;

    @Value("${library.service.url:http://localhost:8081}")
    private String libraryServiceUrl;


    @Autowired
    public StudentService(StudentRepository studentRepository,
                          EnrollmentRepository enrollmentRepository,
                          PasswordEncoder passwordEncoder,
                          RestTemplate restTemplate) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    public Student registerStudent(Student student) {
        String generatedStudentId = "c" + (int)(Math.random() * 10000000);
        student.setStudentId(generatedStudentId);

        student.setPassword(passwordEncoder.encode(student.getPassword()));

        Student savedStudent = studentRepository.save(student);

        try {
            var financeRequest = new java.util.HashMap<String, Object>();
            financeRequest.put("studentId", generatedStudentId);
            String financeUrl = financeServiceUrl + "/accounts";
            restTemplate.postForEntity(financeUrl, financeRequest, String.class);
        } catch (Exception e) {
            System.out.println("Failed to create finance account: " + e.getMessage());
        }

        try {
            var libraryRequest = new java.util.HashMap<String, Object>();
            libraryRequest.put("studentId", generatedStudentId);
            String libraryUrl = libraryServiceUrl + "/api/register";
            restTemplate.postForEntity(libraryUrl, libraryRequest, String.class);
            System.out.println("Registered student in library");
        } catch (Exception e) {
            System.out.println("Failed to register student in library: " + e.getMessage());
        }

        return savedStudent;
    }

    public Student loginStudent(String username, String password) {
        Student student = studentRepository.findByUsername(username);
        if (student == null || !passwordEncoder.matches(password, student.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        return student;
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
    }

    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));
    }

    public StudentProfileDTO getStudentProfile(String studentId) {
        Student student = getStudentByStudentId(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdString(studentId);
        return new StudentProfileDTO(student, enrollments);
    }
    public Map<String, Object> checkGraduationEligibility(String studentId) {
        Map<String, Object> response = new HashMap<>();

        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));

        try {
            String url = financeServiceUrl + "/accounts/student/" + studentId;
            Map<String, Object> financeStatus = restTemplate.getForObject(url, Map.class);

            boolean eligible = financeStatus != null && Boolean.FALSE.equals(financeStatus.get("hasOutstandingInvoices"));
            response.put("eligible", eligible);

            if (!eligible) {
                response.put("reason", "Student has outstanding invoices.");
            } else {
                response.put("reason", "Student is clear for graduation.");
            }
        } catch (Exception e) {
            response.put("eligible", false);
            response.put("reason", "Could not verify with Finance service: " + e.getMessage());
        }

        return response;
    }
    public GraduationStatusDTO getGraduationStatus(String studentId) {
        Student student = getStudentByStudentId(studentId);

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdString(studentId);

        List<String> completedCourses = new ArrayList<>();
        List<String> pendingCourses = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.isCompleted()) {
                completedCourses.add(enrollment.getCourseCode());
            } else {
                pendingCourses.add(enrollment.getCourseCode());
            }
        }

        boolean hasOutstandingInvoices = false;
        try {
            String financeUrl = financeServiceUrl + "/accounts/student/" + studentId;
            Map<String, Object> financeResponse = restTemplate.getForObject(financeUrl, Map.class);
            if (financeResponse != null && Boolean.TRUE.equals(financeResponse.get("hasOutstandingBalance"))) {
                hasOutstandingInvoices = true;
            }
        } catch (Exception e) {
            System.out.println("Error checking finance service: " + e.getMessage());
        }

        return new GraduationStatusDTO(studentId, completedCourses, pendingCourses, hasOutstandingInvoices);
    }

    public String payInvoice(String invoiceRef) {
        String url = financeServiceUrl + "/invoices/" + invoiceRef + "/pay";
        restTemplate.put(url, null);
        return "Invoice " + invoiceRef + " marked as PAID.";
    }

    public String cancelInvoice(String invoiceRef) {
        String url = financeServiceUrl + "/invoices/" + invoiceRef + "/cancel";
        restTemplate.delete(url);
        return "Invoice " + invoiceRef + " has been CANCELLED.";
    }

    public Student updateStudentName(String studentId, UpdateStudentNameDTO dto) {
        Optional<Student> optionalStudent = studentRepository.findByStudentId(studentId);

        if (optionalStudent.isEmpty()) {
            throw new RuntimeException("Student not found with ID: " + studentId);
        }

        Student student = optionalStudent.get();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());

        return studentRepository.save(student);
    }
}
