package com.example.loanjena.service;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 业务规则验证服务
 * 用于验证客户涉诈和欠费等业务规则
 */
@Service
public class BusinessRuleValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleValidationService.class);
    
    private static final String NS = "https://iwhalecloud.com/ontology/transfer#";
    private static final String OWL_FILE = "owl/transfer_order_ontology.owl";
    
    private OntModel ontModel;
    
    /**
     * 初始化本体模型
     */
    private void initModel() {
        if (ontModel == null) {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            try {
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(OWL_FILE);
                if (is == null) {
                    throw new RuntimeException("无法找到本体文件: " + OWL_FILE);
                }
                ontModel.read(is, NS, "RDF/XML");
                logger.info("本体模型加载成功，共 {} 个三元组", ontModel.size());
            } catch (Exception e) {
                logger.error("加载本体模型失败", e);
                throw new RuntimeException("加载本体模型失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 验证客户涉诈规则
     * @param custId 客户ID
     * @param custName 客户名称
     * @param custStatus 客户状态
     * @param isSource 是否源客户
     * @return 验证结果
     */
    public ValidationResult validateFraudCheck(String custId, String custName, 
                                               String custStatus, boolean isSource) {
        initModel();
        
        ValidationResult result = new ValidationResult();
        result.setRuleCode("FraudCustomerCheckRule");
        result.setCustomerId(custId);
        result.setCustomerName(custName);
        
        // 创建客户个体
        String customerType = isSource ? "SourceCustomer" : "TargetCustomer";
        String customerUri = NS + customerType + "_" + custId;
        Individual customer = ontModel.createIndividual(customerUri, 
            ontModel.getOntClass(NS + customerType));
        
        // 设置客户属性
        Property custIdProp = ontModel.getProperty(NS + "custId");
        Property custNameProp = ontModel.getProperty(NS + "custName");
        Property custStatusProp = ontModel.getProperty(NS + "custStatus");
        Property blockBusinessProp = ontModel.getProperty(NS + "blockBusinessOperation");
        Property businessActionProp = ontModel.getProperty(NS + "businessAction");
        Property actionMessageProp = ontModel.getProperty(NS + "actionMessage");
        
        customer.addProperty(custIdProp, custId);
        customer.addProperty(custNameProp, custName);
        customer.addProperty(custStatusProp, custStatus);
        
        // 应用SWRL规则逻辑
        if ("FRAUD".equals(custStatus)) {
            customer.addProperty(blockBusinessProp, ontModel.createTypedLiteral(true));
            customer.addProperty(businessActionProp, "STOP_ALL_OPERATIONS");
            customer.addProperty(actionMessageProp, "涉诈用户不允许办理任何业务，待涉诈解除后方可继续办理");
            
            result.setPassed(false);
            result.setBlocked(true);
            result.setBlockType("STOP_ALL_OPERATIONS");
            result.setMessage("涉诈用户不允许办理任何业务，待涉诈解除后方可继续办理");
        } else {
            result.setPassed(true);
            result.setBlocked(false);
            result.setMessage("客户状态正常，允许办理业务");
        }
        
        logger.info("涉诈检查完成: custId={}, status={}, passed={}", 
            custId, custStatus, result.isPassed());
        
        return result;
    }
    
    /**
     * 验证客户欠费规则
     * @param custId 客户ID
     * @param custName 客户名称
     * @param arrearsStatus 欠费状态
     * @param hasSubscription 是否有订阅
     * @return 验证结果
     */
    public ValidationResult validateArrearsCheck(String custId, String custName, 
                                                 String arrearsStatus, boolean hasSubscription) {
        initModel();
        
        ValidationResult result = new ValidationResult();
        result.setRuleCode("ArrearsCheckRule");
        result.setCustomerId(custId);
        result.setCustomerName(custName);
        
        // 创建源客户个体
        String customerUri = NS + "SourceCustomer_" + custId;
        Individual customer = ontModel.createIndividual(customerUri, 
            ontModel.getOntClass(NS + "SourceCustomer"));
        
        // 设置客户属性
        Property custIdProp = ontModel.getProperty(NS + "custId");
        Property custNameProp = ontModel.getProperty(NS + "custName");
        Property arrearsStatusProp = ontModel.getProperty(NS + "arrearsStatus");
        Property blockTransferProp = ontModel.getProperty(NS + "blockTransferOperation");
        Property businessActionProp = ontModel.getProperty(NS + "businessAction");
        Property actionMessageProp = ontModel.getProperty(NS + "actionMessage");
        
        customer.addProperty(custIdProp, custId);
        customer.addProperty(custNameProp, custName);
        customer.addProperty(arrearsStatusProp, arrearsStatus);
        
        // 如果有订阅，创建订阅关系
        if (hasSubscription) {
            String subscriptionUri = NS + "Subscription_" + custId;
            Individual subscription = ontModel.createIndividual(subscriptionUri,
                ontModel.getOntClass(NS + "TransferableSubscription"));
            
            Property ownsSubProp = ontModel.getProperty(NS + "ownsSubscription");
            customer.addProperty(ownsSubProp, subscription);
        }
        
        // 应用SWRL规则逻辑
        if ("ARREARS".equals(arrearsStatus) && hasSubscription) {
            customer.addProperty(blockTransferProp, ontModel.createTypedLiteral(true));
            customer.addProperty(businessActionProp, "STOP_TRANSFER");
            customer.addProperty(actionMessageProp, "用户存在欠费，不允许办理过户业务，请先缴清费用");
            
            result.setPassed(false);
            result.setBlocked(true);
            result.setBlockType("STOP_TRANSFER");
            result.setMessage("用户存在欠费，不允许办理过户业务，请先缴清费用");
        } else {
            result.setPassed(true);
            result.setBlocked(false);
            result.setMessage("客户无欠费，允许办理过户业务");
        }
        
        logger.info("欠费检查完成: custId={}, arrearsStatus={}, passed={}", 
            custId, arrearsStatus, result.isPassed());
        
        return result;
    }
    
    /**
     * 综合验证（涉诈 + 欠费）
     * @param custId 客户ID
     * @param custName 客户名称
     * @param custStatus 客户状态
     * @param arrearsStatus 欠费状态
     * @param isSource 是否源客户
     * @param hasSubscription 是否有订阅
     * @return 验证结果列表
     */
    public List<ValidationResult> validateAll(String custId, String custName, 
                                              String custStatus, String arrearsStatus,
                                              boolean isSource, boolean hasSubscription) {
        List<ValidationResult> results = new ArrayList<>();
        
        // 1. 涉诈检查（优先级最高）
        ValidationResult fraudResult = validateFraudCheck(custId, custName, custStatus, isSource);
        results.add(fraudResult);
        
        // 如果涉诈检查不通过，直接返回
        if (!fraudResult.isPassed()) {
            return results;
        }
        
        // 2. 欠费检查（仅对源客户进行）
        if (isSource) {
            ValidationResult arrearsResult = validateArrearsCheck(custId, custName, 
                arrearsStatus, hasSubscription);
            results.add(arrearsResult);
        }
        
        return results;
    }
    
    /**
     * 查询业务规则信息
     * @param ruleCode 规则编码
     * @return 规则信息
     */
    public Map<String, Object> getRuleInfo(String ruleCode) {
        initModel();
        
        Map<String, Object> ruleInfo = new HashMap<>();
        
        String ruleUri = NS + ruleCode;
        Individual rule = ontModel.getIndividual(ruleUri);
        
        if (rule != null) {
            ruleInfo.put("ruleCode", ruleCode);
            ruleInfo.put("ruleType", getPropertyValue(rule, NS + "logicType"));
            ruleInfo.put("expression", getPropertyValue(rule, NS + "logicExpression"));
            ruleInfo.put("label", getPropertyValue(rule, "http://www.w3.org/2000/01/rdf-schema#label"));
            ruleInfo.put("comment", getPropertyValue(rule, "http://www.w3.org/2000/01/rdf-schema#comment"));
        }
        
        return ruleInfo;
    }
    
    /**
     * 获取所有业务规则
     * @return 规则列表
     */
    public List<Map<String, Object>> getAllRules() {
        initModel();
        
        List<Map<String, Object>> rules = new ArrayList<>();
        
        OntClass businessLogicClass = ontModel.getOntClass(NS + "BusinessLogic");
        ExtendedIterator<? extends OntResource> instances = businessLogicClass.listInstances();
        
        while (instances.hasNext()) {
            Individual rule = (Individual) instances.next();
            Map<String, Object> ruleInfo = new HashMap<>();
            
            String localName = rule.getLocalName();
            ruleInfo.put("ruleCode", localName);
            ruleInfo.put("ruleType", getPropertyValue(rule, NS + "logicType"));
            ruleInfo.put("expression", getPropertyValue(rule, NS + "logicExpression"));
            ruleInfo.put("label", getPropertyValue(rule, "http://www.w3.org/2000/01/rdf-schema#label"));
            ruleInfo.put("comment", getPropertyValue(rule, "http://www.w3.org/2000/01/rdf-schema#comment"));
            
            rules.add(ruleInfo);
        }
        
        return rules;
    }
    
    private String getPropertyValue(Individual individual, String propertyUri) {
        Statement stmt = individual.getProperty(ontModel.getProperty(propertyUri));
        if (stmt != null) {
            RDFNode node = stmt.getObject();
            if (node.isLiteral()) {
                return node.asLiteral().getString();
            } else if (node.isResource()) {
                return node.asResource().getURI();
            }
        }
        return null;
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private String ruleCode;
        private String customerId;
        private String customerName;
        private boolean passed;
        private boolean blocked;
        private String blockType;
        private String message;
        
        // Getters and Setters
        public String getRuleCode() {
            return ruleCode;
        }
        
        public void setRuleCode(String ruleCode) {
            this.ruleCode = ruleCode;
        }
        
        public String getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
        
        public boolean isPassed() {
            return passed;
        }
        
        public void setPassed(boolean passed) {
            this.passed = passed;
        }
        
        public boolean isBlocked() {
            return blocked;
        }
        
        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }
        
        public String getBlockType() {
            return blockType;
        }
        
        public void setBlockType(String blockType) {
            this.blockType = blockType;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
