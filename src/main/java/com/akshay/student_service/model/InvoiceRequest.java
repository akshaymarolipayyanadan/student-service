package com.akshay.student_service.model;

public class InvoiceRequest {
    private String studentId;
    private Double amount;
    private String description;

    public InvoiceRequest() {
    }

    public InvoiceRequest(String studentId, Double amount, String description) {
        this.studentId = studentId;
        this.amount = amount;
        this.description = description;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
