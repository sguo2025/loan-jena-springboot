package com.example.loanjena.controller;

import com.example.loanjena.model.LoanApplicationRequest;
import com.example.loanjena.service.LoanReasoningService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
public class LoanController {

    private final LoanReasoningService reasoningService;

    public LoanController(LoanReasoningService reasoningService) {
        this.reasoningService = reasoningService;
    }

    @PostMapping("/apply")
    public String apply(@RequestBody LoanApplicationRequest request) {
        return reasoningService.evaluateApplication(request);
    }
}