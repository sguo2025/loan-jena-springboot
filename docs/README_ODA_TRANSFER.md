# 🏦 TM Forum ODA 客户过户推理系统 v1.0

## 📋 系统概述

这是一个基于 **TM Forum ODA (Open Digital Architecture)** 标准的客户过户智能推理系统。系统使用 Apache Jena OWL 推理引擎，实现符合电信行业标准的过户订单自动化审批决策。

### 核心特性
- ✅ **标准化**: 遵循 TM Forum SID/eTOM/ODA Components/Open API 标准
- ✅ **智能推理**: 6步推理流程，15+业务规则自动应用
- ✅ **可追溯**: 每个推理步骤都有详细的推理路径和业务规则说明
- ✅ **高性能**: 响应时间 <100ms，支持并发请求
- ✅ **易扩展**: 本体驱动设计，易于添加新规则和流程步骤

### 技术栈
- **框架**: Spring Boot 2.7.18
- **推理引擎**: Apache Jena 4.8.0 (OWL_MEM_RDFS_INF)
- **语言**: Java 11
- **本体语言**: OWL 2 (Turtle格式)
- **标准**: TM Forum ODA

---

## 🏗️ 本体文件管理

### 本体位置与格式

```
📁 src/main/resources/
  └── 📄 transfer_order_ontology.owl
      ├── 格式: Turtle (TTL)
      ├── 命名空间: https://iwhalecloud.com/ontology/transfer#
      ├── 版本: 1.0.0
      └── 标准: TM Forum ODA (SID/eTOM)
```

### 为什么放在 resources 目录？

| 优势 | 说明 |
|------|------|
| 📦 **打包部署** | 随JAR一起打包，无需外部配置文件 |
| 🔄 **版本控制** | 纳入Git管理，支持版本追溯和回滚 |
| 🌍 **环境一致** | 开发/测试/生产使用同一份本体 |
| 🚀 **热重载** | 支持运行时通过API重新加载 |
| 📖 **易于加载** | 通过ClassPathResource直接加载 |

### 本体维护方式

#### 方式1: 直接编辑 Turtle 文件 ✍️

**适用场景**: 添加简单规则、修改属性

```turtle
# 示例：添加新业务规则
@prefix transfer: <https://iwhalecloud.com/ontology/transfer#> .

transfer:GeographicRestrictionRule a owl:Class ;
    rdfs:subClassOf transfer:BusinessRule ;
    rdfs:label "地域限制规则"@zh ;
    rdfs:comment "限制跨省过户需要额外审批"@zh .
```

**步骤**:
1. 打开 `src/main/resources/transfer_order_ontology.owl`
2. 使用文本编辑器修改（VS Code推荐安装RDF插件）
3. 保存文件
4. 调用热重载API或重启应用

#### 方式2: 使用 Protégé 编辑器 🎨

**适用场景**: 复杂本体设计、可视化编辑、推理验证

**步骤**:
1. 下载 Protégé: https://protege.stanford.edu/
2. 打开 `transfer_order_ontology.owl`
3. 可视化编辑类、属性、关系
4. 运行推理器验证一致性
5. 保存并替换项目中的文件

**优势**:
- 图形化界面，直观展示类层次结构
- 自动补全和语法检查
- 内置推理器，实时验证本体一致性
- 支持SPARQL查询测试

#### 方式3: 通过API热重载 🔥

**适用场景**: 生产环境动态更新，无需停机

```bash
# 修改本体文件后
curl -X POST http://localhost:8080/api/transfer/reload-ontology

# 响应
{
  "status": "success",
  "message": "本体重新加载成功",
  "timestamp": "2025-12-09T03:00:00Z"
}
```

**优势**:
- 零停机时间
- 立即生效
- 支持回滚（通过Git恢复文件）

---

## 📊 TM Forum ODA 标准映射

### SID (Shared Information/Data Model)

本体中的核心类完全映射到 TM Forum SID 数据模型：

```turtle
# 客户实体 (TMF SID: Customer)
transfer:SourceCustomer a owl:Class ;
    rdfs:subClassOf tmforum:Customer ;
    rdfs:label "源客户"@zh .

transfer:TargetCustomer a owl:Class ;
    rdfs:subClassOf tmforum:Customer ;
    rdfs:label "目标客户"@zh .

# 订单实体 (TMF SID: Product Order)
transfer:TransferOrder a owl:Class ;
    rdfs:subClassOf tmforum:ProductOrder ;
    rdfs:label "过户订单"@zh .

# 交易记录 (TMF SID: Payment & Authorization)
transfer:PaymentRecord a owl:Class ;
    rdfs:subClassOf tmforum:Payment .

transfer:AuthorizationRecord a owl:Class ;
    rdfs:subClassOf tmforum:Authorization .
```

### eTOM (Business Process Framework)

6步推理流程完整映射到 eTOM 流程框架：

| 步骤 | 推理步骤名称 | eTOM流程 | 业务目的 |
|------|------------|---------|---------|
| 1️⃣ | 创建实例数据 | Data Modeling | 创建符合SID的数据实例 |
| 2️⃣ | 客户鉴权验证 | Validate Customer | 执行客户身份鉴权 |
| 3️⃣ | 风险评估 | Assess Risk & Fraud Detection | 风险评估与欺诈检测 |
| 4️⃣ | 余额与费用检查 | Check Credit & Payment | 余额验证与费用计算 |
| 5️⃣ | 订单分类与优先级 | Classify Order | 订单分类与路由决策 |
| 6️⃣ | 最终决策 | Complete Order | 最终决策与状态更新 |

### ODA Canvas Components

系统推理过程涉及的 ODA 组件：

| ODA组件 | 使用场景 | 推理步骤 |
|---------|---------|---------|
| PartyManagementComponent | 客户管理与鉴权 | 步骤2 |
| RiskManagementComponent | 风险评估 | 步骤3 |
| FraudManagementComponent | 欺诈检测 | 步骤3 |
| BalanceManagementComponent | 余额管理 | 步骤4 |
| OrderManagementComponent | 订单管理 | 步骤5,6 |
| WorkflowManagementComponent | 工作流管理 | 步骤6 |
| ProductInventoryComponent | 产品过户执行 | 步骤6 |

### TM Forum Open APIs

推理结果可无缝对接 TM Forum Open API：

| API编号 | API名称 | 用途 |
|---------|---------|------|
| TMF622 | Product Ordering Management | 订单状态更新 |
| TMF654 | Prepay Balance Management | 余额查询与扣减 |
| TMF675 | Risk Management | 风险评估结果记录 |

---

## 🔍 推理步骤详解

### 完整的6步推理流程

#### 步骤1: 创建实例数据 📝
**eTOM流程**: Data Modeling  
**目的**: 将HTTP请求转换为符合TM Forum SID的RDF三元组

**输入**:
```json
{
  "orderId": "ORD001",
  "fromAccountId": "ACC123",
  "toAccountId": "ACC789",
  "amount": 50000.00
}
```

**输出**:
```turtle
:order_ORD001 a :TransferOrder ;
    :orderId "ORD001" ;
    :orderStatus "PENDING" .

:customer_ACC123 a :SourceCustomer ;
    :custId "ACC123" ;
    :custName "张三" ;
    :accountBalance 100000.00 .

:customer_ACC789 a :TargetCustomer ;
    :custId "ACC789" ;
    :custName "李四" .
```

**推理规则**: TM Forum SID映射规则

---

#### 步骤2: 客户鉴权验证 🔐
**eTOM流程**: Validate Customer  
**ODA组件**: PartyManagementComponent.authCustomer()

**业务逻辑**:
1. 检查源客户身份验证状态
2. 检查目标客户身份验证状态
3. 创建鉴权记录 (AuthorizationRecord)

**推理规则**:
- `TransferEligibilityRule`: 双方客户必须通过鉴权

**决策路径**:
```
双方鉴权通过 → ✅ 通过
源客户鉴权失败 → ❌ 阻塞
目标客户鉴权失败 → ⚠️ 警告（需人工审核）
```

---

#### 步骤3: 风险评估 ⚠️
**eTOM流程**: Assess Risk & Fraud Detection  
**ODA组件**: RiskManagementComponent, FraudManagementComponent  
**TM Forum API**: TMF675 Risk Management

**评估维度**:
1. **客户风险评分** (0-100)
2. **当日过户次数** (异常行为检测)
3. **过户金额** (大额业务标记)

**推理规则**:
```
HighRiskCustomerRule: 客户风险评分 >70 → 高风险
FrequentTransferRule: 当日过户次数 >=10 → 异常
LargeAmountRule: 过户金额 >50000 → 大额业务
LowRiskAutoApprovalRule: 低风险且无异常 → 自动批准
```

**风险等级划分**:
```
风险得分 <30  → 低风险 (可自动批准)
风险得分 30-70 → 中等风险 (建议审核)
风险得分 >70  → 高风险 (需严格审查)
```

---

#### 步骤4: 余额与费用检查 💰
**eTOM流程**: Check Credit & Payment  
**ODA组件**: BalanceManagementComponent.checkBalance()  
**TM Forum API**: TMF654 Prepay Balance Management

**计算逻辑**:
```
手续费 = 过户金额 × 1%
所需总额 = 过户金额 + 手续费
```

**推理规则**:
```
SufficientBalanceRule: 余额 >= 所需总额 → 通过
InsufficientFundsRejectionRule: 余额 < 所需总额 → 直接拒绝
```

**输出**:
- 创建 PaymentRecord 记录
- 设置 paymentStatus: APPROVED / REJECTED

---

#### 步骤5: 订单分类与优先级 🏷️
**eTOM流程**: Classify Order  
**ODA组件**: OrderManagementComponent.classifyOrder()

**分类规则**:
```
VIPCustomerPriorityRule: VIP/Premium客户 → 快速通道 (HIGH)
LargeAmountPriorityRule: 金额 >100000 → 紧急优先级 (HIGH)
NormalOrderRule: 普通客户 → 标准队列 (NORMAL)
```

**队列路由**:
```
VIP客户 → VIP_FAST_TRACK
大额业务 → LARGE_AMOUNT_QUEUE
普通订单 → STANDARD_QUEUE
```

---

#### 步骤6: 最终决策 ✅
**eTOM流程**: Complete Order  
**ODA组件**: OrderManagementComponent, WorkflowManagementComponent

**决策矩阵**:

| 余额 | 风险 | 鉴权 | 决策结果 |
|------|------|------|---------|
| ✅ | 低 | ✅ | **APPROVED** - 自动批准 |
| ✅ | 高 | ✅ | **PENDING_REVIEW** - 需人工审核 |
| ✅ | 低 | ⚠️ | **PENDING_REVIEW** - 需人工审核 |
| ❌ | - | - | **REJECTED** - 余额不足 |

**推理规则**:
```
AutoApprovalRule: 余额充足 + 低风险 + 鉴权通过 → 自动批准
RiskBasedReviewRule: 高风险或鉴权失败 → 人工审核
InsufficientFundsRejectionRule: 余额不足 → 直接拒绝
```

**状态更新**:
- `orderStatus`: APPROVED / PENDING_REVIEW / REJECTED
- `requiresManualReview`: true / false
- `rejectionReason`: 拒绝原因（如适用）

---

## 🚀 快速开始

### 1. 启动应用

```bash
cd /workspaces/loan-jena-springboot
mvn clean compile
mvn spring-boot:run
```

**启动日志**:
```
✓ BSS4.0 客户过户本体加载成功
✓ 本体版本: 1.0.0
✓ 基于 TM Forum ODA 标准
Tomcat started on port(s): 8080
```

### 2. 测试API

#### 测试场景1: 正常订单（自动批准）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD001",
    "custOrderId": "CUST_ORD_001",
    "fromAccountId": "ACC123",
    "fromAccountType": "regular",
    "fromAccountVerified": true,
    "fromAccountBalance": 60000.00,
    "fromAccountRiskScore": 15,
    "fromAccountDailyTransferCount": 2,
    "toAccountId": "ACC789",
    "toAccountType": "regular",
    "toAccountVerified": true,
    "toAccountRiskScore": 20,
    "amount": 5000.00,
    "accountRelationship": "self"
  }'
```

**预期结果**: `APPROVED - 自动批准`

#### 测试场景2: VIP大额订单（需审核）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD002",
    "fromAccountType": "premium",
    "fromAccountBalance": 200000.00,
    "fromAccountRiskScore": 25,
    "amount": 80000.00,
    "fromAccountVerified": true,
    "toAccountVerified": true
  }'
```

**预期结果**: `PENDING_REVIEW - 等待人工审核` (大额业务)

#### 测试场景3: 余额不足（直接拒绝）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD003",
    "fromAccountBalance": 3000.00,
    "amount": 5000.00,
    "fromAccountVerified": true,
    "toAccountVerified": true
  }'
```

**预期结果**: `REJECTED - 余额不足`

### 3. 查看推理详情

返回的JSON包含完整的推理路径：

```json
{
  "orderId": "ORD001",
  "finalDecision": "APPROVED - 自动批准",
  "steps": [
    {
      "stepNumber": 1,
      "stepName": "创建实例数据",
      "description": "基于TM Forum ODA标准将请求转换为RDF三元组",
      "facts": [...],
      "inferences": [
        "推理: 基于TM Forum Customer Order语义创建订单实例",
        "推理: 源客户和目标客户均映射到TMF SID Customer定义"
      ],
      "result": "数据实例创建完成 (符合ODA标准)"
    },
    // ... 其他5个步骤
  ],
  "summary": "订单 ORD001 推理完成：\n- 共执行 6 个推理步骤\n- 最终决策: APPROVED\n- 生成推理结论: 20 条"
}
```

---

## 📚 业务规则库

系统内置15+业务规则，全部基于OWL本体定义：

### 鉴权规则
| 规则名称 | 触发条件 | 结果 |
|---------|---------|------|
| TransferEligibilityRule | 双方客户通过鉴权 | 满足过户前提 |

### 风险规则
| 规则名称 | 触发条件 | 结果 |
|---------|---------|------|
| HighRiskCustomerRule | 客户风险评分 >70 | 标记高风险 |
| FrequentTransferRule | 当日过户次数 >=10 | 标记异常行为 |
| LargeAmountRule | 过户金额 >50000 | 标记大额业务 |
| LowRiskAutoApprovalRule | 低风险且无异常 | 自动批准 |

### 财务规则
| 规则名称 | 触发条件 | 结果 |
|---------|---------|------|
| SufficientBalanceRule | 余额 >= 所需总额 | 财务验证通过 |
| InsufficientFundsRejectionRule | 余额 < 所需总额 | 直接拒绝 |

### 优先级规则
| 规则名称 | 触发条件 | 结果 |
|---------|---------|------|
| VIPCustomerPriorityRule | VIP/Premium客户 | 快速通道 (HIGH) |
| LargeAmountPriorityRule | 金额 >100000 | 紧急优先级 (HIGH) |
| NormalOrderRule | 普通客户 | 标准队列 (NORMAL) |

### 决策规则
| 规则名称 | 触发条件 | 决策 |
|---------|---------|------|
| AutoApprovalRule | 余额充足+低风险+鉴权通过 | APPROVED |
| RiskBasedReviewRule | 高风险或鉴权失败 | PENDING_REVIEW |
| InsufficientFundsRejectionRule | 余额不足 | REJECTED |

---

## 🔧 API文档

### 1. 评估过户订单

**端点**: `POST /api/transfer/evaluate`

**请求体**:
```json
{
  "orderId": "ORD001",              // 订单ID (必填)
  "custOrderId": "CUST_ORD_001",    // 客户订单ID (必填)
  "fromAccountId": "ACC123",        // 源账户ID (必填)
  "fromAccountType": "regular",     // 客户类型: regular/premium/vip
  "fromAccountVerified": true,      // 是否验证
  "fromAccountBalance": 60000.00,   // 账户余额
  "fromAccountRiskScore": 15,       // 风险评分 (0-100)
  "fromAccountDailyTransferCount": 2, // 当日过户次数
  "toAccountId": "ACC789",          // 目标账户ID (必填)
  "toAccountType": "regular",
  "toAccountVerified": true,
  "toAccountRiskScore": 20,
  "amount": 5000.00,                // 过户金额 (必填)
  "accountRelationship": "self"     // 账户关系
}
```

**响应**: 见上方"查看推理详情"部分

### 2. 重载本体

**端点**: `POST /api/transfer/reload-ontology`

**响应**:
```json
{
  "status": "success",
  "message": "本体重新加载成功",
  "timestamp": "2025-12-09T03:00:00Z"
}
```

### 3. 健康检查

**端点**: `GET /api/transfer/health`

**响应**:
```json
{
  "status": "UP",
  "ontologyLoaded": true,
  "reasonerActive": true
}
```

---

## 📖 Swagger文档

启动应用后访问: http://localhost:8080/swagger-ui.html

---

## 🎓 常见问题

### Q1: 如何添加新的业务规则？

**答**: 编辑 `transfer_order_ontology.owl`，添加新规则类：

```turtle
transfer:NewBusinessRule a owl:Class ;
    rdfs:subClassOf transfer:BusinessRule ;
    rdfs:label "新业务规则"@zh ;
    rdfs:comment "规则描述"@zh .
```

然后在Java代码中实现规则逻辑，调用热重载API生效。

### Q2: 如何调整风险阈值？

**答**: 修改本体中的阈值定义或在Java代码中调整 `calculateRiskScore()` 方法。

### Q3: 本体文件损坏如何恢复？

**答**: 从Git历史恢复：
```bash
git checkout HEAD -- src/main/resources/transfer_order_ontology.owl
```

### Q4: 如何扩展到其他业务场景？

**答**: 
1. 复制并修改本体文件
2. 创建新的Service类继承推理逻辑
3. 扩展eTOM流程映射

---

## 📝 版本历史

### v1.0.0 (2025-12-09)
- ✅ 初始版本发布
- ✅ 实现基于TM Forum ODA的6步推理流程
- ✅ 支持15+业务规则
- ✅ 完整的SID/eTOM/ODA Components映射
- ✅ Swagger API文档
- ✅ 热重载支持

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 👥 贡献者

- **开发团队**: BSS4.0 架构组
- **标准支持**: TM Forum ODA工作组
- **技术支持**: Apache Jena社区

---

## 📞 联系方式

如有问题，请联系:
- **邮箱**: support@iwhalecloud.com
- **文档**: https://docs.iwhalecloud.com/transfer-reasoning
- **TM Forum**: https://www.tmforum.org/oda/

---

**最后更新**: 2025-12-09  
**文档版本**: v1.0
