package com.akshay.student_service.dto;

public class EnrollmentDTO {

    private Long id;
    private String studentIdString;
    private String courseCode;
    private boolean completed;

    public EnrollmentDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentIdString() {
        return studentIdString;
    }

    public void setStudentIdString(String studentIdString) {
        this.studentIdString = studentIdString;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
