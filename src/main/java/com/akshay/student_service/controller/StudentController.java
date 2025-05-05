package com.akshay.student_service.controller;

import com.akshay.student_service.dto.GraduationStatusDTO;
import com.akshay.student_service.dto.StudentProfileDTO;
import com.akshay.student_service.dto.UpdateStudentNameDTO;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public Student registerStudent(@RequestBody Student student) {
        return studentService.registerStudent(student);
    }


//    @PostMapping("/login")
//    public String login(@RequestBody Map<String, String> creds) {
//        String username = creds.get("username");
//        String password = creds.get("password");
//        Student s = studentService.loginStudent(username, password);
//        return "Login successful! Welcome, " + s.getFirstName() + ".";
//    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        try {
            String username = creds.get("username");
            String password = creds.get("password");
            Student s = studentService.loginStudent(username, password);
            return ResponseEntity.ok("Login successful! Welcome, " + s.getFirstName() + ".");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/profile/{studentId}")
    public ResponseEntity<?> getStudentProfile(@PathVariable String studentId) {
        try {
            StudentProfileDTO profile = studentService.getStudentProfile(studentId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/check-graduation/{studentId}")
    public ResponseEntity<Map<String, Object>> checkGraduationEligibility(@PathVariable String studentId) {
        Map<String, Object> result = studentService.checkGraduationEligibility(studentId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/graduation-status/{studentId}")
    public ResponseEntity<?> getGraduationStatus(@PathVariable String studentId) {
        try {
            GraduationStatusDTO status = studentService.getGraduationStatus(studentId);
            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/invoices/{invoiceRef}/pay")
    public ResponseEntity<?> payInvoice(@PathVariable String invoiceRef) {
        try {
            String result = studentService.payInvoice(invoiceRef);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/invoices/{invoiceRef}/cancel")
    public ResponseEntity<?> cancelInvoice(@PathVariable String invoiceRef) {
        try {
            String result = studentService.cancelInvoice(invoiceRef);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{studentId}/update-name")
    public ResponseEntity<Student> updateStudentName(
            @PathVariable String studentId,
            @RequestBody UpdateStudentNameDTO dto
    ) {
        Student updatedStudent = studentService.updateStudentName(studentId, dto);
        return ResponseEntity.ok(updatedStudent);
    }




}
