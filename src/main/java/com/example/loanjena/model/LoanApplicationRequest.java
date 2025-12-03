package com.example.loanjena.model;

public class LoanApplicationRequest {
    private String applicantId;
    private int age;
    private int creditScore;
    private boolean isStudent;

    // Getters and Setters
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    public boolean isStudent() { return isStudent; }
    public void setStudent(boolean student) { isStudent = student; }
}