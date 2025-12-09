package com.example.loanjena.model;

import java.util.ArrayList;
import java.util.List;

public class ReasoningStep {
    private int stepNumber;
    private String stepName;
    private String description;
    private List<String> facts = new ArrayList<>();
    private List<String> inferences = new ArrayList<>();
    private String result;

    public ReasoningStep(int stepNumber, String stepName, String description) {
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.description = description;
    }

    public void addFact(String fact) {
        this.facts.add(fact);
    }

    public void addInference(String inference) {
        this.inferences.add(inference);
    }

    // Getters and Setters
    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFacts() {
        return facts;
    }

    public void setFacts(List<String> facts) {
        this.facts = facts;
    }

    public List<String> getInferences() {
        return inferences;
    }

    public void setInferences(List<String> inferences) {
        this.inferences = inferences;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
