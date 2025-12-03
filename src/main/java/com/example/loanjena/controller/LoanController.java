package com.example.loanjena.controller;

import com.example.loanjena.model.LoanApplicationRequest;
import com.example.loanjena.service.LoanReasoningService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
@Tag(name = "Loan", description = "贷款申请评估 API")
public class LoanController {

    private final LoanReasoningService reasoningService;

    public LoanController(LoanReasoningService reasoningService) {
        this.reasoningService = reasoningService;
    }

    @PostMapping("/apply")
    @Operation(
        summary = "提交贷款申请",
        description = "根据申请人信息进行贷款批准或拒绝的评估"
    )
    @ApiResponse(
        responseCode = "200",
        description = "返回评估结果 (Accepted 或 Rejected)",
        content = @Content(schema = @Schema(type = "string", example = "Accepted"))
    )
    public String apply(@RequestBody LoanApplicationRequest request) {
        return reasoningService.evaluateApplication(request);
    }
}