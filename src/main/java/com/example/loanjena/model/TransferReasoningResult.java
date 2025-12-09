package com.example.loanjena.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 过户业务推理结果
 */
@Data
public class TransferReasoningResult {
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 当前步骤编号
     */
    private Integer currentStep;
    
    /**
     * 当前步骤名称
     */
    private String currentStepName;
    
    /**
     * 总步骤数
     */
    private Integer totalSteps = 8;
    
    /**
     * 下一步骤编号
     */
    private Integer nextStep;
    
    /**
     * 下一步骤名称
     */
    private String nextStepName;
    
    /**
     * 下一步操作提示
     */
    private String nextStepGuide;
    
    /**
     * 是否可以回退
     */
    private Boolean canRollback;
    
    /**
     * 可回退到的步骤
     */
    private Integer rollbackToStep;
    
    /**
     * 推理步骤详情
     */
    private List<ReasoningStep> steps = new ArrayList<>();
    
    /**
     * 最终决策
     */
    private String finalDecision;
    
    /**
     * 推理摘要
     */
    private String summary;
    
    /**
     * 步骤完成状态
     */
    private StepCompletionStatus completionStatus;
    
    /**
     * 智能体决策说明
     */
    private String agentDecisionExplanation;
    
    /**
     * ODA组件调用记录
     */
    private List<String> odaComponentCalls = new ArrayList<>();
    
    /**
     * TMF API调用记录
     */
    private List<String> tmfApiCalls = new ArrayList<>();
    
    /**
     * 业务规则应用记录
     */
    private List<String> businessRulesApplied = new ArrayList<>();
    
    /**
     * 添加推理步骤
     */
    public void addStep(ReasoningStep step) {
        this.steps.add(step);
    }
    
    /**
     * 添加ODA组件调用
     */
    public void addOdaComponentCall(String componentCall) {
        this.odaComponentCalls.add(componentCall);
    }
    
    /**
     * 添加TMF API调用
     */
    public void addTmfApiCall(String apiCall) {
        this.tmfApiCalls.add(apiCall);
    }
    
    /**
     * 添加业务规则
     */
    public void addBusinessRule(String rule) {
        this.businessRulesApplied.add(rule);
    }
    
    /**
     * 步骤完成状态
     */
    @Data
    public static class StepCompletionStatus {
        private boolean step1Completed;  // 定位源客户
        private boolean step2Completed;  // 过户号码选择
        private boolean step3Completed;  // 创建客户订单
        private boolean step4Completed;  // 过户业务初始化
        private boolean step5Completed;  // 公共属性初始化
        private boolean step6Completed;  // 目标客户确认
        private boolean step7Completed;  // 订单保存
        private boolean step8Completed;  // 订单确认
        
        /**
         * 获取完成的步骤数
         */
        public int getCompletedCount() {
            int count = 0;
            if (step1Completed) count++;
            if (step2Completed) count++;
            if (step3Completed) count++;
            if (step4Completed) count++;
            if (step5Completed) count++;
            if (step6Completed) count++;
            if (step7Completed) count++;
            if (step8Completed) count++;
            return count;
        }
        
        /**
         * 设置步骤完成状态
         */
        public void setStepCompleted(int stepNumber, boolean completed) {
            switch (stepNumber) {
                case 1: step1Completed = completed; break;
                case 2: step2Completed = completed; break;
                case 3: step3Completed = completed; break;
                case 4: step4Completed = completed; break;
                case 5: step5Completed = completed; break;
                case 6: step6Completed = completed; break;
                case 7: step7Completed = completed; break;
                case 8: step8Completed = completed; break;
            }
        }
        
        /**
         * 获取步骤完成状态
         */
        public boolean isStepCompleted(int stepNumber) {
            switch (stepNumber) {
                case 1: return step1Completed;
                case 2: return step2Completed;
                case 3: return step3Completed;
                case 4: return step4Completed;
                case 5: return step5Completed;
                case 6: return step6Completed;
                case 7: return step7Completed;
                case 8: return step8Completed;
                default: return false;
            }
        }
    }
}
