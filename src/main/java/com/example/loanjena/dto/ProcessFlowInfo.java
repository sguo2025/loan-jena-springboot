package com.example.loanjena.dto;

import java.util.List;

/**
 * 流程信息
 */
public class ProcessFlowInfo {
    
    /** 流程总步数 */
    private Integer totalSteps;
    
    /** 所有流程步骤列表 */
    private List<ProcessStepInfo> steps;
    
    /** 流程开始步骤URI */
    private String startStepUri;
    
    /** 流程结束步骤URI */
    private String endStepUri;

    public ProcessFlowInfo() {
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public List<ProcessStepInfo> getSteps() {
        return steps;
    }

    public void setSteps(List<ProcessStepInfo> steps) {
        this.steps = steps;
    }

    public String getStartStepUri() {
        return startStepUri;
    }

    public void setStartStepUri(String startStepUri) {
        this.startStepUri = startStepUri;
    }

    public String getEndStepUri() {
        return endStepUri;
    }

    public void setEndStepUri(String endStepUri) {
        this.endStepUri = endStepUri;
    }
}
