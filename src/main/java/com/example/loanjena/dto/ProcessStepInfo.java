package com.example.loanjena.dto;

import java.util.List;

/**
 * 流程步骤信息
 */
public class ProcessStepInfo {
    
    /** 步骤URI */
    private String stepUri;
    
    /** 步骤名称 */
    private String stepName;
    
    /** 步骤标签 */
    private String label;
    
    /** 步骤描述 */
    private String comment;
    
    /** 步骤编号 */
    private Integer stepNumber;
    
    /** 上一步骤URI列表 */
    private List<String> previousSteps;
    
    /** 下一步骤URI列表 */
    private List<String> nextSteps;
    
    /** 所需实体类型列表 */
    private List<String> requiredEntities;
    
    /** 产出实体类型列表 */
    private List<String> producedEntities;
    
    /** 映射的ODA组件 */
    private String mappedComponent;
    
    /** 使用的API */
    private String usedAPI;
    
    /** eTOM引用 */
    private String etomRef;
    
    /** 步骤代码 */
    private String stepCode;

    public ProcessStepInfo() {
    }

    public String getStepUri() {
        return stepUri;
    }

    public void setStepUri(String stepUri) {
        this.stepUri = stepUri;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public List<String> getPreviousSteps() {
        return previousSteps;
    }

    public void setPreviousSteps(List<String> previousSteps) {
        this.previousSteps = previousSteps;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(List<String> nextSteps) {
        this.nextSteps = nextSteps;
    }

    public List<String> getRequiredEntities() {
        return requiredEntities;
    }

    public void setRequiredEntities(List<String> requiredEntities) {
        this.requiredEntities = requiredEntities;
    }

    public List<String> getProducedEntities() {
        return producedEntities;
    }

    public void setProducedEntities(List<String> producedEntities) {
        this.producedEntities = producedEntities;
    }

    public String getMappedComponent() {
        return mappedComponent;
    }

    public void setMappedComponent(String mappedComponent) {
        this.mappedComponent = mappedComponent;
    }

    public String getUsedAPI() {
        return usedAPI;
    }

    public void setUsedAPI(String usedAPI) {
        this.usedAPI = usedAPI;
    }

    public String getEtomRef() {
        return etomRef;
    }

    public void setEtomRef(String etomRef) {
        this.etomRef = etomRef;
    }

    public String getStepCode() {
        return stepCode;
    }

    public void setStepCode(String stepCode) {
        this.stepCode = stepCode;
    }
}
