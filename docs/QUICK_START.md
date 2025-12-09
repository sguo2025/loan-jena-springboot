# 转账订单 OWL 本体推理系统 - 完整指南

## ✅ 系统已成功部署

您的转账订单智能推理系统已经成功创建并运行！

## 📁 项目结构

```
loan-jena-springboot/
├── src/main/
│   ├── java/com/example/loanjena/
│   │   ├── LoanJenaApplication.java              # 主应用
│   │   ├── controller/
│   │   │   ├── LoanController.java               # 贷款控制器
│   │   │   └── TransferOrderController.java      # 转账控制器 ✨
│   │   ├── model/
│   │   │   ├── LoanApplicationRequest.java
│   │   │   ├── TransferOrderRequest.java         # 转账请求模型 ✨
│   │   │   ├── ReasoningStep.java                # 推理步骤 ✨
│   │   │   └── ReasoningResult.java              # 推理结果 ✨
│   │   └── service/
│   │       ├── LoanReasoningService.java
│   │       └── TransferReasoningService.java     # 转账推理服务 ✨
│   └── resources/
│       ├── application.properties
│       └── transfer_order_ontology.owl           # OWL 本体文件 ✨
├── pom.xml
├── README_TRANSFER.md                            # 详细文档
└── test_transfer.sh                              # 测试脚本 ✨
```

## 🚀 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

### 2. 访问 Swagger API 文档

打开浏览器：`http://localhost:8080/swagger-ui.html`

## 💡 核心功能

### 6 步智能推理流程

```
1️⃣ 创建实例数据      → 转换为 RDF 三元组
2️⃣ 验证账户状态      → 检查身份验证
3️⃣ 评估风险等级      → 风险评分分析
4️⃣ 检查余额充足性    → 资金可用性
5️⃣ 确定订单类型      → 优先级判断
6️⃣ 最终决策          → 综合结论

结果：APPROVED / REJECTED / PENDING_REVIEW
```

## 📝 API 测试示例

### 示例 1: 正常订单（✅ 通过）

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

**返回结果**:
```json
{
  "orderId": "T001",
  "finalDecision": "APPROVED - 自动批准",
  "steps": [
    {
      "stepNumber": 1,
      "stepName": "创建实例数据",
      "facts": ["创建转账订单: order_T001", ...],
      "inferences": [],
      "result": "数据实例创建完成"
    },
    ...
  ],
  "summary": "订单 T001 推理完成：共执行 6 个推理步骤"
}
```

### 示例 2: 高风险订单（⚠️ 需审核）

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

**结果**: `PENDING_REVIEW - 等待人工审核`

### 示例 3: 余额不足（❌ 拒绝）

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

**结果**: `REJECTED - 余额不足`

## 🔧 OWL 本体维护

### 本体文件位置

```
src/main/resources/transfer_order_ontology.owl
```

### 为什么放在 resources 目录？

- ✅ 随 JAR 包一起部署
- ✅ 类路径自动加载
- ✅ Git 版本控制
- ✅ 环境一致性

### 如何修改本体？

#### 方法 1: 直接编辑（小改动）

直接修改 `transfer_order_ontology.owl` 文件，添加新的类或属性：

```xml
<!-- 添加新账户类型 -->
<owl:Class rdf:about="http://example.org/transfer#PremiumAccount">
    <rdfs:subClassOf rdf:resource="http://example.org/transfer#Account"/>
    <rdfs:label>高级账户</rdfs:label>
</owl:Class>
```

#### 方法 2: 使用 Protégé（推荐）

1. 下载 [Protégé](https://protege.stanford.edu/)
2. 打开本体文件进行可视化编辑
3. 保存并替换项目中的文件

#### 重新加载本体

```bash
curl -X POST http://localhost:8080/api/transfer/reload-ontology
```

## 📊 推理规则说明

| 规则类型 | 条件 | 结论 |
|---------|------|------|
| **高风险-评分** | 账户风险评分 > 70 | 标记为高风险订单 |
| **高风险-频率** | 当日转账次数 >= 10 | 标记为高风险订单 |
| **高风险-金额** | 转账金额 > 50000 | 标记为高风险订单 |
| **VIP 优先** | 源账户类型 = VIP | 设为紧急优先级 |
| **大额紧急** | 转账金额 > 100000 | 设为紧急优先级 |
| **余额检查** | 余额 < 转账金额 | 直接拒绝 |
| **审核条件** | 高风险 OR 账户未验证 | 需要人工审核 |
| **自动批准** | 所有条件满足 | 自动批准 |

## 🎯 技术架构

### 核心技术栈

- **Spring Boot 2.7.18**: Web 应用框架
- **Apache Jena 4.8.0**: RDF 和本体处理
- **OWL 2**: Web 本体语言
- **Java 11**: 编程语言

### 推理机制

1. **加载本体**: 从 resources 读取 OWL 文件
2. **创建实例**: 将请求转换为 RDF 三元组
3. **应用规则**: 基于本体定义执行推理
4. **生成结论**: 综合所有步骤做出决策

## 📚 完整测试

运行自动化测试脚本：

```bash
chmod +x test_transfer.sh
./test_transfer.sh
```

该脚本将执行 5 个测试用例，涵盖：
- 正常订单
- 高风险订单
- 余额不足
- VIP 订单
- 账户未验证

## 🔍 调试技巧

### 查看应用日志

```bash
tail -f app.log
```

### 查看推理详情

每个步骤包含：
- `facts`: 输入的事实
- `inferences`: 推理产生的结论
- `result`: 步骤结果

## 🌐 生产环境部署

### 打包应用

```bash
mvn clean package
```

### 运行 JAR

```bash
java -jar target/loan-jena-springboot-1.0.0.jar
```

### Docker 部署（可选）

```dockerfile
FROM openjdk:11-jre-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## ❓ 常见问题

**Q: 本体文件修改后不生效？**
A: 调用 `/api/transfer/reload-ontology` 重新加载

**Q: 如何添加自定义推理规则？**
A: 在 `TransferReasoningService` 中添加新的推理方法

**Q: 支持哪些推理机？**
A: 默认使用 OWL 推理机，可切换为 Pellet、HermiT 等

**Q: 本体文件可以放在其他位置吗？**
A: 可以，建议生产环境：
   - 配置文件路径: `application.properties`
   - 外部文件系统: 便于热更新
   - 数据库存储: 支持版本管理

## 📖 学习资源

- [Apache Jena 文档](https://jena.apache.org/documentation/)
- [OWL 2 规范](https://www.w3.org/TR/owl2-overview/)
- [Protégé 教程](https://protege.stanford.edu/publications/ontology101/ontology101.html)

## ✨ 下一步

1. **扩展本体**: 添加更多账户类型和规则
2. **集成数据库**: 持久化推理结果
3. **添加监控**: 集成 Prometheus/Grafana
4. **API 认证**: 添加 JWT 安全认证
5. **性能优化**: 缓存本体模型

---

**系统状态**: ✅ 运行正常
**端口**: 8080
**API 文档**: http://localhost:8080/swagger-ui.html

祝使用愉快！ 🎉
