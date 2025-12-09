package com.example.loanjena.service;

import com.example.loanjena.model.ReasoningStep;
import com.example.loanjena.model.TransferBusinessRequest;
import com.example.loanjena.model.TransferReasoningResult;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BSS4.0过户业务推理服务
 * 实现8步骤过户流程的智能推理和决策
 * 
 * 业务流程：
 * 步骤1: 定位源客户 - 查询并鉴权源客户
 * 步骤2: 过户号码选择 - 查询产品实例并选择过户号码
 * 步骤3: 创建客户订单 - 创建订单（可回退到步骤1）
 * 步骤4: 过户业务初始化 - 初始化产品实例受理
 * 步骤5: 公共属性初始化 - 初始化公共属性
 * 步骤6: 目标客户确认 - 查询并鉴权目标客户
 * 步骤7: 订单保存 - 提交并保存订单
 * 步骤8: 订单确认 - 收银台确认订单
 */
@Service
public class TransferBusinessReasoningService {

    private static final String NS = "https://iwhalecloud.com/ontology/transfer#";
    private static final String TMFORUM_NS = "https://tmforum.org/oda/";
    private OntModel ontologyModel;
    private Reasoner reasoner;
    
    // 步骤名称映射
    private static final String[] STEP_NAMES = {
        "",  // 占位，索引从1开始
        "定位源客户",
        "过户号码选择",
        "创建客户订单",
        "过户业务初始化",
        "公共属性初始化",
        "目标客户确认",
        "订单保存",
        "订单确认"
    };

    public TransferBusinessReasoningService() {
        loadOntology();
    }

    /**
     * 加载本体文件
     */
    private void loadOntology() {
        try {
            ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
            
            ClassPathResource resource = new ClassPathResource("transfer_order_ontology.owl");
            InputStream inputStream = resource.getInputStream();
            
            ontologyModel.read(inputStream, null, "TURTLE");
            System.out.println("✓ BSS4.0 过户本体加载成功 - 8步骤流程");
            System.out.println("✓ 支持步骤推理、状态追踪、回退控制");
            
            reasoner = ReasonerRegistry.getOWLReasoner();
            reasoner = reasoner.bindSchema(ontologyModel);
            
        } catch (Exception e) {
            System.err.println("✗ 本体文件加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行过户业务推理
     * 智能体会根据当前状态推理出：
     * 1. 当前在哪一步
     * 2. 总共多少步
     * 3. 下一步做什么
     * 4. 是否可以回退
     */
    public TransferReasoningResult performReasoning(TransferBusinessRequest request) {
        TransferReasoningResult result = new TransferReasoningResult();
        result.setOrderId(request.getOrderId());
        result.setTotalSteps(8);
        
        // 初始化步骤完成状态
        TransferReasoningResult.StepCompletionStatus status = 
            new TransferReasoningResult.StepCompletionStatus();
        result.setCompletionStatus(status);
        
        // 创建推理模型
        InfModel infModel = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        infModel.setNsPrefix("transfer", NS);
        
        // 创建订单实例
        Resource order = createOrderInstance(request, infModel);
        
        // 分析当前步骤状态
        int currentStep = analyzeCurrentStep(request, status, result);
        result.setCurrentStep(currentStep);
        result.setCurrentStepName(STEP_NAMES[currentStep]);
        
        // 更新订单的当前步骤
        order.addLiteral(infModel.createProperty(NS + "currentStepNumber"), currentStep);
        order.addLiteral(infModel.createProperty(NS + "totalSteps"), 8);
        
        // 检查是否可以回退（步骤3可以回退到步骤1）
        boolean canRollback = (currentStep == 3);
        result.setCanRollback(canRollback);
        if (canRollback) {
            result.setRollbackToStep(1);
            order.addLiteral(infModel.createProperty(NS + "canRollback"), true);
            order.addLiteral(infModel.createProperty(NS + "rollbackToStep"), 1);
        }
        
        // 执行各步骤的推理
        ReasoningStep step1 = executeStep1(request, infModel, order);
        result.addStep(step1);
        
        ReasoningStep step2 = executeStep2(request, infModel, order);
        result.addStep(step2);
        
        ReasoningStep step3 = executeStep3(request, infModel, order);
        result.addStep(step3);
        
        ReasoningStep step4 = executeStep4(request, infModel, order);
        result.addStep(step4);
        
        ReasoningStep step5 = executeStep5(request, infModel, order);
        result.addStep(step5);
        
        ReasoningStep step6 = executeStep6(request, infModel, order);
        result.addStep(step6);
        
        ReasoningStep step7 = executeStep7(request, infModel, order);
        result.addStep(step7);
        
        ReasoningStep step8 = executeStep8(request, infModel, order);
        result.addStep(step8);
        
        // 推理下一步
        ReasoningStep nextStepAnalysis = analyzeNextStep(request, infModel, order, currentStep, status);
        result.addStep(nextStepAnalysis);
        
        // 设置下一步信息
        int nextStep = inferNextStep(currentStep, status);
        result.setNextStep(nextStep);
        if (nextStep <= 8) {
            result.setNextStepName(STEP_NAMES[nextStep]);
            result.setNextStepGuide(getStepGuide(nextStep, request));
            order.addLiteral(infModel.createProperty(NS + "nextStepNumber"), nextStep);
        }
        
        // 生成最终决策
        String decision = makeFinalDecision(currentStep, status, request);
        result.setFinalDecision(decision);
        
        // 生成智能体决策说明
        result.setAgentDecisionExplanation(generateAgentExplanation(currentStep, nextStep, status, canRollback));
        
        // 生成摘要
        result.setSummary(generateSummary(result));
        
        return result;
    }

    /**
     * 创建订单实例
     */
    private Resource createOrderInstance(TransferBusinessRequest request, InfModel model) {
        Resource order = model.createResource(NS + "order_" + request.getOrderId());
        order.addProperty(RDF.type, model.getResource(NS + "TransferOrder"));
        order.addLiteral(model.createProperty(NS + "orderId"), request.getOrderId());
        
        if (request.getCustOrderId() != null) {
            order.addLiteral(model.createProperty(NS + "custOrderId"), request.getCustOrderId());
        }
        
        order.addLiteral(model.createProperty(NS + "orderStatus"), "IN_PROGRESS");
        
        return order;
    }

    /**
     * 分析当前步骤状态
     * 智能体根据请求数据推理出当前处于哪个步骤
     */
    private int analyzeCurrentStep(TransferBusinessRequest request, 
                                   TransferReasoningResult.StepCompletionStatus status,
                                   TransferReasoningResult result) {
        
        // 步骤1: 源客户定位完成
        if (request.getSourceCustId() != null && request.getSourceAuthPassed() != null) {
            status.setStep1Completed(request.getSourceAuthPassed());
            result.addBusinessRule("Rule: 源客户鉴权完成 -> 步骤1完成");
        }
        
        // 步骤2: 号码选择完成
        if (request.getSelectedAccNum() != null && request.getSelectedProdInstId() != null) {
            status.setStep2Completed(true);
            result.addBusinessRule("Rule: 过户号码已选择 -> 步骤2完成");
        }
        
        // 步骤3: 订单创建完成
        if (request.getCustOrderId() != null) {
            status.setStep3Completed(true);
            result.addBusinessRule("Rule: 客户订单已创建 -> 步骤3完成");
        }
        
        // 步骤4: 业务初始化完成
        if (request.getBusinessInitSuccess() != null && request.getBusinessInitSuccess()) {
            status.setStep4Completed(true);
            result.addBusinessRule("Rule: 业务初始化成功 -> 步骤4完成");
        }
        
        // 步骤5: 公共属性初始化完成
        if (request.getCommonAttrInitSuccess() != null && request.getCommonAttrInitSuccess()) {
            status.setStep5Completed(true);
            result.addBusinessRule("Rule: 公共属性初始化成功 -> 步骤5完成");
        }
        
        // 步骤6: 目标客户确认完成
        if (request.getTargetCustId() != null && request.getTargetAuthPassed() != null) {
            status.setStep6Completed(request.getTargetAuthPassed());
            result.addBusinessRule("Rule: 目标客户鉴权完成 -> 步骤6完成");
        }
        
        // 步骤7: 订单保存完成
        if (request.getOrderSaved() != null && request.getOrderSaved()) {
            status.setStep7Completed(true);
            result.addBusinessRule("Rule: 订单已保存 -> 步骤7完成");
        }
        
        // 步骤8: 订单确认完成
        if (request.getOrderConfirmed() != null && request.getOrderConfirmed()) {
            status.setStep8Completed(true);
            result.addBusinessRule("Rule: 订单已确认 -> 步骤8完成 (流程结束)");
        }
        
        // 推理当前步骤：最后一个完成的步骤 + 1
        int current = status.getCompletedCount() + 1;
        if (current > 8) current = 8;  // 最多到步骤8
        
        return current;
    }

    /**
     * 推理下一步骤
     */
    private int inferNextStep(int currentStep, TransferReasoningResult.StepCompletionStatus status) {
        // 如果当前步骤未完成，下一步就是完成当前步骤
        if (!status.isStepCompleted(currentStep) && currentStep <= 8) {
            return currentStep;
        }
        
        // 如果当前步骤已完成，下一步是 currentStep + 1
        int next = currentStep + 1;
        if (next > 8) {
            return 8;  // 已经是最后一步
        }
        
        return next;
    }

    /**
     * 步骤1: 定位源客户
     */
    private ReasoningStep executeStep1(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(1, "定位源客户", 
            "查询源客户列表并进行鉴权 (eTOM: Identify & Validate Customer)");
        
        if (request.getSourceSearchValue() != null) {
            step.addFact(String.format("查询值: %s", request.getSourceSearchValue()));
            step.addFact(String.format("查询类型: %s (%s)", 
                request.getSourceSearchType(),
                getSearchTypeLabel(request.getSourceSearchType())));
        }
        
        if (request.getSourceCustId() != null) {
            // 创建源客户实例
            Resource sourceCustomer = model.createResource(NS + "customer_" + request.getSourceCustId());
            sourceCustomer.addProperty(RDF.type, model.getResource(NS + "SourceCustomer"));
            sourceCustomer.addLiteral(model.createProperty(NS + "custId"), request.getSourceCustId());
            
            if (request.getSourceCustName() != null) {
                sourceCustomer.addLiteral(model.createProperty(NS + "custName"), request.getSourceCustName());
                step.addFact(String.format("✓ 源客户: %s (%s)", 
                    request.getSourceCustName(), request.getSourceCustId()));
            }
            
            if (request.getSourceCertNumber() != null) {
                sourceCustomer.addLiteral(model.createProperty(NS + "certNumber"), request.getSourceCertNumber());
                step.addFact(String.format("✓ 证件号: %s", maskCertNumber(request.getSourceCertNumber())));
            }
            
            order.addProperty(model.createProperty(NS + "hasSourceCustomer"), sourceCustomer);
            
            // 1.2 源客户鉴权
            if (request.getSourceAuthPassed() != null) {
                Resource authRecord = model.createResource(NS + "auth_source_" + request.getOrderId());
                authRecord.addProperty(RDF.type, model.getResource(NS + "AuthorizationRecord"));
                authRecord.addLiteral(model.createProperty(NS + "isTarget"), false);
                authRecord.addLiteral(model.createProperty(NS + "authResult"), 
                    request.getSourceAuthPassed() ? "PASSED" : "FAILED");
                authRecord.addLiteral(model.createProperty(NS + "searchType"), "idCard");
                
                order.addProperty(model.createProperty(NS + "hasAuthorization"), authRecord);
                
                if (request.getSourceAuthPassed()) {
                    step.addInference("推理: TransferEligibilityRule - 源客户鉴权通过");
                    step.addInference("ODA组件: PartyManagementComponent.authCustomer()");
                    step.addInference("TMF API: ICustomerLocationService.listCustomers");
                    step.setResult("✓ 完成 - 源客户定位并鉴权成功");
                } else {
                    step.addInference("推理: 源客户鉴权失败，需要重新鉴权");
                    step.setResult("✗ 失败 - 源客户鉴权未通过");
                }
            } else {
                step.setResult("⏳ 等待 - 需要进行源客户鉴权");
            }
        } else {
            step.setResult("⏳ 待执行 - 需要查询源客户");
            step.addInference("提示: 调用 ICustomerLocationService.listCustomers 查询客户");
            step.addInference("提示: 调用 ICustomerAuthService.authCustomer 进行鉴权");
        }
        
        return step;
    }

    /**
     * 步骤2: 过户号码选择
     */
    private ReasoningStep executeStep2(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(2, "过户号码选择", 
            "查询客户产品实例并选择过户号码 (eTOM: Capture Order)");
        
        if (request.getTotalSubscriptionCount() != null) {
            step.addFact(String.format("产品实例总数: %d", request.getTotalSubscriptionCount()));
            
            if (request.getTotalSubscriptionCount() > 1) {
                step.addInference("规则: MultiInstanceSelectionRule - 产品实例数 > 1, 需要客户选择");
                step.addInference("推理: 列出所有可过户号码，等待客户选择");
            } else if (request.getTotalSubscriptionCount() == 1) {
                step.addInference("推理: 仅有1个产品实例，询问客户是否对该号码过户");
            }
        }
        
        if (request.getSelectedAccNum() != null && request.getSelectedProdInstId() != null) {
            // 创建可转移订阅实例
            Resource subscription = model.createResource(NS + "subscription_" + request.getSelectedProdInstId());
            subscription.addProperty(RDF.type, model.getResource(NS + "TransferableSubscription"));
            subscription.addLiteral(model.createProperty(NS + "accNum"), request.getSelectedAccNum());
            subscription.addLiteral(model.createProperty(NS + "prodInstId"), request.getSelectedProdInstId());
            
            if (request.getSelectedProdName() != null) {
                subscription.addLiteral(model.createProperty(NS + "prodName"), request.getSelectedProdName());
            }
            
            order.addProperty(model.createProperty(NS + "changesSubscription"), subscription);
            
            step.addFact(String.format("✓ 已选择号码: %s", request.getSelectedAccNum()));
            step.addFact(String.format("✓ 产品实例ID: %s", request.getSelectedProdInstId()));
            
            step.addInference("ODA组件: ServiceConfigurationComponent.getProductInstance()");
            step.addInference("TMF API: ICustomerLocationService.getCustProdInst");
            step.setResult("✓ 完成 - 过户号码已选择");
        } else {
            step.setResult("⏳ 待执行 - 需要查询产品实例并选择号码");
            step.addInference("提示: 调用 ICustomerLocationService.getCustProdInst");
        }
        
        return step;
    }

    /**
     * 步骤3: 创建客户订单
     */
    private ReasoningStep executeStep3(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(3, "创建客户订单", 
            "调用IOrderMgrService创建客户订单 (eTOM: Create Customer Order)");
        
        step.addFact("⚠️ 特别提示: 此步骤可回退到步骤1重新开始");
        
        if (request.getCustOrderId() != null) {
            order.addLiteral(model.createProperty(NS + "custOrderId"), request.getCustOrderId());
            
            step.addFact(String.format("✓ 客户订单号: %s", request.getCustOrderId()));
            
            if (request.getSourceCustId() != null) {
                step.addFact(String.format("入参: cust_id=%s", request.getSourceCustId()));
            }
            if (request.getSourceCertType() != null) {
                step.addFact(String.format("入参: certType=%s", request.getSourceCertType()));
            }
            if (request.getSourceCertNumber() != null) {
                step.addFact(String.format("入参: certNum=%s", maskCertNumber(request.getSourceCertNumber())));
            }
            
            step.addInference("ODA组件: OrderCaptureComponent.createOrder()");
            step.addInference("TMF API: IOrderMgrService.createCustomerOrder");
            step.addInference("推理: 订单创建成功，可继续后续步骤");
            step.addInference("推理: 如需修改源客户或号码，可调用回退功能返回步骤1");
            step.setResult("✓ 完成 - 客户订单已创建");
        } else {
            step.setResult("⏳ 待执行 - 需要创建客户订单");
            step.addInference("提示: 调用 IOrderMgrService.createCustomerOrder");
            step.addInference("提示: 入参取步骤1的custId、certType、certNum");
        }
        
        return step;
    }

    /**
     * 步骤4: 过户业务初始化
     */
    private ReasoningStep executeStep4(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(4, "过户业务初始化", 
            "初始化产品实例受理 (eTOM: Service Activation & Configuration)");
        
        step.addFact("参数: serviceOfferId = 2549 (固定值)");
        
        if (request.getBusinessInitSuccess() != null && request.getBusinessInitSuccess()) {
            step.addFact("✓ 业务初始化成功");
            
            if (request.getSourceCustId() != null) {
                step.addFact(String.format("入参: custId=%s", request.getSourceCustId()));
            }
            if (request.getSelectedProdInstId() != null) {
                step.addFact(String.format("入参: prodInstId=%s", request.getSelectedProdInstId()));
            }
            if (request.getCustOrderId() != null) {
                step.addFact(String.format("入参: custOrderId=%s", request.getCustOrderId()));
            }
            
            step.addInference("ODA组件: ServiceConfigurationComponent.initAcceptance()");
            step.addInference("TMF API: IAppCardAcceptOptService.smartInitProdInstAcceptance");
            step.setResult("✓ 完成 - 业务初始化成功");
        } else {
            step.setResult("⏳ 待执行 - 需要初始化过户业务");
            step.addInference("提示: 调用 IAppCardAcceptOptService.smartInitProdInstAcceptance");
            step.addInference("提示: serviceOfferId=2549, custId、prodInstId、custOrderId取前面步骤的值");
        }
        
        return step;
    }

    /**
     * 步骤5: 公共属性初始化
     */
    private ReasoningStep executeStep5(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(5, "公共属性初始化", 
            "初始化公共属性 (eTOM: Configure Service Attributes)");
        
        if (request.getCommonAttrInitSuccess() != null && request.getCommonAttrInitSuccess()) {
            step.addFact("✓ 公共属性初始化成功");
            step.addInference("ODA组件: ServiceConfigurationComponent.initCommonAttr()");
            step.addInference("TMF API: IAppCardAcceptOptService.initCommonAttr");
            step.setResult("✓ 完成 - 公共属性初始化成功");
        } else {
            step.setResult("⏳ 待执行 - 需要初始化公共属性");
            step.addInference("提示: 调用 IAppCardAcceptOptService.initCommonAttr");
        }
        
        return step;
    }

    /**
     * 步骤6: 目标客户确认
     */
    private ReasoningStep executeStep6(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(6, "目标客户确认", 
            "查询并鉴权目标客户 (eTOM: Identify & Validate Target Customer)");
        
        if (request.getTargetSearchValue() != null) {
            step.addFact(String.format("查询值: %s", request.getTargetSearchValue()));
            step.addFact(String.format("查询类型: %s (%s)", 
                request.getTargetSearchType(),
                getSearchTypeLabel(request.getTargetSearchType())));
        }
        
        if (request.getTargetCustId() != null) {
            // 创建目标客户实例
            Resource targetCustomer = model.createResource(NS + "customer_" + request.getTargetCustId());
            targetCustomer.addProperty(RDF.type, model.getResource(NS + "TargetCustomer"));
            targetCustomer.addLiteral(model.createProperty(NS + "custId"), request.getTargetCustId());
            
            if (request.getTargetCustName() != null) {
                targetCustomer.addLiteral(model.createProperty(NS + "custName"), request.getTargetCustName());
                step.addFact(String.format("✓ 目标客户: %s (%s)", 
                    request.getTargetCustName(), request.getTargetCustId()));
            }
            
            if (request.getTargetCertNumber() != null) {
                targetCustomer.addLiteral(model.createProperty(NS + "certNumber"), request.getTargetCertNumber());
                step.addFact(String.format("✓ 证件号: %s", maskCertNumber(request.getTargetCertNumber())));
            }
            
            order.addProperty(model.createProperty(NS + "hasTargetCustomer"), targetCustomer);
            
            // 6.2 目标客户鉴权
            if (request.getTargetAuthPassed() != null) {
                Resource authRecord = model.createResource(NS + "auth_target_" + request.getOrderId());
                authRecord.addProperty(RDF.type, model.getResource(NS + "AuthorizationRecord"));
                authRecord.addLiteral(model.createProperty(NS + "isTarget"), true);
                authRecord.addLiteral(model.createProperty(NS + "authResult"), 
                    request.getTargetAuthPassed() ? "PASSED" : "FAILED");
                authRecord.addLiteral(model.createProperty(NS + "searchType"), "idCard");
                
                order.addProperty(model.createProperty(NS + "hasAuthorization"), authRecord);
                
                if (request.getTargetAuthPassed()) {
                    step.addInference("推理: TransferEligibilityRule - 目标客户鉴权通过");
                    step.addInference("推理: 源客户和目标客户均鉴权通过，满足过户条件");
                    step.addInference("ODA组件: PartyManagementComponent.authCustomer()");
                    step.addInference("TMF API: ICustomerLocationService.listCustomers");
                    step.setResult("✓ 完成 - 目标客户确认并鉴权成功");
                } else {
                    step.addInference("推理: 目标客户鉴权失败，需要重新鉴权");
                    step.setResult("✗ 失败 - 目标客户鉴权未通过");
                }
            } else {
                step.setResult("⏳ 等待 - 需要进行目标客户鉴权");
            }
        } else {
            step.setResult("⏳ 待执行 - 需要查询目标客户");
            step.addInference("提示: 调用 ICustomerLocationService.listCustomers 查询客户");
            step.addInference("提示: 调用 ICustomerAuthService.authCustomer 进行鉴权 (isTarget=true)");
        }
        
        return step;
    }

    /**
     * 步骤7: 订单保存
     */
    private ReasoningStep executeStep7(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(7, "订单保存", 
            "提交并保存订单 (eTOM: Complete Order Capture)");
        
        if (request.getOrderSaved() != null && request.getOrderSaved()) {
            step.addFact("✓ 订单已保存");
            
            if (request.getOrderSaveTime() != null) {
                step.addFact(String.format("保存时间: %s", request.getOrderSaveTime()));
            }
            
            step.addFact("详细内容:");
            step.addFact(String.format("  - 源客户: %s (%s)", 
                request.getSourceCustName(), request.getSourceCustId()));
            step.addFact(String.format("  - 目标客户: %s (%s)", 
                request.getTargetCustName(), request.getTargetCustId()));
            step.addFact(String.format("  - 过户号码: %s", request.getSelectedAccNum()));
            step.addFact(String.format("  - 产品实例: %s", request.getSelectedProdInstId()));
            step.addFact(String.format("  - 客户订单号: %s", request.getCustOrderId()));
            
            order.addLiteral(model.createProperty(NS + "orderStatus"), "SAVED");
            
            step.addInference("ODA组件: OrderCaptureComponent.saveOrder()");
            step.addInference("TMF API: BusinessAcceptService.saveOrder");
            step.addInference("推理: 订单保存成功，等待收银台确认");
            step.setResult("✓ 完成 - 订单已保存，待确认");
        } else {
            step.setResult("⏳ 待执行 - 需要保存订单");
            step.addInference("提示: 调用 BusinessAcceptService.saveOrder(reqMap)");
            step.addInference("提示: reqMap包含完整的订单信息");
        }
        
        return step;
    }

    /**
     * 步骤8: 订单确认
     */
    private ReasoningStep executeStep8(TransferBusinessRequest request, InfModel model, Resource order) {
        ReasoningStep step = new ReasoningStep(8, "订单确认", 
            "收银台确认订单 (eTOM: Billing & Payment)");
        
        if (request.getOrderConfirmed() != null && request.getOrderConfirmed()) {
            step.addFact("✓ 订单已确认");
            
            if (request.getOrderConfirmTime() != null) {
                step.addFact(String.format("确认时间: %s", request.getOrderConfirmTime()));
            }
            
            // 创建支付记录
            Resource paymentRecord = model.createResource(NS + "payment_" + request.getOrderId());
            paymentRecord.addProperty(RDF.type, model.getResource(NS + "PaymentRecord"));
            paymentRecord.addLiteral(model.createProperty(NS + "paymentStatus"), "CONFIRMED");
            
            if (request.getPaymentAmount() != null) {
                paymentRecord.addLiteral(model.createProperty(NS + "amount"), request.getPaymentAmount());
                step.addFact(String.format("缴费金额: ¥%.2f", request.getPaymentAmount()));
            }
            
            if (request.getPayChannel() != null) {
                paymentRecord.addLiteral(model.createProperty(NS + "payChannel"), request.getPayChannel());
                step.addFact(String.format("支付渠道: %s", request.getPayChannel()));
            }
            
            if (request.getReceiptNo() != null) {
                paymentRecord.addLiteral(model.createProperty(NS + "receiptNo"), request.getReceiptNo());
                step.addFact(String.format("票据号: %s", request.getReceiptNo()));
            }
            
            order.addProperty(model.createProperty(NS + "hasPayment"), paymentRecord);
            order.addLiteral(model.createProperty(NS + "orderStatus"), "CONFIRMED");
            
            step.addInference("规则: PaymentConfirmationRule - 订单确认完成");
            step.addInference("ODA组件: BillingManagementComponent.confirmPayment()");
            step.addInference("TMF API: CashierTaiService.updateConfirm");
            step.addInference("推理: 过户流程全部完成 ✓");
            step.setResult("✓ 完成 - 订单已确认，过户流程结束");
        } else {
            step.setResult("⏳ 待执行 - 需要确认订单");
            step.addInference("提示: 调用 CashierTaiService.updateConfirm");
            step.addInference("提示: 完成收银台缴费流程");
        }
        
        return step;
    }

    /**
     * 分析下一步操作
     */
    private ReasoningStep analyzeNextStep(TransferBusinessRequest request, InfModel model, Resource order,
                                         int currentStep, TransferReasoningResult.StepCompletionStatus status) {
        ReasoningStep step = new ReasoningStep(9, "智能体决策分析", 
            "综合推理当前状态、下一步操作和回退选项");
        
        step.addFact(String.format("当前步骤: 步骤%d - %s", currentStep, STEP_NAMES[currentStep]));
        step.addFact(String.format("总步骤数: 8"));
        step.addFact(String.format("已完成: %d/%d 步", status.getCompletedCount(), 8));
        
        // 检查回退条件
        if (currentStep == 3) {
            step.addInference("推理: 当前在步骤3，支持回退到步骤1");
            step.addInference("回退场景: 如需修改源客户或过户号码，可执行回退操作");
            step.addFact("⚠️ 回退功能: 可回退到步骤1重新选择源客户");
        }
        
        // 推理下一步
        int nextStep = inferNextStep(currentStep, status);
        
        if (nextStep <= 8) {
            step.addInference(String.format("推理: 下一步为步骤%d - %s", nextStep, STEP_NAMES[nextStep]));
            step.addInference(String.format("执行提示: %s", getStepGuide(nextStep, request)));
            step.setResult(String.format("继续执行步骤%d", nextStep));
        } else {
            step.addInference("推理: 所有步骤已完成");
            step.setResult("流程结束");
        }
        
        return step;
    }

    /**
     * 生成最终决策
     */
    private String makeFinalDecision(int currentStep, 
                                     TransferReasoningResult.StepCompletionStatus status,
                                     TransferBusinessRequest request) {
        
        if (status.isStep8Completed()) {
            return "SUCCESS - 过户流程全部完成，订单已确认";
        }
        
        if (currentStep == 3 && request.getNeedRollback() != null && request.getNeedRollback()) {
            return "ROLLBACK - 从步骤3回退到步骤1，重新开始流程";
        }
        
        int nextStep = inferNextStep(currentStep, status);
        return String.format("IN_PROGRESS - 当前步骤%d/%d，下一步: 步骤%d - %s", 
            currentStep, 8, nextStep, STEP_NAMES[nextStep]);
    }

    /**
     * 生成智能体决策说明
     */
    private String generateAgentExplanation(int currentStep, int nextStep, 
                                            TransferReasoningResult.StepCompletionStatus status,
                                            boolean canRollback) {
        StringBuilder explanation = new StringBuilder();
        
        explanation.append("【智能体推理结果】\n");
        explanation.append(String.format("当前位置: 步骤%d - %s\n", currentStep, STEP_NAMES[currentStep]));
        explanation.append(String.format("完成进度: %d/8 步\n", status.getCompletedCount()));
        explanation.append(String.format("下一步骤: 步骤%d - %s\n", nextStep, STEP_NAMES[nextStep]));
        
        if (canRollback) {
            explanation.append("\n【回退选项】\n");
            explanation.append("✓ 当前支持回退到步骤1\n");
            explanation.append("✓ 回退后可重新选择源客户和过户号码\n");
        }
        
        explanation.append("\n【推理依据】\n");
        explanation.append("✓ 基于TM Forum ODA本体模型\n");
        explanation.append("✓ 应用eTOM业务流程规范\n");
        explanation.append("✓ 执行BSS4.0业务规则引擎\n");
        
        return explanation.toString();
    }

    /**
     * 生成摘要
     */
    private String generateSummary(TransferReasoningResult result) {
        return String.format(
            "订单 %s 推理完成: 当前步骤 %d/%d, 下一步: 步骤%d, 已应用 %d 条业务规则, 调用 %d 个ODA组件",
            result.getOrderId(),
            result.getCurrentStep(),
            result.getTotalSteps(),
            result.getNextStep(),
            result.getBusinessRulesApplied().size(),
            result.getOdaComponentCalls().size()
        );
    }

    /**
     * 获取步骤指导
     */
    private String getStepGuide(int stepNumber, TransferBusinessRequest request) {
        switch (stepNumber) {
            case 1:
                return "调用 ICustomerLocationService.listCustomers 查询源客户，然后调用 ICustomerAuthService.authCustomer 进行鉴权";
            case 2:
                return "调用 ICustomerLocationService.getCustProdInst 查询产品实例，让客户选择过户号码";
            case 3:
                return "调用 IOrderMgrService.createCustomerOrder 创建客户订单 (此步骤可回退到步骤1)";
            case 4:
                return "调用 IAppCardAcceptOptService.smartInitProdInstAcceptance 初始化过户业务 (serviceOfferId=2549)";
            case 5:
                return "调用 IAppCardAcceptOptService.initCommonAttr 初始化公共属性";
            case 6:
                return "调用 ICustomerLocationService.listCustomers 查询目标客户，然后调用 ICustomerAuthService.authCustomer 进行鉴权 (isTarget=true)";
            case 7:
                return "调用 BusinessAcceptService.saveOrder 保存订单";
            case 8:
                return "调用 CashierTaiService.updateConfirm 确认订单";
            default:
                return "流程已完成";
        }
    }

    /**
     * 获取查询类型标签
     */
    private String getSearchTypeLabel(String searchType) {
        if (searchType == null) return "未知";
        switch (searchType.toUpperCase()) {
            case "B": return "客户名称";
            case "C": return "证件号码";
            case "D": return "业务号码";
            default: return "未知类型";
        }
    }

    /**
     * 脱敏证件号码
     */
    private String maskCertNumber(String certNumber) {
        if (certNumber == null || certNumber.length() < 8) {
            return certNumber;
        }
        int len = certNumber.length();
        return certNumber.substring(0, 4) + "********" + certNumber.substring(len - 4);
    }

    /**
     * 重新加载本体
     */
    public void reloadOntology() {
        loadOntology();
    }
}
