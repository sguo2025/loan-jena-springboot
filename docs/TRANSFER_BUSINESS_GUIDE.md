# BSS4.0 过户业务智能推理系统

## 系统概述

本系统基于 **TM Forum ODA (Open Digital Architecture)** 标准，实现了完整的BSS4.0过户业务智能推理引擎。

### 核心能力

1. **8步骤过户流程管理**
2. **智能状态推理** - 自动判断当前步骤
3. **下一步决策** - 智能推荐下一步操作
4. **步骤回退** - 步骤3可回退到步骤1
5. **本体建模** - 基于OWL语义模型
6. **规则推理** - SWRL业务规则引擎

---

## 业务流程详解

### 步骤1: 定位源客户

**目标**: 查询并鉴权源客户身份

**执行内容**:
1. **1.1 查询源客户列表**
   - 接口: `ICustomerLocationService.listCustomers`
   - 入参示例:
   ```json
   {
     "searchValue": "李三狗",
     "searchType": "B",  // B-姓名, C-证件号, D-业务号码
     "pageNum": 1,
     "pageSize": 10
   }
   ```
   - 出参示例:
   ```json
   {
     "resultCode": "0",
     "resultMsg": "success",
     "resultObject": {
       "total": "1",
       "list": [{
         "custId": "931282308426",
         "custName": "李**",
         "certiType": "1",
         "certiNumber": "440113********8939",
         "commonRegionId": "930013101"
       }]
     }
   }
   ```

2. **1.2 源客户鉴权**
   - 接口: `ICustomerAuthService.authCustomer`
   - 入参:
   ```json
   {
     "custId": "931282308426",
     "searchType": "idCard",
     "isTarget": false
   }
   ```

**本体建模**:
- 类: `transfer:SourceCustomer` (源客户)
- 类: `transfer:AuthorizationRecord` (鉴权记录)
- 属性: `transfer:hasAuthorization` (关联鉴权)

**推理规则**:
```
TransferEligibilityRule:
  IF SourceCustomer(?s) AND AuthorizationRecord(?a) 
     AND hasAuthorization(?o, ?a) AND authResult(?a, "PASSED")
  THEN isEligibleForTransfer(?o, true)
```

**ODA组件**: 
- `PartyManagementComponent.authCustomer()`

**TMF API**: 
- `TMF632 Party Management API v4`

---

### 步骤2: 过户号码选择

**目标**: 查询客户产品实例，选择过户号码

**执行内容**:
1. 调用 `ICustomerLocationService.getCustProdInst`
   - 入参:
   ```json
   {
     "custId": "931282308432"
   }
   ```
   - 出参:
   ```json
   {
     "resultObject": {
       "mobileCount": 0,
       "broadbandCount": 2,
       "broadbandList": [{
         "accNum": "931LZ0040613023",
         "prodInstId": "613300070614",
         "prodName": "宽带",
         "status": "100000"
       }]
     }
   }
   ```

2. **多号码选择规则**:
   - 如果总数 > 1: 列出所有号码，让客户选择
   - 如果总数 = 1: 询问客户是否对该号码过户

**本体建模**:
- 类: `transfer:TransferableSubscription` (可转移订阅)
- 属性: `transfer:accNum`, `transfer:prodInstId`

**推理规则**:
```
MultiInstanceSelectionRule:
  IF subscriptionCount(?c, ?count) AND greaterThan(?count, 1)
  THEN customerSelectionRequired(?c, true)
```

**ODA组件**: 
- `ServiceConfigurationComponent.getProductInstance()`

**TMF API**: 
- `TMF637 Product Inventory Management API v4`

---

### 步骤3: 创建客户订单 ⚠️ 可回退点

**目标**: 创建BSS客户订单

**特别说明**: 
> 🔄 **此步骤支持回退到步骤1** - 如需修改源客户或过户号码，可执行回退操作

**执行内容**:
1. 调用 `IOrderMgrService.createCustomerOrder`
   - 入参说明:
     - `cust_id`: 取步骤1的 `custId`
     - `certType`: 取步骤1的 `certiType`
     - `certNum`: 取步骤1的 `certiNumber`
   - 出参:
     - `custOrderId`: 客户订单号

**本体建模**:
- 类: `transfer:TransferOrder` (过户订单)
- 属性: `transfer:custOrderId`, `transfer:orderStatus`
- 属性: `transfer:canRollback = true`, `transfer:rollbackToStep = 1`

**回退逻辑**:
```
IF currentStepNumber(?o, 3) AND needRollback(?o, true)
THEN rollbackToStep(?o, 1) AND resetOrderState(?o)
```

**ODA组件**: 
- `OrderCaptureComponent.createOrder()`

**TMF API**: 
- `TMF622 Product Order Management API v4`

---

### 步骤4: 过户业务初始化

**目标**: 初始化产品实例受理

**执行内容**:
1. 调用 `IAppCardAcceptOptService.smartInitProdInstAcceptance`
   - 入参:
     - `serviceOfferId`: 固定值 `"2549"`
     - `custId`: 取步骤1的 `custId`
     - `prodInstId`: 取步骤2选择的 `prodInstId`
     - `custOrderId`: 取步骤3的 `custOrderId`

**本体建模**:
- 流程步骤: `transfer:Step4_InitTransferBusiness`
- eTOM映射: `Fulfillment.Service Configuration & Activation`

**ODA组件**: 
- `ServiceConfigurationComponent.initAcceptance()`

**TMF API**: 
- `TMF640 Service Activation & Configuration API v4`

---

### 步骤5: 公共属性初始化

**目标**: 初始化业务公共属性

**执行内容**:
1. 调用 `IAppCardAcceptOptService.initCommonAttr()`

**本体建模**:
- 流程步骤: `transfer:Step5_InitCommonAttributes`

**ODA组件**: 
- `ServiceConfigurationComponent.initCommonAttr()`

---

### 步骤6: 目标客户确认

**目标**: 查询并鉴权目标客户（过户接收方）

**执行内容**:
1. **6.1 查询目标客户列表**
   - 接口: `ICustomerLocationService.listCustomers`
   - 入参: 同步骤1
   
2. **6.2 目标客户鉴权**
   - 接口: `ICustomerAuthService.authCustomer`
   - 入参:
   ```json
   {
     "custId": "931282308427",
     "searchType": "idCard",
     "isTarget": true  // ⚠️ 注意: 目标客户设为 true
   }
   ```

**本体建模**:
- 类: `transfer:TargetCustomer` (目标客户)
- 属性: `transfer:hasTargetCustomer`

**推理规则**:
```
TransferCompletionRule:
  IF hasSourceCustomer(?o, ?sc) AND sourceAuthPassed(?sc, true)
     AND hasTargetCustomer(?o, ?tc) AND targetAuthPassed(?tc, true)
  THEN canProceedToSave(?o, true)
```

**ODA组件**: 
- `PartyManagementComponent.authCustomer()`

---

### 步骤7: 订单保存

**目标**: 提交并保存订单

**执行内容**:
1. 调用 `BusinessAcceptService.saveOrder(reqMap)`
   - `reqMap` 包含完整的订单信息:
     - 源客户信息 (步骤1)
     - 过户号码信息 (步骤2)
     - 订单号 (步骤3)
     - 目标客户信息 (步骤6)

**订单保存详细内容**:
```json
{
  "custOrderId": "CUST_ORD_20251209001",
  "sourceCustomer": {
    "custId": "931282308426",
    "custName": "李三狗",
    "certType": "1",
    "certNumber": "440113********8939"
  },
  "targetCustomer": {
    "custId": "931282308427",
    "custName": "张四",
    "certType": "1",
    "certNumber": "360311********2517"
  },
  "subscription": {
    "accNum": "931LZ0040613023",
    "prodInstId": "613300070614",
    "prodName": "宽带",
    "serviceOfferId": "2549"
  },
  "orderStatus": "SAVED",
  "channelId": "CH001",
  "operatorId": "OP12345"
}
```

**本体建模**:
- 状态变更: `orderStatus = "SAVED"`

**ODA组件**: 
- `OrderCaptureComponent.saveOrder()`

**TMF API**: 
- `TMF622 Product Order Management API v4`

---

### 步骤8: 订单确认

**目标**: 收银台确认订单，完成过户

**执行内容**:
1. 调用 `CashierTaiService.updateConfirm`
   - 完成缴费流程
   - 生成票据

**订单确认详细内容**:
```json
{
  "custOrderId": "CUST_ORD_20251209001",
  "confirmTime": "2025-12-09 15:30:00",
  "payment": {
    "paymentAmount": 0.00,
    "payChannel": "COUNTER",
    "paymentStatus": "CONFIRMED",
    "receiptNo": "RCP2025120900001"
  },
  "orderStatus": "CONFIRMED"
}
```

**本体建模**:
- 类: `transfer:PaymentRecord` (缴费记录)
- 属性: `transfer:hasPayment`
- 状态变更: `orderStatus = "CONFIRMED"`

**推理规则**:
```
PaymentConfirmationRule:
  IF TransferOrder(?o) AND PaymentRecord(?p) 
     AND paymentStatus(?p, "SETTLED")
  THEN canConfirmOrder(?o, true)
```

**ODA组件**: 
- `BillingManagementComponent.confirmPayment()`

**TMF API**: 
- `TMF676 Payment Management API v4`

---

## 智能推理引擎

### 推理能力1: 当前步骤判断

**推理逻辑**:
```
根据请求数据中的完成状态推理当前步骤:
- 如果 sourceCustId != null && sourceAuthPassed == true => 步骤1完成
- 如果 selectedAccNum != null && selectedProdInstId != null => 步骤2完成
- 如果 custOrderId != null => 步骤3完成
- 如果 businessInitSuccess == true => 步骤4完成
- 如果 commonAttrInitSuccess == true => 步骤5完成
- 如果 targetCustId != null && targetAuthPassed == true => 步骤6完成
- 如果 orderSaved == true => 步骤7完成
- 如果 orderConfirmed == true => 步骤8完成

当前步骤 = 最后一个完成的步骤 + 1
```

### 推理能力2: 下一步决策

**推理逻辑**:
```
IF 当前步骤未完成:
  下一步 = 完成当前步骤
ELSE:
  下一步 = 当前步骤 + 1
  
IF 下一步 > 8:
  流程结束
```

### 推理能力3: 回退控制

**回退规则**:
```
IF currentStep == 3 AND needRollback == true:
  ROLLBACK TO step1
  CLEAR: sourceCustId, selectedAccNum, custOrderId
  REASON: "需要重新选择源客户或过户号码"
```

**回退场景**:
1. 客户临时改变主意，想换另一个号码过户
2. 发现源客户信息有误
3. 需要重新进行客户鉴权

---

## 本体建模说明

### 核心类定义

```turtle
# 业务域对象
transfer:DomainObject a owl:Class .

# 客户类
transfer:SourceCustomer rdfs:subClassOf transfer:DomainObject, tmforum:Customer .
transfer:TargetCustomer rdfs:subClassOf transfer:DomainObject, tmforum:Customer .

# 订单类
transfer:TransferOrder rdfs:subClassOf transfer:DomainObject, tmforum:CustomerOrder .

# 订阅类
transfer:TransferableSubscription rdfs:subClassOf transfer:DomainObject, tmforum:ProductOrderItem .

# 鉴权记录
transfer:AuthorizationRecord rdfs:subClassOf transfer:DomainObject, tmforum:CustomerInteraction .

# 缴费记录
transfer:PaymentRecord rdfs:subClassOf transfer:DomainObject, tmforum:Payment .

# 流程步骤
transfer:Step1_LocateSourceCustomer rdfs:subClassOf transfer:ProcessStep .
transfer:Step2_SelectTransferNumber rdfs:subClassOf transfer:ProcessStep .
transfer:Step3_CreateCustomerOrder rdfs:subClassOf transfer:ProcessStep .
transfer:Step4_InitTransferBusiness rdfs:subClassOf transfer:ProcessStep .
transfer:Step5_InitCommonAttributes rdfs:subClassOf transfer:ProcessStep .
transfer:Step6_ConfirmTargetCustomer rdfs:subClassOf transfer:ProcessStep .
transfer:Step7_SaveOrder rdfs:subClassOf transfer:ProcessStep .
transfer:Step8_ConfirmOrder rdfs:subClassOf transfer:ProcessStep .
```

### 对象属性

```turtle
# 订单关联
transfer:hasSourceCustomer rdfs:domain transfer:TransferOrder ;
                           rdfs:range transfer:SourceCustomer .

transfer:hasTargetCustomer rdfs:domain transfer:TransferOrder ;
                           rdfs:range transfer:TargetCustomer .

transfer:changesSubscription rdfs:domain transfer:TransferOrder ;
                             rdfs:range transfer:TransferableSubscription .

transfer:hasAuthorization rdfs:domain transfer:TransferOrder ;
                          rdfs:range transfer:AuthorizationRecord .

transfer:hasPayment rdfs:domain transfer:TransferOrder ;
                    rdfs:range transfer:PaymentRecord .
```

### 数据属性（流程控制）

```turtle
# 步骤控制
transfer:currentStepNumber rdfs:domain transfer:TransferOrder ;
                           rdfs:range xsd:integer .

transfer:totalSteps rdfs:domain transfer:TransferOrder ;
                    rdfs:range xsd:integer .

transfer:nextStepNumber rdfs:domain transfer:TransferOrder ;
                        rdfs:range xsd:integer .

# 回退控制
transfer:canRollback rdfs:domain transfer:TransferOrder ;
                     rdfs:range xsd:boolean .

transfer:rollbackToStep rdfs:domain transfer:TransferOrder ;
                        rdfs:range xsd:integer .

# 步骤状态
transfer:stepStatus rdfs:domain transfer:ProcessStep ;
                    rdfs:range xsd:string .  # PENDING/IN_PROGRESS/COMPLETED/FAILED/ROLLED_BACK
```

---

## API接口说明

### 主接口: 过户业务推理

**端点**: `POST /api/transfer/business/reason`

**请求示例1 - 步骤1 (源客户定位)**:
```json
{
  "orderId": "ORD_20251209001",
  "currentStep": 1,
  "action": "NEXT",
  "sourceSearchValue": "李三狗",
  "sourceSearchType": "B",
  "sourceCustId": "931282308426",
  "sourceCustName": "李三狗",
  "sourceCertType": "1",
  "sourceCertNumber": "440113199012018939",
  "sourceAuthPassed": true
}
```

**响应示例1**:
```json
{
  "orderId": "ORD_20251209001",
  "currentStep": 1,
  "currentStepName": "定位源客户",
  "totalSteps": 8,
  "nextStep": 2,
  "nextStepName": "过户号码选择",
  "nextStepGuide": "调用 ICustomerLocationService.getCustProdInst 查询产品实例，让客户选择过户号码",
  "canRollback": false,
  "finalDecision": "IN_PROGRESS - 当前步骤1/8，下一步: 步骤2 - 过户号码选择",
  "agentDecisionExplanation": "【智能体推理结果】\n当前位置: 步骤1 - 定位源客户\n完成进度: 1/8 步\n下一步骤: 步骤2 - 过户号码选择\n...",
  "steps": [...],
  "completionStatus": {
    "step1Completed": true,
    "step2Completed": false,
    ...
  }
}
```

**请求示例2 - 步骤3 (可回退点)**:
```json
{
  "orderId": "ORD_20251209001",
  "currentStep": 3,
  "action": "NEXT",
  "sourceCustId": "931282308426",
  "sourceCertType": "1",
  "sourceCertNumber": "440113199012018939",
  "sourceAuthPassed": true,
  "selectedAccNum": "931LZ0040613023",
  "selectedProdInstId": "613300070614",
  "custOrderId": "CUST_ORD_20251209001"
}
```

**响应示例2**:
```json
{
  "currentStep": 3,
  "currentStepName": "创建客户订单",
  "nextStep": 4,
  "nextStepName": "过户业务初始化",
  "canRollback": true,
  "rollbackToStep": 1,
  "agentDecisionExplanation": "【智能体推理结果】\n...\n【回退选项】\n✓ 当前支持回退到步骤1\n✓ 回退后可重新选择源客户和过户号码"
}
```

**请求示例3 - 回退操作**:
```json
{
  "orderId": "ORD_20251209001",
  "currentStep": 3,
  "action": "ROLLBACK",
  "needRollback": true,
  "rollbackToStep": 1,
  "rollbackReason": "客户需要更换过户号码"
}
```

---

## 使用示例

### 场景1: 完整流程执行

```bash
# 步骤1: 定位源客户
curl -X POST http://localhost:8080/api/transfer/business/reason \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD001",
    "sourceSearchValue": "李三狗",
    "sourceSearchType": "B",
    "sourceCustId": "931282308426",
    "sourceAuthPassed": true
  }'

# 步骤2: 选择号码
curl -X POST http://localhost:8080/api/transfer/business/reason \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD001",
    "sourceCustId": "931282308426",
    "sourceAuthPassed": true,
    "totalSubscriptionCount": 2,
    "selectedAccNum": "931LZ0040613023",
    "selectedProdInstId": "613300070614"
  }'

# ... 继续后续步骤
```

### 场景2: 步骤3回退

```bash
# 在步骤3时决定回退
curl -X POST http://localhost:8080/api/transfer/business/reason \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD001",
    "currentStep": 3,
    "custOrderId": "CUST_ORD_001",
    "needRollback": true,
    "rollbackReason": "需要更换源客户"
  }'
```

---

## 技术架构

### 技术栈
- **Spring Boot 3.x**: 应用框架
- **Apache Jena**: OWL本体推理引擎
- **TM Forum ODA**: 业务标准和语义模型
- **SWRL**: 语义Web规则语言

### 推理引擎工作流程

```
1. 加载OWL本体文件 (transfer_order_ontology.owl)
2. 创建推理器 (OWL Reasoner)
3. 根据请求创建RDF实例
4. 应用SWRL规则推理
5. 执行状态分析和步骤判断
6. 生成推理结果和下一步建议
7. 返回JSON格式的决策报告
```

### 关键类说明

| 类名 | 作用 |
|------|------|
| `TransferBusinessReasoningService` | 核心推理服务 |
| `TransferBusinessRequest` | 业务请求模型 |
| `TransferReasoningResult` | 推理结果模型 |
| `TransferOrderController` | REST API控制器 |

---

## 配置说明

### application.properties

```properties
# Spring Boot配置
spring.application.name=loan-jena-springboot
server.port=8080

# 日志配置
logging.level.com.example.loanjena=DEBUG
logging.level.org.apache.jena=INFO

# Swagger配置
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 总结

本系统实现了完整的BSS4.0过户业务智能推理，具有以下特点：

✅ **8步骤流程管理** - 完整覆盖从客户定位到订单确认  
✅ **智能状态推理** - 自动判断当前步骤和完成状态  
✅ **下一步决策** - 基于本体推理推荐下一步操作  
✅ **步骤回退** - 步骤3支持回退到步骤1  
✅ **本体建模** - 基于TM Forum ODA标准  
✅ **规则推理** - SWRL规则引擎自动推理  
✅ **详细追踪** - 记录ODA组件调用、TMF API、业务规则应用  

**推理引擎思考决策过程**:
1. 分析请求中的数据完整性
2. 推理各步骤的完成状态
3. 应用业务规则验证
4. 判断当前所处步骤
5. 计算下一步操作
6. 检查回退可行性
7. 生成智能决策建议

通过本系统，实现了从传统流程驱动到智能推理驱动的转变，大幅提升了业务处理的智能化水平。
