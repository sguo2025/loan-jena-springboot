package com.example.loanjena.model;

import java.util.ArrayList;
import java.util.List;

public class ReasoningResult {
    private String orderId;
    private String finalDecision;
    private List<ReasoningStep> steps = new ArrayList<>();
    private String summary;

    public void addStep(ReasoningStep step) {
        this.steps.add(step);
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public List<ReasoningStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ReasoningStep> steps) {
        this.steps = steps;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
