package com.akshay.student_service.dto;

import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;

import java.util.List;

public class StudentProfileDTO {

    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private List<Enrollment> enrolledCourses;

    public StudentProfileDTO(Student student, List<Enrollment> enrolledCourses) {
        this.studentId = student.getStudentId();
        this.firstName = student.getFirstName();
        this.lastName = student.getLastName();
        this.email = student.getEmail();
        this.enrolledCourses = enrolledCourses;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Enrollment> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<Enrollment> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }
}
