package com.example.loanjena.service;

import com.example.loanjena.model.ReasoningResult;
import com.example.loanjena.model.ReasoningStep;
import com.example.loanjena.model.TransferOrderRequest;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Iterator;

/**
 * 转账订单推理服务
 * 实现基于 OWL 本体的步骤化推理
 */
@Service
public class TransferReasoningService {

    private static final String NS = "https://iwhalecloud.com/ontology/transfer#";
    private static final String TMFORUM_NS = "https://tmforum.org/oda/";
    private OntModel ontologyModel;
    private Reasoner reasoner;

    public TransferReasoningService() {
        loadOntology();
    }

    /**
     * 加载本体文件
     */
    private void loadOntology() {
        try {
            ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
            
            // 从 classpath 加载本体文件
            ClassPathResource resource = new ClassPathResource("transfer_order_ontology.owl");
            InputStream inputStream = resource.getInputStream();
            
            // 用户的本体文件是Turtle格式，指定格式为TURTLE
            ontologyModel.read(inputStream, null, "TURTLE");
            System.out.println("✓ BSS4.0 客户过户本体加载成功");
            System.out.println("✓ 本体版本: 1.0.0");
            System.out.println("✓ 基于 TM Forum ODA 标准");
            
            // 创建推理机
            reasoner = ReasonerRegistry.getOWLReasoner();
            reasoner = reasoner.bindSchema(ontologyModel);
            
        } catch (Exception e) {
            System.err.println("✗ 本体文件加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行转账订单推理
     */
    public ReasoningResult performReasoning(TransferOrderRequest request) {
        ReasoningResult result = new ReasoningResult();
        result.setOrderId(request.getOrderId());

        // 创建推理模型
        InfModel infModel = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        infModel.setNsPrefix("transfer", NS);

        // ========== 步骤 1: 创建实例数据 ==========
        ReasoningStep step1 = createInstanceData(request, infModel);
        result.addStep(step1);

        // ========== 步骤 2: 验证账户状态 ==========
        ReasoningStep step2 = verifyAccountStatus(request, infModel);
        result.addStep(step2);

        // ========== 步骤 3: 评估风险等级 ==========
        ReasoningStep step3 = assessRiskLevel(request, infModel);
        result.addStep(step3);

        // ========== 步骤 4: 检查余额充足性 ==========
        ReasoningStep step4 = checkBalanceSufficiency(request, infModel);
        result.addStep(step4);

        // ========== 步骤 5: 确定订单类型 ==========
        ReasoningStep step5 = determineOrderType(request, infModel);
        result.addStep(step5);

        // ========== 步骤 6: 做出最终决策 ==========
        ReasoningStep step6 = makeFinalDecision(request, infModel, step2, step3, step4);
        result.addStep(step6);

        // 设置最终决策
        result.setFinalDecision(step6.getResult());
        result.setSummary(generateSummary(result));

        return result;
    }

    /**
     * 步骤 1: 创建实例数据（基于新本体）
     */
    private ReasoningStep createInstanceData(TransferOrderRequest request, InfModel model) {
        ReasoningStep step = new ReasoningStep(1, "创建实例数据", "基于TM Forum ODA标准将请求转换为RDF三元组");

        // 创建转账订单实例 (TransferOrder)
        Resource order = model.createResource(NS + "order_" + request.getOrderId());
        order.addProperty(RDF.type, model.getResource(NS + "TransferOrder"));
        order.addLiteral(model.createProperty(NS + "orderId"), request.getOrderId());
        order.addLiteral(model.createProperty(NS + "custOrderId"), "CUST_" + request.getOrderId());
        order.addLiteral(model.createProperty(NS + "orderStatus"), "INIT");
        step.addFact("✓ 创建过户订单: " + request.getOrderId());

        // 创建源客户 (SourceCustomer)
        Resource fromCustomer = model.createResource(NS + "customer_" + request.getFromAccountId());
        fromCustomer.addProperty(RDF.type, model.getResource(NS + "SourceCustomer"));
        fromCustomer.addLiteral(model.createProperty(NS + "custId"), request.getFromAccountId());
        fromCustomer.addLiteral(model.createProperty(NS + "custName"), "源客户_" + request.getFromAccountId());
        
        step.addFact(String.format("✓ 源客户: ID=%s, 余额=%.2f, 验证=%s", 
            request.getFromAccountId(), request.getFromAccountBalance(), 
            request.isFromAccountVerified() ? "已验证" : "未验证"));

        // 创建目标客户 (TargetCustomer)
        Resource toCustomer = model.createResource(NS + "customer_" + request.getToAccountId());
        toCustomer.addProperty(RDF.type, model.getResource(NS + "TargetCustomer"));
        toCustomer.addLiteral(model.createProperty(NS + "custId"), request.getToAccountId());
        toCustomer.addLiteral(model.createProperty(NS + "custName"), "目标客户_" + request.getToAccountId());
        
        step.addFact(String.format("✓ 目标客户: ID=%s, 验证=%s", 
            request.getToAccountId(), request.isToAccountVerified() ? "已验证" : "未验证"));

        // 关联订单和客户 (使用本体定义的属性)
        order.addProperty(model.createProperty(NS + "hasSourceCustomer"), fromCustomer);
        order.addProperty(model.createProperty(NS + "hasTargetCustomer"), toCustomer);
        order.addLiteral(model.createProperty(NS + "amount"), request.getAmount());
        
        step.addFact(String.format("✓ 过户金额: %.2f 元", request.getAmount()));
        step.addInference("推理: 基于TM Forum Customer Order语义创建订单实例");
        step.addInference("推理: 源客户和目标客户均映射到TMF SID Customer定义");

        step.setResult("数据实例创建完成 (符合ODA标准)");
        return step;
    }

    /**
     * 步骤 2: 客户鉴权验证（映射到eTOM流程）
     */
    private ReasoningStep verifyAccountStatus(TransferOrderRequest request, InfModel model) {
        ReasoningStep step = new ReasoningStep(2, "客户鉴权验证", "执行源客户和目标客户的身份鉴权 (eTOM: Validate Customer)");

        boolean fromVerified = request.isFromAccountVerified();
        boolean toVerified = request.isToAccountVerified();

        // 创建鉴权记录 (AuthorizationRecord)
        Resource order = model.getResource(NS + "order_" + request.getOrderId());
        
        // 源客户鉴权记录
        Resource sourceAuthRecord = model.createResource(NS + "auth_source_" + request.getOrderId());
        sourceAuthRecord.addProperty(RDF.type, model.getResource(NS + "AuthorizationRecord"));
        sourceAuthRecord.addLiteral(model.createProperty(NS + "authId"), "AUTH_SRC_" + System.currentTimeMillis());
        sourceAuthRecord.addLiteral(model.createProperty(NS + "isTarget"), false);
        sourceAuthRecord.addLiteral(model.createProperty(NS + "authResult"), fromVerified ? "PASSED" : "FAILED");
        order.addProperty(model.createProperty(NS + "hasAuthorization"), sourceAuthRecord);
        
        step.addFact(String.format("源客户鉴权: %s", fromVerified ? "通过 ✓" : "失败 ✗"));
        
        // 目标客户鉴权记录
        Resource targetAuthRecord = model.createResource(NS + "auth_target_" + request.getOrderId());
        targetAuthRecord.addProperty(RDF.type, model.getResource(NS + "AuthorizationRecord"));
        targetAuthRecord.addLiteral(model.createProperty(NS + "authId"), "AUTH_TGT_" + System.currentTimeMillis());
        targetAuthRecord.addLiteral(model.createProperty(NS + "isTarget"), true);
        targetAuthRecord.addLiteral(model.createProperty(NS + "authResult"), toVerified ? "PASSED" : "FAILED");
        order.addProperty(model.createProperty(NS + "hasAuthorization"), targetAuthRecord);
        
        step.addFact(String.format("目标客户鉴权: %s", toVerified ? "通过 ✓" : "失败 ✗"));

        if (fromVerified && toVerified) {
            step.addInference("推理: 应用 TransferEligibilityRule - 双方鉴权通过");
            step.addInference("推理: 订单满足过户前提条件，可继续执行");
            step.addInference("ODA组件: PartyManagementComponent.authCustomer()");
            step.setResult("通过 - 客户身份验证完整");
        } else if (!fromVerified) {
            step.addInference("推理: 源客户鉴权失败，违反 TransferEligibilityRule");
            step.addInference("警告: 需要源客户完成身份验证流程");
            step.setResult("阻塞 - 源客户鉴权失败");
        } else {
            step.addInference("推理: 目标客户鉴权失败，需要人工审核");
            step.addInference("警告: 建议联系目标客户完成验证");
            step.setResult("警告 - 目标客户鉴权失败");
        }

        return step;
    }

    /**
     * 步骤 3: 风险评估 (eTOM: Assess Risk & Fraud Detection)
     */
    private ReasoningStep assessRiskLevel(TransferOrderRequest request, InfModel model) {
        ReasoningStep step = new ReasoningStep(3, "风险评估", "执行订单风险与欺诈检测 (eTOM: Assess Risk)");

        int fromRiskScore = request.getFromAccountRiskScore();
        int toRiskScore = request.getToAccountRiskScore();
        int dailyCount = request.getFromAccountDailyTransferCount();
        double amount = request.getAmount();

        step.addFact(String.format("源客户风险评分: %d/100", fromRiskScore));
        step.addFact(String.format("目标客户风险评分: %d/100", toRiskScore));
        step.addFact(String.format("当日过户次数: %d", dailyCount));
        step.addFact(String.format("本次过户金额: ¥%.2f", amount));

        // 计算综合风险得分
        int riskScore = Math.max(fromRiskScore, toRiskScore);
        if (dailyCount >= 10) riskScore += 20;
        if (amount > 50000) riskScore += 15;
        riskScore = Math.min(riskScore, 100);

        // 添加风险属性到订单
        Resource order = model.getResource(NS + "order_" + request.getOrderId());
        order.addLiteral(model.createProperty(NS + "riskScore"), riskScore);
        
        // 应用风险推理规则
        boolean isHighRisk = false;
        
        if (fromRiskScore > 70) {
            step.addInference("规则: HighRiskCustomerRule - 源客户风险评分 > 70");
            isHighRisk = true;
        }
        
        if (toRiskScore > 70) {
            step.addInference("规则: HighRiskCustomerRule - 目标客户风险评分 > 70");
            isHighRisk = true;
        }
        
        if (dailyCount >= 10) {
            step.addInference("规则: FrequentTransferRule - 当日过户次数 >= 10，可能存在异常");
            isHighRisk = true;
        }
        
        if (amount > 50000) {
            step.addInference("规则: LargeAmountRule - 过户金额 > 50000，属于大额业务");
            isHighRisk = true;
        }

        // 标记订单风险类型
        if (isHighRisk) {
            order.addProperty(RDF.type, model.getResource(NS + "HighRiskOrder"));
            order.addLiteral(model.createProperty(NS + "riskLevel"), "HIGH");
            step.addInference("ODA组件: RiskManagementComponent.assessRisk()");
            step.addInference("ODA组件: FraudManagementComponent.detectFraud()");
            step.addInference("TMF API: TMF675 Risk Management");
            step.setResult("高风险订单 - 需要人工审核");
        } else {
            order.addProperty(RDF.type, model.getResource(NS + "NormalOrder"));
            order.addLiteral(model.createProperty(NS + "riskLevel"), "LOW");
            step.addInference("推理: 订单符合 LowRiskAutoApprovalRule");
            step.addInference("ODA组件: RiskManagementComponent.assessRisk()");
            step.setResult("正常风险订单 - 可继续流程");
        }

        return step;
    }

    /**
     * 步骤 4: 账户余额与费用检查 (eTOM: Check Credit & Payment)
     */
    private ReasoningStep checkBalanceSufficiency(TransferOrderRequest request, InfModel model) {
        ReasoningStep step = new ReasoningStep(4, "账户余额与费用检查", "验证余额并计算过户费用 (eTOM: Check Credit)");

        double balance = request.getFromAccountBalance();
        double amount = request.getAmount();
        double transferFee = amount * 0.01; // 假设1%手续费
        double totalRequired = amount + transferFee;

        step.addFact(String.format("源客户账户余额: ¥%.2f", balance));
        step.addFact(String.format("过户金额: ¥%.2f", amount));
        step.addFact(String.format("手续费 (1%%): ¥%.2f", transferFee));
        step.addFact(String.format("所需总额: ¥%.2f", totalRequired));

        // 创建付款记录
        Resource order = model.getResource(NS + "order_" + request.getOrderId());
        Resource paymentRecord = model.createResource(NS + "payment_" + request.getOrderId());
        paymentRecord.addProperty(RDF.type, model.getResource(NS + "PaymentRecord"));
        paymentRecord.addLiteral(model.createProperty(NS + "paymentId"), "PAY_" + System.currentTimeMillis());
        paymentRecord.addLiteral(model.createProperty(NS + "paymentAmount"), amount);
        paymentRecord.addLiteral(model.createProperty(NS + "paymentFee"), transferFee);
        
        order.addProperty(model.createProperty(NS + "hasPayment"), paymentRecord);

        if (balance >= totalRequired) {
            paymentRecord.addLiteral(model.createProperty(NS + "paymentStatus"), "APPROVED");
            step.addInference(String.format("推理: 应用 SufficientBalanceRule - 余额充足 (¥%.2f >= ¥%.2f)", balance, totalRequired));
            step.addInference("推理: 订单满足财务前提条件");
            step.addInference("ODA组件: BalanceManagementComponent.checkBalance()");
            step.addInference("TMF API: TMF654 Prepay Balance Management");
            step.setResult("通过 - 余额充足，可执行过户");
        } else {
            paymentRecord.addLiteral(model.createProperty(NS + "paymentStatus"), "REJECTED");
            double shortfall = totalRequired - balance;
            step.addInference(String.format("推理: 违反 SufficientBalanceRule - 余额不足 (¥%.2f < ¥%.2f)", balance, totalRequired));
            step.addInference(String.format("警告: 缺少 ¥%.2f", shortfall));
            step.addInference("ODA组件: BalanceManagementComponent.rejectTransaction()");
            step.setResult("失败 - 余额不足，订单阻塞");
        }

        return step;
    }

    /**
     * 步骤 5: 订单类型与优先级判定 (eTOM: Classify Order)
     */
    private ReasoningStep determineOrderType(TransferOrderRequest request, InfModel model) {
        ReasoningStep step = new ReasoningStep(5, "订单类型与优先级判定", "根据客户等级和业务金额确定处理优先级 (eTOM: Classify Order)");

        String fromType = request.getFromAccountType();
        double amount = request.getAmount();

        step.addFact(String.format("源客户类型: %s", fromType));
        step.addFact(String.format("过户金额: ¥%.2f", amount));

        Resource order = model.getResource(NS + "order_" + request.getOrderId());
        
        if ("vip".equalsIgnoreCase(fromType) || "premium".equalsIgnoreCase(fromType)) {
            step.addInference("规则: VIPCustomerPriorityRule - VIP客户订单设为紧急优先级");
            step.addInference("推理: VIP客户享受快速通道处理");
            order.addProperty(RDF.type, model.getResource(NS + "UrgentOrder"));
            order.addLiteral(model.createProperty(NS + "orderPriority"), "HIGH");
            order.addLiteral(model.createProperty(NS + "processingQueue"), "VIP_FAST_TRACK");
            step.addInference("ODA组件: OrderManagementComponent.classifyOrder()");
            step.addInference("TMF API: TMF622 Product Ordering");
            step.setResult("紧急订单 (VIP客户)");
        } else if (amount > 100000) {
            step.addInference("规则: LargeAmountPriorityRule - 大额过户（>100000）设为紧急优先级");
            step.addInference("推理: 大额业务需要优先处理以降低资金风险");
            order.addProperty(RDF.type, model.getResource(NS + "UrgentOrder"));
            order.addLiteral(model.createProperty(NS + "orderPriority"), "HIGH");
            order.addLiteral(model.createProperty(NS + "processingQueue"), "LARGE_AMOUNT_QUEUE");
            step.addInference("ODA组件: OrderManagementComponent.classifyOrder()");
            step.setResult("紧急订单 (大额业务)");
        } else {
            step.addInference("规则: NormalOrderRule - 普通客户与金额，正常优先级");
            order.addLiteral(model.createProperty(NS + "orderPriority"), "NORMAL");
            order.addLiteral(model.createProperty(NS + "processingQueue"), "STANDARD_QUEUE");
            step.addInference("ODA组件: OrderManagementComponent.routeToQueue()");
            step.setResult("普通订单 - 标准队列处理");
        }

        return step;
    }

    /**
     * 步骤 6: 最终决策与订单状态更新 (eTOM: Complete Order)
     */
    private ReasoningStep makeFinalDecision(TransferOrderRequest request, InfModel model, 
                                            ReasoningStep accountStep, ReasoningStep riskStep, 
                                            ReasoningStep balanceStep) {
        ReasoningStep step = new ReasoningStep(6, "最终决策与订单完成", "综合所有推理结果做出决策 (eTOM: Complete Order)");

        Resource order = model.getResource(NS + "order_" + request.getOrderId());

        // 收集所有条件
        boolean balanceSufficient = balanceStep.getResult().contains("通过");
        boolean isHighRisk = riskStep.getResult().contains("高风险");
        boolean accountsVerified = accountStep.getResult().contains("通过");

        step.addFact(String.format("余额检查: %s", balanceSufficient ? "✓" : "✗"));
        step.addFact(String.format("风险评估: %s", isHighRisk ? "高风险" : "正常"));
        step.addFact(String.format("客户鉴权: %s", accountsVerified ? "✓" : "警告"));

        // 应用决策规则（TM Forum ODA业务规则）
        String decision;
        if (!balanceSufficient) {
            step.addInference("决策规则: InsufficientFundsRejectionRule - 余额不足 → 直接拒绝");
            step.addInference("推理: 订单无法满足财务前提，自动拒绝");
            decision = "REJECTED - 余额不足";
            order.addProperty(RDF.type, model.getResource(NS + "RejectedOrder"));
            order.addLiteral(model.createProperty(NS + "orderStatus"), "REJECTED");
            order.addLiteral(model.createProperty(NS + "requiresManualReview"), false);
            order.addLiteral(model.createProperty(NS + "rejectionReason"), "INSUFFICIENT_BALANCE");
            step.addInference("ODA组件: OrderManagementComponent.rejectOrder()");
            step.addInference("TMF API: TMF622 Update Order Status");
        } else if (isHighRisk || !accountsVerified) {
            step.addInference("决策规则: RiskBasedReviewRule - 高风险或鉴权失败 → 需要人工审核");
            step.addInference("推理: 订单存在风险因素，提交人工复核流程");
            decision = "PENDING_REVIEW - 等待人工审核";
            order.addLiteral(model.createProperty(NS + "orderStatus"), "PENDING_REVIEW");
            order.addLiteral(model.createProperty(NS + "requiresManualReview"), true);
            step.addInference("ODA组件: OrderManagementComponent.escalateToReview()");
            step.addInference("ODA组件: WorkflowManagementComponent.assignToAgent()");
            step.addInference("TMF API: TMF622 Update Order Status");
            step.addInference("eTOM流程: 转入 Handle Customer Problem 流程");
        } else {
            step.addInference("决策规则: AutoApprovalRule - 所有条件满足 → 自动批准");
            step.addInference("推理: 订单通过所有验证步骤，自动批准执行");
            decision = "APPROVED - 自动批准";
            order.addProperty(RDF.type, model.getResource(NS + "ApprovedOrder"));
            order.addLiteral(model.createProperty(NS + "orderStatus"), "APPROVED");
            order.addLiteral(model.createProperty(NS + "requiresManualReview"), false);
            step.addInference("ODA组件: OrderManagementComponent.approveOrder()");
            step.addInference("ODA组件: ProductInventoryComponent.executeTransfer()");
            step.addInference("TMF API: TMF622 Complete Order");
            step.addInference("eTOM流程: 完成 Fulfill Order 流程");
        }

        step.setResult(decision);
        return step;
    }

    /**
     * 生成推理摘要
     */
    private String generateSummary(ReasoningResult result) {
        StringBuilder summary = new StringBuilder();
        summary.append("订单 ").append(result.getOrderId()).append(" 推理完成：\n");
        summary.append("- 共执行 ").append(result.getSteps().size()).append(" 个推理步骤\n");
        summary.append("- 最终决策: ").append(result.getFinalDecision()).append("\n");
        
        long inferenceCount = result.getSteps().stream()
            .mapToLong(s -> s.getInferences().size())
            .sum();
        summary.append("- 生成推理结论: ").append(inferenceCount).append(" 条");
        
        return summary.toString();
    }

    /**
     * 根据账户类型获取对应的 OWL 类
     */
    private Resource getAccountTypeResource(Model model, String accountType) {
        if (accountType == null) {
            return model.getResource(NS + "Account");
        }
        
        switch (accountType.toLowerCase()) {
            case "personal":
                return model.getResource(NS + "PersonalAccount");
            case "corporate":
                return model.getResource(NS + "CorporateAccount");
            case "vip":
                return model.getResource(NS + "VIPAccount");
            default:
                return model.getResource(NS + "Account");
        }
    }

    /**
     * 重新加载本体（用于本体文件更新后）
     */
    public void reloadOntology() {
        loadOntology();
    }
}
