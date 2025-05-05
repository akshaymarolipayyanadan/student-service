package com.akshay.student_service.controller;

import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }


    @PostMapping("/enroll")
    public Map<String, Object> enrollStudent(@RequestParam String studentId,
                                             @RequestParam String courseCode) {
        return enrollmentService.enrollStudent(studentId, courseCode);
    }

    @PutMapping("/complete")
    public ResponseEntity<?> completeCourse(@RequestParam String studentId,
                                            @RequestParam String courseCode) {
        try {
            String result = enrollmentService.markCourseAsCompleted(studentId, courseCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @GetMapping("/student/{studentId}")
    public List<Enrollment> getStudentEnrollments(@PathVariable String studentId) {
        return enrollmentService.getEnrollmentsByStudentId(studentId);
    }
}
