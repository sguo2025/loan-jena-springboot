# 转账订单 OWL 本体推理系统

## 📋 概述

这是一个基于 Apache Jena 和 OWL 本体的转账订单智能推理系统。系统通过语义推理技术，实现转账订单的自动化审批决策。

## 🏗️ 架构设计

### 1. 本体文件维护

**本体文件位置：** `src/main/resources/transfer_order_ontology.owl`

#### 为什么放在 resources 目录？

- ✅ **打包到 JAR**：随应用一起部署
- ✅ **类路径加载**：通过 `ClassPathResource` 轻松加载
- ✅ **版本控制**：纳入 Git 管理
- ✅ **环境一致性**：开发、测试、生产环境使用同一份本体

#### 本体文件维护方式

```
方式 1：直接编辑 OWL 文件（推荐用于小改动）
   - 使用文本编辑器直接修改
   - 适合添加简单的类、属性

方式 2：使用 Protégé 编辑器（推荐用于复杂本体）
   - 下载 Protégé: https://protege.stanford.edu/
   - 打开本体文件进行可视化编辑
   - 保存后替换项目中的 OWL 文件

方式 3：通过 API 动态更新（高级）
   - 运行时修改本体
   - 调用 /api/transfer/reload-ontology 重新加载
```

### 2. 推理步骤说明

系统实现 **6 步推理流程**：

```
步骤 1: 创建实例数据
   ↓ 将请求转换为 RDF 三元组
   
步骤 2: 验证账户状态
   ↓ 检查账户是否通过身份验证
   
步骤 3: 评估风险等级
   ↓ 基于风险评分和行为模式
   
步骤 4: 检查余额充足性
   ↓ 验证转账金额是否合理
   
步骤 5: 确定订单类型
   ↓ 分类为普通/紧急/VIP 订单
   
步骤 6: 最终决策
   ↓ 综合所有推理结果
   
结果: APPROVED / REJECTED / PENDING_REVIEW
```

## 🚀 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 访问 API 文档

打开浏览器访问：`http://localhost:8080/swagger-ui.html`

### 3. 测试示例

#### 示例 1：正常订单（应该通过）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "T001",
    "fromAccountId": "ACC001",
    "toAccountId": "ACC002",
    "amount": 1000.00,
    "fromAccountType": "personal",
    "fromAccountBalance": 5000.00,
    "fromAccountVerified": true,
    "fromAccountDailyTransferCount": 2,
    "fromAccountRiskScore": 30,
    "toAccountType": "personal",
    "toAccountBalance": 2000.00,
    "toAccountVerified": true,
    "toAccountRiskScore": 25
  }'
```

**预期结果：** `APPROVED - 自动批准`

#### 示例 2：高风险订单（需要审核）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "T002",
    "fromAccountId": "ACC003",
    "toAccountId": "ACC004",
    "amount": 60000.00,
    "fromAccountType": "personal",
    "fromAccountBalance": 80000.00,
    "fromAccountVerified": true,
    "fromAccountDailyTransferCount": 12,
    "fromAccountRiskScore": 75,
    "toAccountType": "personal",
    "toAccountBalance": 1000.00,
    "toAccountVerified": false,
    "toAccountRiskScore": 80
  }'
```

**预期结果：** `PENDING_REVIEW - 等待人工审核`

#### 示例 3：余额不足（拒绝）

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "T003",
    "fromAccountId": "ACC005",
    "toAccountId": "ACC006",
    "amount": 10000.00,
    "fromAccountType": "personal",
    "fromAccountBalance": 500.00,
    "fromAccountVerified": true,
    "fromAccountDailyTransferCount": 1,
    "fromAccountRiskScore": 20,
    "toAccountType": "personal",
    "toAccountBalance": 3000.00,
    "toAccountVerified": true,
    "toAccountRiskScore": 15
  }'
```

**预期结果：** `REJECTED - 余额不足`

#### 示例 4：VIP 紧急订单

```bash
curl -X POST http://localhost:8080/api/transfer/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "T004",
    "fromAccountId": "VIP001",
    "toAccountId": "ACC007",
    "amount": 50000.00,
    "fromAccountType": "vip",
    "fromAccountBalance": 100000.00,
    "fromAccountVerified": true,
    "fromAccountDailyTransferCount": 5,
    "fromAccountRiskScore": 10,
    "toAccountType": "corporate",
    "toAccountBalance": 50000.00,
    "toAccountVerified": true,
    "toAccountRiskScore": 5
  }'
```

**预期结果：** `APPROVED - 自动批准（VIP 优先）`

## 📊 返回结果示例

```json
{
  "orderId": "T001",
  "finalDecision": "APPROVED - 自动批准",
  "steps": [
    {
      "stepNumber": 1,
      "stepName": "创建实例数据",
      "description": "将请求数据转换为 RDF 三元组",
      "facts": [
        "创建转账订单: order_T001",
        "源账户 ACC001: 类型=personal, 余额=5000.00, 已验证=true",
        "目标账户 ACC002: 类型=personal, 已验证=true",
        "转账金额: 1000.00"
      ],
      "inferences": [],
      "result": "数据实例创建完成，共创建 1 个订单和 2 个账户"
    },
    {
      "stepNumber": 2,
      "stepName": "验证账户状态",
      "description": "检查账户是否通过验证",
      "facts": [
        "源账户验证状态: 已验证 ✓",
        "目标账户验证状态: 已验证 ✓"
      ],
      "inferences": [
        "推理: 双方账户均已验证，可以进行转账操作"
      ],
      "result": "通过 - 账户验证完整"
    },
    // ... 其他步骤
  ],
  "summary": "订单 T001 推理完成：\n- 共执行 6 个推理步骤\n- 最终决策: APPROVED - 自动批准\n- 生成推理结论: 8 条"
}
```

## 🔧 本体维护指南

### 添加新的账户类型

编辑 `transfer_order_ontology.owl`，在类定义部分添加：

```xml
<!-- 黄金账户 -->
<owl:Class rdf:about="http://example.org/transfer#GoldAccount">
    <rdfs:subClassOf rdf:resource="http://example.org/transfer#Account"/>
    <rdfs:label>黄金账户</rdfs:label>
</owl:Class>
```

### 添加新的属性

```xml
<!-- 账户信用等级 -->
<owl:DatatypeProperty rdf:about="http://example.org/transfer#creditLevel">
    <rdfs:domain rdf:resource="http://example.org/transfer#Account"/>
    <rdfs:range rdf:resource="http://www.w3.org/2001/XMLSchema#string"/>
    <rdfs:label>信用等级</rdfs:label>
</owl:DatatypeProperty>
```

### 修改后重新加载

```bash
curl -X POST http://localhost:8080/api/transfer/reload-ontology
```

## 🎯 推理规则说明

系统内置的推理规则：

| 规则 | 条件 | 结论 |
|------|------|------|
| 高风险-评分 | 账户风险评分 > 70 | 标记为高风险订单 |
| 高风险-频率 | 当日转账次数 >= 10 | 标记为高风险订单 |
| 高风险-金额 | 转账金额 > 50000 | 标记为高风险订单 |
| VIP优先 | 源账户类型 = VIP | 设为紧急优先级 |
| 大额紧急 | 转账金额 > 100000 | 设为紧急优先级 |
| 余额检查 | 余额 < 转账金额 | 直接拒绝 |
| 审核条件 | 高风险 OR 账户未验证 | 需要人工审核 |
| 自动批准 | 所有条件满足 | 自动批准 |

## 📁 项目结构

```
src/main/
├── java/com/example/loanjena/
│   ├── controller/
│   │   ├── LoanController.java          # 贷款评估控制器
│   │   └── TransferOrderController.java # 转账订单控制器 (新)
│   ├── model/
│   │   ├── LoanApplicationRequest.java
│   │   ├── TransferOrderRequest.java    # 转账请求模型 (新)
│   │   ├── ReasoningStep.java           # 推理步骤模型 (新)
│   │   └── ReasoningResult.java         # 推理结果模型 (新)
│   ├── service/
│   │   ├── LoanReasoningService.java
│   │   └── TransferReasoningService.java # 转账推理服务 (新)
│   └── LoanJenaApplication.java
└── resources/
    ├── application.properties
    └── transfer_order_ontology.owl       # OWL 本体文件 (新)
```

## 🔍 调试技巧

### 查看推理过程

每个步骤都包含：
- `facts`: 输入的事实
- `inferences`: 推理产生的结论
- `result`: 步骤结果

### 使用 Postman 测试

导入以下环境变量：
- `BASE_URL`: http://localhost:8080
- 使用 Swagger 导出 OpenAPI 规范

## 🚨 常见问题

**Q: 本体文件修改后不生效？**
A: 调用 `/api/transfer/reload-ontology` 重新加载

**Q: 如何添加自定义推理规则？**
A: 在 `TransferReasoningService` 中添加新的推理方法

**Q: 支持哪些推理机？**
A: 默认使用 OWL 推理机，可切换为 Pellet、HermiT 等

**Q: 本体文件可以放在其他位置吗？**
A: 可以，但需要修改加载路径。生产环境建议：
   - 配置文件路径：`application.properties`
   - 外部文件系统：便于热更新
   - 数据库存储：支持版本管理

## 📚 相关技术

- **Apache Jena**: RDF 和本体处理框架
- **OWL 2**: Web 本体语言
- **SPARQL**: RDF 查询语言
- **Spring Boot**: 应用框架

## 🎓 学习资源

- [Apache Jena 文档](https://jena.apache.org/documentation/)
- [OWL 2 规范](https://www.w3.org/TR/owl2-overview/)
- [Protégé 教程](https://protege.stanford.edu/publications/ontology101/ontology101.html)

---

**作者**: AI Assistant  
**更新时间**: 2025-12-09
