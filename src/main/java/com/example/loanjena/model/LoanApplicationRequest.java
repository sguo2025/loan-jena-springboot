package com.example.loanjena.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "贷款申请请求")
public class LoanApplicationRequest {
    @Schema(description = "申请人ID", example = "Bob")
    private String applicantId;

    @Schema(description = "申请人年龄", example = "30")
    private int age;

    @Schema(description = "信用评分", example = "700")
    private int creditScore;

    @Schema(description = "是否为学生", example = "false")
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