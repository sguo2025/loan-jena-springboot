package com.example.loanjena.controller;

import com.example.loanjena.model.ReasoningResult;
import com.example.loanjena.model.TransferOrderRequest;
import com.example.loanjena.service.TransferReasoningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 转账订单控制器
 */
@RestController
@RequestMapping("/api/transfer")
@Tag(name = "转账订单推理", description = "基于 OWL 本体的转账订单推理 API")
public class TransferOrderController {

    @Autowired
    private TransferReasoningService reasoningService;

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
        return ResponseEntity.ok("本体文件已重新加载");
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务状态")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("转账推理服务运行正常");
    }
}
