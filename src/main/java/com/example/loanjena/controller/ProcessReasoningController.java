package com.example.loanjena.controller;

import com.example.loanjena.dto.ProcessFlowInfo;
import com.example.loanjena.dto.ProcessStepInfo;
import com.example.loanjena.service.ProcessReasoningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程推理控制器
 * 基于 OWL 本体进行流程结构推理，不执行业务调度或流程实例编排
 */
@RestController
@RequestMapping("/api/process-reasoning")
@Tag(name = "流程推理", description = "基于 OWL 本体的流程推理接口")
public class ProcessReasoningController {

    @Autowired
    private ProcessReasoningService reasoningService;

    /**
     * 获取完整的流程信息
     * 
     * @return 流程信息，包含所有步骤及其关系
     */
    @GetMapping("/flow")
    @Operation(summary = "获取完整流程信息", description = "返回流程的所有步骤、总步数、开始步骤和结束步骤")
    public ResponseEntity<ProcessFlowInfo> getProcessFlow() {
        ProcessFlowInfo flowInfo = reasoningService.getProcessFlow();
        return ResponseEntity.ok(flowInfo);
    }

    /**
     * 获取指定步骤的详细信息
     * 
     * @param stepUri 步骤URI（完整URI或本地名称）
     * @return 步骤详细信息
     */
    @GetMapping("/step/{stepUri}")
    @Operation(summary = "获取步骤详细信息", description = "根据步骤URI获取步骤的详细信息，包括前驱、后继、所需实体等")
    public ResponseEntity<ProcessStepInfo> getStepInfo(
            @Parameter(description = "步骤URI或本地名称，如 'Step1_LocateSourceCustomer'", example = "Step1_LocateSourceCustomer")
            @PathVariable String stepUri) {
        ProcessStepInfo stepInfo = reasoningService.getStepInfo(stepUri);
        if (stepInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stepInfo);
    }

    /**
     * 获取指定步骤的前驱步骤列表
     * 
     * @param stepUri 步骤URI（完整URI或本地名称）
     * @return 前驱步骤列表
     */
    @GetMapping("/step/{stepUri}/previous")
    @Operation(summary = "获取前驱步骤", description = "获取指定步骤的所有前驱步骤信息")
    public ResponseEntity<List<ProcessStepInfo>> getPreviousSteps(
            @Parameter(description = "步骤URI或本地名称", example = "Step3_CreateCustomerOrder")
            @PathVariable String stepUri) {
        List<ProcessStepInfo> previousSteps = reasoningService.getPreviousStepInfos(stepUri);
        return ResponseEntity.ok(previousSteps);
    }

    /**
     * 获取指定步骤的后继步骤列表
     * 
     * @param stepUri 步骤URI（完整URI或本地名称）
     * @return 后继步骤列表
     */
    @GetMapping("/step/{stepUri}/next")
    @Operation(summary = "获取后继步骤", description = "获取指定步骤的所有后继步骤信息")
    public ResponseEntity<List<ProcessStepInfo>> getNextSteps(
            @Parameter(description = "步骤URI或本地名称", example = "Step1_LocateSourceCustomer")
            @PathVariable String stepUri) {
        List<ProcessStepInfo> nextSteps = reasoningService.getNextStepInfos(stepUri);
        return ResponseEntity.ok(nextSteps);
    }

    /**
     * 获取流程总步数
     * 
     * @return 流程总步数
     */
    @GetMapping("/flow/total-steps")
    @Operation(summary = "获取流程总步数", description = "返回流程中的步骤总数")
    public ResponseEntity<Integer> getTotalSteps() {
        ProcessFlowInfo flowInfo = reasoningService.getProcessFlow();
        return ResponseEntity.ok(flowInfo.getTotalSteps());
    }

    /**
     * 获取流程开始步骤
     * 
     * @return 开始步骤信息
     */
    @GetMapping("/flow/start-step")
    @Operation(summary = "获取流程开始步骤", description = "返回流程的第一个步骤（没有前驱的步骤）")
    public ResponseEntity<ProcessStepInfo> getStartStep() {
        ProcessFlowInfo flowInfo = reasoningService.getProcessFlow();
        if (flowInfo.getStartStepUri() == null) {
            return ResponseEntity.notFound().build();
        }
        ProcessStepInfo startStep = reasoningService.getStepInfo(flowInfo.getStartStepUri());
        return ResponseEntity.ok(startStep);
    }

    /**
     * 获取流程结束步骤
     * 
     * @return 结束步骤信息
     */
    @GetMapping("/flow/end-step")
    @Operation(summary = "获取流程结束步骤", description = "返回流程的最后一个步骤（没有后继的步骤）")
    public ResponseEntity<ProcessStepInfo> getEndStep() {
        ProcessFlowInfo flowInfo = reasoningService.getProcessFlow();
        if (flowInfo.getEndStepUri() == null) {
            return ResponseEntity.notFound().build();
        }
        ProcessStepInfo endStep = reasoningService.getStepInfo(flowInfo.getEndStepUri());
        return ResponseEntity.ok(endStep);
    }
}
