package com.example.loanjena.controller;

import com.example.loanjena.model.ReasoningResult;
import com.example.loanjena.model.TransferOrderRequest;
import com.example.loanjena.model.TransferBusinessRequest;
import com.example.loanjena.model.TransferReasoningResult;
import com.example.loanjena.service.TransferReasoningService;
import com.example.loanjena.service.TransferBusinessReasoningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 过户订单控制器 - BSS4.0
 * 支持完整的8步骤过户流程和智能推理
 */
@RestController
@RequestMapping("/api/transfer")
@Tag(name = "BSS4.0过户订单推理", description = "基于 TM Forum ODA 本体的过户业务推理 API")
public class TransferOrderController {

    @Autowired
    private TransferReasoningService reasoningService;
    
    @Autowired
    private TransferBusinessReasoningService businessReasoningService;

    /**
     * 【新接口】执行BSS4.0过户业务推理
     * 智能推理：
     * 1. 当前在哪一步
     * 2. 总共多少步 (8步)
     * 3. 下一步做什么
     * 4. 步骤3可以回退到步骤1
     */
    @PostMapping("/business/reason")
    @Operation(summary = "BSS4.0过户业务推理", 
               description = "执行8步骤过户流程推理，自动判断当前步骤、下一步操作、支持回退")
    public ResponseEntity<TransferReasoningResult> reasonTransferBusiness(
            @RequestBody TransferBusinessRequest request) {
        TransferReasoningResult result = businessReasoningService.performReasoning(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 【旧接口】评估转账订单（保留兼容性）
     */
    @PostMapping("/evaluate")
    @Operation(summary = "评估转账订单", description = "执行步骤化推理并返回详细的推理过程")
    public ResponseEntity<ReasoningResult> evaluateTransferOrder(@RequestBody TransferOrderRequest request) {
        ReasoningResult result = reasoningService.performReasoning(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reload-ontology")
    @Operation(summary = "重新加载本体", description = "当本体文件更新后，重新加载")
    public ResponseEntity<String> reloadOntology() {
        reasoningService.reloadOntology();
        businessReasoningService.reloadOntology();
        return ResponseEntity.ok("本体文件已重新加载 (包含8步骤过户流程)");
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务状态")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BSS4.0过户推理服务运行正常 - 支持8步骤流程");
    }
    
    @GetMapping("/steps")
    @Operation(summary = "获取过户流程步骤", description = "返回完整的8步骤流程说明")
    public ResponseEntity<String> getSteps() {
        StringBuilder steps = new StringBuilder();
        steps.append("BSS4.0过户业务流程 - 8个步骤:\n\n");
        steps.append("步骤1: 定位源客户\n");
        steps.append("  - 1.1 查询源客户列表 (ICustomerLocationService.listCustomers)\n");
        steps.append("  - 1.2 源客户鉴权 (ICustomerAuthService.authCustomer)\n\n");
        steps.append("步骤2: 过户号码选择\n");
        steps.append("  - 查询客户产品实例 (ICustomerLocationService.getCustProdInst)\n");
        steps.append("  - 客户选择过户号码\n\n");
        steps.append("步骤3: 创建客户订单 (可回退到步骤1)\n");
        steps.append("  - 调用 IOrderMgrService.createCustomerOrder\n");
        steps.append("  - 返回 custOrderId\n\n");
        steps.append("步骤4: 过户业务初始化\n");
        steps.append("  - 调用 IAppCardAcceptOptService.smartInitProdInstAcceptance\n");
        steps.append("  - serviceOfferId = 2549\n\n");
        steps.append("步骤5: 公共属性初始化\n");
        steps.append("  - 调用 IAppCardAcceptOptService.initCommonAttr\n\n");
        steps.append("步骤6: 目标客户确认\n");
        steps.append("  - 6.1 查询目标客户列表 (ICustomerLocationService.listCustomers)\n");
        steps.append("  - 6.2 目标客户鉴权 (ICustomerAuthService.authCustomer, isTarget=true)\n\n");
        steps.append("步骤7: 订单保存\n");
        steps.append("  - 调用 BusinessAcceptService.saveOrder\n\n");
        steps.append("步骤8: 订单确认\n");
        steps.append("  - 调用 CashierTaiService.updateConfirm\n");
        steps.append("  - 收银台确认，流程结束\n\n");
        steps.append("智能推理功能:\n");
        steps.append("- 自动判断当前在哪一步\n");
        steps.append("- 提示总共多少步 (8步)\n");
        steps.append("- 推荐下一步做什么\n");
        steps.append("- 步骤3支持回退到步骤1\n");
        return ResponseEntity.ok(steps.toString());
    }
}
