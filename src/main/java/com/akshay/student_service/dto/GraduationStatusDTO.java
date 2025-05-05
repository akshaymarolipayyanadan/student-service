package com.akshay.student_service.dto;

import java.util.List;

public class GraduationStatusDTO {

    private String studentId;
    private List<String> completedCourses;
    private List<String> pendingCourses;
    private boolean hasOutstandingInvoices;

    public GraduationStatusDTO(String studentId, List<String> completedCourses, List<String> pendingCourses, boolean hasOutstandingInvoices) {
        this.studentId = studentId;
        this.completedCourses = completedCourses;
        this.pendingCourses = pendingCourses;
        this.hasOutstandingInvoices = hasOutstandingInvoices;
    }
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<String> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<String> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public List<String> getPendingCourses() {
        return pendingCourses;
    }

    public void setPendingCourses(List<String> pendingCourses) {
        this.pendingCourses = pendingCourses;
    }

    public boolean isHasOutstandingInvoices() {
        return hasOutstandingInvoices;
    }

    public void setHasOutstandingInvoices(boolean hasOutstandingInvoices) {
        this.hasOutstandingInvoices = hasOutstandingInvoices;
    }
}
