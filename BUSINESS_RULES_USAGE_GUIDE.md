# 业务规则验证 - 使用指南

## 快速开始

### 1. 启动应用
```bash
cd /workspaces/loan-jena-springboot
mvn spring-boot:run
```

### 2. 运行测试
```bash
# 运行Shell脚本测试
./scripts/test_business_rules.sh

# 或运行JUnit测试
mvn test -Dtest=BusinessRuleValidationServiceTest
```

## API使用示例

### 1. 涉诈客户检查

**检查涉诈状态客户**
```bash
curl -X POST http://localhost:8080/api/business-rules/validate/fraud \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST001",
    "custName": "张三",
    "custStatus": "FRAUD",
    "isSource": true
  }'
```

**响应**:
```json
{
  "success": false,
  "blocked": true,
  "blockType": "STOP_ALL_OPERATIONS",
  "message": "涉诈用户不允许办理任何业务，待涉诈解除后方可继续办理"
}
```

**检查正常状态客户**
```bash
curl -X POST http://localhost:8080/api/business-rules/validate/fraud \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST002",
    "custName": "李四",
    "custStatus": "NORMAL",
    "isSource": true
  }'
```

**响应**:
```json
{
  "success": true,
  "blocked": false,
  "message": "客户状态正常，允许办理业务"
}
```

### 2. 客户欠费检查

**检查欠费客户**
```bash
curl -X POST http://localhost:8080/api/business-rules/validate/arrears \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST003",
    "custName": "王五",
    "arrearsStatus": "ARREARS",
    "hasSubscription": true
  }'
```

**响应**:
```json
{
  "success": false,
  "blocked": true,
  "blockType": "STOP_TRANSFER",
  "message": "用户存在欠费，不允许办理过户业务，请先缴清费用"
}
```

### 3. 综合验证

**综合检查客户**
```bash
curl -X POST http://localhost:8080/api/business-rules/validate/all \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST008",
    "custName": "郑十",
    "custStatus": "NORMAL",
    "arrearsStatus": "NO_ARREARS",
    "isSource": true,
    "hasSubscription": true
  }'
```

**响应**:
```json
{
  "success": true,
  "validationResults": [
    {
      "ruleCode": "FraudCustomerCheckRule",
      "passed": true,
      "blocked": false,
      "message": "客户状态正常，允许办理业务"
    },
    {
      "ruleCode": "ArrearsCheckRule",
      "passed": true,
      "blocked": false,
      "message": "客户无欠费，允许办理过户业务"
    }
  ]
}
```

### 4. 查询规则信息

**查询涉诈规则**
```bash
curl http://localhost:8080/api/business-rules/rule/FraudCustomerCheckRule
```

**查询所有规则**
```bash
curl http://localhost:8080/api/business-rules/rules
```

## 业务场景

### 场景1: 正常过户流程
```json
{
  "custStatus": "NORMAL",
  "arrearsStatus": "NO_ARREARS",
  "hasSubscription": true
}
→ ✅ 所有检查通过，允许办理过户
```

### 场景2: 涉诈客户拦截
```json
{
  "custStatus": "FRAUD",
  "arrearsStatus": "NO_ARREARS"
}
→ ❌ 涉诈检查失败，拦截所有业务操作
```

### 场景3: 欠费客户拦截
```json
{
  "custStatus": "NORMAL",
  "arrearsStatus": "ARREARS",
  "hasSubscription": true
}
→ ❌ 欠费检查失败，拦截过户业务
```

### 场景4: 复合问题优先级
```json
{
  "custStatus": "FRAUD",
  "arrearsStatus": "ARREARS"
}
→ ❌ 涉诈优先级最高，只进行涉诈检查即返回
```

## 参数说明

### 涉诈检查参数
- `custId`: 客户ID (必填)
- `custName`: 客户名称 (必填)
- `custStatus`: 客户状态 (必填)
  - `NORMAL`: 正常
  - `FRAUD`: 涉诈
  - `SUSPENDED`: 暂停
- `isSource`: 是否源客户 (可选，默认true)

### 欠费检查参数
- `custId`: 客户ID (必填)
- `custName`: 客户名称 (必填)
- `arrearsStatus`: 欠费状态 (必填)
  - `NO_ARREARS`: 无欠费
  - `ARREARS`: 欠费
- `hasSubscription`: 是否有订阅 (可选，默认true)

### 综合验证参数
包含以上所有参数

## 响应字段说明

### 基础响应
- `success`: 验证是否通过
- `ruleCode`: 规则编码
- `customerId`: 客户ID
- `customerName`: 客户名称
- `blocked`: 是否被拦截
- `blockType`: 拦截类型
  - `STOP_ALL_OPERATIONS`: 停止所有业务
  - `STOP_TRANSFER`: 仅停止过户
- `message`: 提示信息

### 综合验证响应
- `validationResults`: 验证结果数组
  - `ruleCode`: 规则编码
  - `passed`: 是否通过
  - `blocked`: 是否被拦截
  - `blockType`: 拦截类型
  - `message`: 提示信息

## 集成示例

### Java集成
```java
@Autowired
private BusinessRuleValidationService validationService;

// 涉诈检查
ValidationResult fraudResult = validationService.validateFraudCheck(
    custId, custName, custStatus, isSource
);

if (!fraudResult.isPassed()) {
    throw new BusinessException(fraudResult.getMessage());
}

// 欠费检查
ValidationResult arrearsResult = validationService.validateArrearsCheck(
    custId, custName, arrearsStatus, hasSubscription
);

if (!arrearsResult.isPassed()) {
    throw new BusinessException(arrearsResult.getMessage());
}

// 综合验证
List<ValidationResult> results = validationService.validateAll(
    custId, custName, custStatus, arrearsStatus, isSource, hasSubscription
);

boolean allPassed = results.stream().allMatch(ValidationResult::isPassed);
```

### JavaScript集成
```javascript
// 涉诈检查
const fraudCheck = async (customer) => {
  const response = await fetch('http://localhost:8080/api/business-rules/validate/fraud', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      custId: customer.id,
      custName: customer.name,
      custStatus: customer.status,
      isSource: true
    })
  });
  return await response.json();
};

// 综合验证
const validateCustomer = async (customer) => {
  const response = await fetch('http://localhost:8080/api/business-rules/validate/all', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      custId: customer.id,
      custName: customer.name,
      custStatus: customer.status,
      arrearsStatus: customer.arrearsStatus,
      isSource: true,
      hasSubscription: true
    })
  });
  const result = await response.json();
  
  if (!result.success) {
    const failedRule = result.validationResults.find(r => !r.passed);
    alert(failedRule.message);
    return false;
  }
  
  return true;
};
```

## 故障排查

### 问题1: 端口被占用
```bash
# 查找占用8080端口的进程
lsof -i :8080

# 停止进程
pkill -f spring-boot:run
```

### 问题2: 本体加载失败
检查文件路径: `src/main/resources/owl/transfer_order_ontology.owl`

### 问题3: 规则不生效
1. 检查本体文件中规则定义是否正确
2. 查看应用日志确认规则加载情况
3. 确认请求参数格式正确

## 性能建议

1. **缓存优化**: 本体模型会被缓存，首次加载较慢
2. **批量验证**: 使用综合验证接口减少网络请求
3. **异步处理**: 对于非关键路径可考虑异步验证

## 相关文档

- 测试报告: `BUSINESS_RULES_TEST_REPORT.md`
- 测试用例: `test_business_rules.json`
- API文档: `API_DOCUMENTATION.md`
- 项目结构: `docs/PROJECT_STRUCTURE.md`
