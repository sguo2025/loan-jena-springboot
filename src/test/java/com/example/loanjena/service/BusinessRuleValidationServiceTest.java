package com.example.loanjena.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务规则验证服务单元测试
 */
@SpringBootTest
public class BusinessRuleValidationServiceTest {
    
    @Autowired
    private BusinessRuleValidationService validationService;
    
    @BeforeEach
    void setUp() {
        // 每个测试前的准备工作
    }
    
    @Test
    @DisplayName("测试涉诈客户检查 - 涉诈状态应拦截")
    void testFraudCustomerCheck_FraudStatus() {
        // Given
        String custId = "CUST001";
        String custName = "张三";
        String custStatus = "FRAUD";
        boolean isSource = true;
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateFraudCheck(custId, custName, custStatus, isSource);
        
        // Then
        assertNotNull(result);
        assertEquals("FraudCustomerCheckRule", result.getRuleCode());
        assertEquals(custId, result.getCustomerId());
        assertEquals(custName, result.getCustomerName());
        assertFalse(result.isPassed(), "涉诈客户检查应失败");
        assertTrue(result.isBlocked(), "应拦截业务操作");
        assertEquals("STOP_ALL_OPERATIONS", result.getBlockType());
        assertTrue(result.getMessage().contains("涉诈用户不允许办理任何业务"));
    }
    
    @Test
    @DisplayName("测试涉诈客户检查 - 正常状态应通过")
    void testFraudCustomerCheck_NormalStatus() {
        // Given
        String custId = "CUST002";
        String custName = "李四";
        String custStatus = "NORMAL";
        boolean isSource = true;
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateFraudCheck(custId, custName, custStatus, isSource);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isPassed(), "正常客户检查应通过");
        assertFalse(result.isBlocked(), "不应拦截业务操作");
        assertNull(result.getBlockType());
        assertTrue(result.getMessage().contains("客户状态正常"));
    }
    
    @Test
    @DisplayName("测试涉诈客户检查 - 目标客户涉诈也应拦截")
    void testFraudCustomerCheck_TargetCustomerFraud() {
        // Given
        String custId = "CUST003";
        String custName = "王五";
        String custStatus = "FRAUD";
        boolean isSource = false;  // 目标客户
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateFraudCheck(custId, custName, custStatus, isSource);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isPassed(), "目标客户涉诈也应拦截");
        assertTrue(result.isBlocked());
        assertEquals("STOP_ALL_OPERATIONS", result.getBlockType());
    }
    
    @Test
    @DisplayName("测试欠费检查 - 欠费状态有订阅应拦截")
    void testArrearsCheck_ArrearsWithSubscription() {
        // Given
        String custId = "CUST004";
        String custName = "赵六";
        String arrearsStatus = "ARREARS";
        boolean hasSubscription = true;
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateArrearsCheck(custId, custName, arrearsStatus, hasSubscription);
        
        // Then
        assertNotNull(result);
        assertEquals("ArrearsCheckRule", result.getRuleCode());
        assertFalse(result.isPassed(), "欠费客户检查应失败");
        assertTrue(result.isBlocked(), "应拦截过户业务");
        assertEquals("STOP_TRANSFER", result.getBlockType());
        assertTrue(result.getMessage().contains("用户存在欠费"));
    }
    
    @Test
    @DisplayName("测试欠费检查 - 无欠费应通过")
    void testArrearsCheck_NoArrears() {
        // Given
        String custId = "CUST005";
        String custName = "孙七";
        String arrearsStatus = "NO_ARREARS";
        boolean hasSubscription = true;
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateArrearsCheck(custId, custName, arrearsStatus, hasSubscription);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isPassed(), "无欠费客户检查应通过");
        assertFalse(result.isBlocked());
        assertTrue(result.getMessage().contains("客户无欠费"));
    }
    
    @Test
    @DisplayName("测试欠费检查 - 欠费但无订阅应通过")
    void testArrearsCheck_ArrearsWithoutSubscription() {
        // Given
        String custId = "CUST006";
        String custName = "周八";
        String arrearsStatus = "ARREARS";
        boolean hasSubscription = false;  // 无订阅
        
        // When
        BusinessRuleValidationService.ValidationResult result = 
            validationService.validateArrearsCheck(custId, custName, arrearsStatus, hasSubscription);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isPassed(), "无订阅时规则不触发");
        assertFalse(result.isBlocked());
    }
    
    @Test
    @DisplayName("测试综合验证 - 涉诈客户优先级最高")
    void testValidateAll_FraudCustomerPriority() {
        // Given
        String custId = "CUST007";
        String custName = "吴九";
        String custStatus = "FRAUD";
        String arrearsStatus = "ARREARS";  // 同时欠费
        boolean isSource = true;
        boolean hasSubscription = true;
        
        // When
        List<BusinessRuleValidationService.ValidationResult> results = 
            validationService.validateAll(custId, custName, custStatus, arrearsStatus, 
                isSource, hasSubscription);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size(), "涉诈检查失败后不应继续检查");
        
        BusinessRuleValidationService.ValidationResult fraudResult = results.get(0);
        assertEquals("FraudCustomerCheckRule", fraudResult.getRuleCode());
        assertFalse(fraudResult.isPassed());
        assertTrue(fraudResult.isBlocked());
    }
    
    @Test
    @DisplayName("测试综合验证 - 正常但欠费客户")
    void testValidateAll_NormalButArrears() {
        // Given
        String custId = "CUST008";
        String custName = "郑十";
        String custStatus = "NORMAL";
        String arrearsStatus = "ARREARS";
        boolean isSource = true;
        boolean hasSubscription = true;
        
        // When
        List<BusinessRuleValidationService.ValidationResult> results = 
            validationService.validateAll(custId, custName, custStatus, arrearsStatus, 
                isSource, hasSubscription);
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size(), "应进行两项检查");
        
        BusinessRuleValidationService.ValidationResult fraudResult = results.get(0);
        assertEquals("FraudCustomerCheckRule", fraudResult.getRuleCode());
        assertTrue(fraudResult.isPassed(), "涉诈检查应通过");
        
        BusinessRuleValidationService.ValidationResult arrearsResult = results.get(1);
        assertEquals("ArrearsCheckRule", arrearsResult.getRuleCode());
        assertFalse(arrearsResult.isPassed(), "欠费检查应失败");
        assertTrue(arrearsResult.isBlocked());
    }
    
    @Test
    @DisplayName("测试综合验证 - 完全正常的客户")
    void testValidateAll_NormalCustomer() {
        // Given
        String custId = "CUST009";
        String custName = "钱十一";
        String custStatus = "NORMAL";
        String arrearsStatus = "NO_ARREARS";
        boolean isSource = true;
        boolean hasSubscription = true;
        
        // When
        List<BusinessRuleValidationService.ValidationResult> results = 
            validationService.validateAll(custId, custName, custStatus, arrearsStatus, 
                isSource, hasSubscription);
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size(), "应进行两项检查");
        
        for (BusinessRuleValidationService.ValidationResult result : results) {
            assertTrue(result.isPassed(), "所有检查都应通过");
            assertFalse(result.isBlocked());
        }
    }
    
    @Test
    @DisplayName("测试综合验证 - 目标客户不检查欠费")
    void testValidateAll_TargetCustomerNoArrearsCheck() {
        // Given
        String custId = "CUST010";
        String custName = "陈十二";
        String custStatus = "NORMAL";
        String arrearsStatus = "ARREARS";  // 有欠费
        boolean isSource = false;  // 目标客户
        boolean hasSubscription = true;
        
        // When
        List<BusinessRuleValidationService.ValidationResult> results = 
            validationService.validateAll(custId, custName, custStatus, arrearsStatus, 
                isSource, hasSubscription);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size(), "目标客户只检查涉诈");
        
        BusinessRuleValidationService.ValidationResult fraudResult = results.get(0);
        assertEquals("FraudCustomerCheckRule", fraudResult.getRuleCode());
        assertTrue(fraudResult.isPassed());
    }
    
    @Test
    @DisplayName("测试获取规则信息 - 涉诈规则")
    void testGetRuleInfo_FraudRule() {
        // When
        Map<String, Object> ruleInfo = validationService.getRuleInfo("FraudCustomerCheckRule");
        
        // Then
        assertNotNull(ruleInfo);
        assertFalse(ruleInfo.isEmpty());
        assertEquals("FraudCustomerCheckRule", ruleInfo.get("ruleCode"));
        assertEquals("SWRL", ruleInfo.get("ruleType"));
        assertNotNull(ruleInfo.get("expression"));
        assertNotNull(ruleInfo.get("label"));
    }
    
    @Test
    @DisplayName("测试获取规则信息 - 欠费规则")
    void testGetRuleInfo_ArrearsRule() {
        // When
        Map<String, Object> ruleInfo = validationService.getRuleInfo("ArrearsCheckRule");
        
        // Then
        assertNotNull(ruleInfo);
        assertFalse(ruleInfo.isEmpty());
        assertEquals("ArrearsCheckRule", ruleInfo.get("ruleCode"));
        assertEquals("SWRL", ruleInfo.get("ruleType"));
        assertNotNull(ruleInfo.get("expression"));
        assertNotNull(ruleInfo.get("label"));
    }
    
    @Test
    @DisplayName("测试获取所有规则")
    void testGetAllRules() {
        // When
        List<Map<String, Object>> rules = validationService.getAllRules();
        
        // Then
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
        assertTrue(rules.size() >= 2, "至少应包含涉诈和欠费两个规则");
        
        boolean hasFraudRule = rules.stream()
            .anyMatch(r -> "FraudCustomerCheckRule".equals(r.get("ruleCode")));
        boolean hasArrearsRule = rules.stream()
            .anyMatch(r -> "ArrearsCheckRule".equals(r.get("ruleCode")));
        
        assertTrue(hasFraudRule, "应包含涉诈规则");
        assertTrue(hasArrearsRule, "应包含欠费规则");
    }
}
