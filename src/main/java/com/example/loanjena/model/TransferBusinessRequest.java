package com.example.loanjena.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 完整的BSS4.0过户业务请求模型
 * 支持8步骤过户流程和智能推理
 */
@Data
public class TransferBusinessRequest {
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 当前步骤编号 (1-8)
     */
    private Integer currentStep;
    
    /**
     * 操作类型: NEXT-前进到下一步, ROLLBACK-回退, AUTO-自动推理
     */
    private String action;
    
    // ========== 步骤1: 源客户定位 ==========
    /**
     * 查询值（客户姓名/手机号/证件号）
     */
    private String sourceSearchValue;
    
    /**
     * 查询类型: B-姓名, C-证件号, D-业务号码
     */
    private String sourceSearchType;
    
    /**
     * 源客户ID (从步骤1查询结果中获取)
     */
    private String sourceCustId;
    
    /**
     * 源客户姓名
     */
    private String sourceCustName;
    
    /**
     * 源客户证件类型
     */
    private String sourceCertType;
    
    /**
     * 源客户证件号码
     */
    private String sourceCertNumber;
    
    /**
     * 源客户区域ID
     */
    private String sourceCommonRegionId;
    
    /**
     * 源客户审批等级
     */
    private String sourceApproveLevel;
    
    /**
     * 源客户鉴权结果
     */
    private Boolean sourceAuthPassed;
    
    // ========== 步骤2: 过户号码选择 ==========
    /**
     * 客户拥有的产品实例总数
     */
    private Integer totalSubscriptionCount;
    
    /**
     * 选择的过户号码
     */
    private String selectedAccNum;
    
    /**
     * 选择的产品实例ID
     */
    private String selectedProdInstId;
    
    /**
     * 选择的产品ID
     */
    private String selectedProdId;
    
    /**
     * 选择的产品名称
     */
    private String selectedProdName;
    
    /**
     * 选择的角色ID
     */
    private String selectedRoleId;
    
    /**
     * 选择的销售品实例ID
     */
    private String selectedOfferInstId;
    
    // ========== 步骤3: 创建客户订单 ==========
    /**
     * 客户订单号 (从IOrderMgrService返回)
     */
    private String custOrderId;
    
    /**
     * 订单创建时间
     */
    private String orderCreateTime;
    
    /**
     * 渠道ID
     */
    private String channelId;
    
    /**
     * 场景编码
     */
    private String sceneCode;
    
    /**
     * 受理工号
     */
    private String operatorId;
    
    // ========== 步骤4: 过户业务初始化 ==========
    /**
     * 服务包ID (固定值2549)
     */
    private String serviceOfferId = "2549";
    
    /**
     * 业务初始化结果
     */
    private Boolean businessInitSuccess;
    
    // ========== 步骤5: 公共属性初始化 ==========
    /**
     * 公共属性初始化结果
     */
    private Boolean commonAttrInitSuccess;
    
    // ========== 步骤6: 目标客户确认 ==========
    /**
     * 目标客户查询值
     */
    private String targetSearchValue;
    
    /**
     * 目标客户查询类型: B-姓名, C-证件号, D-业务号码
     */
    private String targetSearchType;
    
    /**
     * 目标客户ID
     */
    private String targetCustId;
    
    /**
     * 目标客户姓名
     */
    private String targetCustName;
    
    /**
     * 目标客户证件类型
     */
    private String targetCertType;
    
    /**
     * 目标客户证件号码
     */
    private String targetCertNumber;
    
    /**
     * 目标客户鉴权结果
     */
    private Boolean targetAuthPassed;
    
    // ========== 步骤7: 订单保存 ==========
    /**
     * 订单保存结果
     */
    private Boolean orderSaved;
    
    /**
     * 订单保存时间
     */
    private String orderSaveTime;
    
    // ========== 步骤8: 订单确认 ==========
    /**
     * 订单确认结果
     */
    private Boolean orderConfirmed;
    
    /**
     * 订单确认时间
     */
    private String orderConfirmTime;
    
    /**
     * 缴费金额
     */
    private Double paymentAmount;
    
    /**
     * 缴费渠道
     */
    private String payChannel;
    
    /**
     * 票据号
     */
    private String receiptNo;
    
    // ========== 流程控制 ==========
    /**
     * 是否需要回退
     */
    private Boolean needRollback;
    
    /**
     * 回退到的步骤编号
     */
    private Integer rollbackToStep;
    
    /**
     * 回退原因
     */
    private String rollbackReason;
}
