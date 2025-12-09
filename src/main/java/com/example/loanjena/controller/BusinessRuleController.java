package com.example.loanjena.controller;

import com.example.loanjena.service.BusinessRuleValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 业务规则验证控制器
 */
@RestController
@RequestMapping("/api/business-rules")
public class BusinessRuleController {
    
    @Autowired
    private BusinessRuleValidationService validationService;
    
    /**
     * 验证客户涉诈规则
     */
    @PostMapping("/validate/fraud")
    public ResponseEntity<Map<String, Object>> validateFraud(@RequestBody Map<String, Object> request) {
        String custId = (String) request.get("custId");
        String custName = (String) request.get("custName");
        String custStatus = (String) request.get("custStatus");
        Boolean isSource = (Boolean) request.getOrDefault("isSource", true);
        
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateFraudCheck(custId, custName, custStatus, isSource);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isPassed());
        response.put("ruleCode", result.getRuleCode());
        response.put("customerId", result.getCustomerId());
        response.put("customerName", result.getCustomerName());
        response.put("blocked", result.isBlocked());
        response.put("blockType", result.getBlockType());
        response.put("message", result.getMessage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证客户欠费规则
     */
    @PostMapping("/validate/arrears")
    public ResponseEntity<Map<String, Object>> validateArrears(@RequestBody Map<String, Object> request) {
        String custId = (String) request.get("custId");
        String custName = (String) request.get("custName");
        String arrearsStatus = (String) request.get("arrearsStatus");
        Boolean hasSubscription = (Boolean) request.getOrDefault("hasSubscription", true);
        
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateArrearsCheck(custId, custName, arrearsStatus, hasSubscription);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isPassed());
        response.put("ruleCode", result.getRuleCode());
        response.put("customerId", result.getCustomerId());
        response.put("customerName", result.getCustomerName());
        response.put("blocked", result.isBlocked());
        response.put("blockType", result.getBlockType());
        response.put("message", result.getMessage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 综合验证（涉诈 + 欠费）
     */
    @PostMapping("/validate/all")
    public ResponseEntity<Map<String, Object>> validateAll(@RequestBody Map<String, Object> request) {
        String custId = (String) request.get("custId");
        String custName = (String) request.get("custName");
        String custStatus = (String) request.get("custStatus");
        String arrearsStatus = (String) request.get("arrearsStatus");
        Boolean isSource = (Boolean) request.getOrDefault("isSource", true);
        Boolean hasSubscription = (Boolean) request.getOrDefault("hasSubscription", true);
        
        List<BusinessRuleValidationService.ValidationResult> results = 
            validationService.validateAll(custId, custName, custStatus, arrearsStatus, 
                isSource, hasSubscription);
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        boolean allPassed = true;
        
        for (BusinessRuleValidationService.ValidationResult result : results) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("ruleCode", result.getRuleCode());
            resultMap.put("passed", result.isPassed());
            resultMap.put("blocked", result.isBlocked());
            resultMap.put("blockType", result.getBlockType());
            resultMap.put("message", result.getMessage());
            resultList.add(resultMap);
            
            if (!result.isPassed()) {
                allPassed = false;
            }
        }
        
        response.put("success", allPassed);
        response.put("customerId", custId);
        response.put("customerName", custName);
        response.put("validationResults", resultList);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取规则信息
     */
    @GetMapping("/rule/{ruleCode}")
    public ResponseEntity<Map<String, Object>> getRuleInfo(@PathVariable String ruleCode) {
        Map<String, Object> ruleInfo = validationService.getRuleInfo(ruleCode);
        
        if (ruleInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ruleInfo);
    }
    
    /**
     * 获取所有规则
     */
    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getAllRules() {
        List<Map<String, Object>> rules = validationService.getAllRules();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", rules.size());
        response.put("rules", rules);
        
        return ResponseEntity.ok(response);
    }
}
