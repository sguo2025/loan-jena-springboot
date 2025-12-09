# 推理与执行架构讨论 - 2025-12-09

## 核心问题与解决方案

### 问题1: StepExecutorFactory中存在硬编码

**用户提问**：
> 这里为什么还有硬编码，不能根据owl文件动态创建？

**问题分析**：
在`StepExecutorFactory`中，存在硬编码的步骤注册：
```java
executors.put(1, createStep1Executor());
executors.put(2, createStep2Executor());
// ...
executors.put(8, createStep8Executor());
```

**解决方案（已实现）**：
- 创建`step-configurations.yaml`配置文件
- 从YAML动态加载步骤配置
- 通过`StepConfigurationLoader`加载配置
- `StepExecutorFactory`从配置动态创建执行器

---

### 问题2: 推理与执行的职责边界

**用户观点**：
> OWL文件中目前只定义了步骤的元数据（步骤号、名称、描述），但没有定义每个步骤的具体执行逻辑配置。
> 注意：推理引擎只需推理出步骤的元数据，具体执行逻辑是由调用推理引擎客户端智能体去组装调用，所有推理只管推理，执行要管执行。

**架构原则**：
1. **推理层**：从OWL推理元数据（步骤号、名称、描述、回退规则）
2. **执行层**：客户端智能体组装业务逻辑
3. **分离原则**：推理与执行完全分离

---

## 架构分层

### 当前正确的架构

```
┌─────────────────────────────────────────┐
│          OWL本体（推理层）               │
│  - 定义步骤类（Step1_LocateSourceCustomer）│
│  - 定义业务规则                          │
│  - 定义回退规则                          │
└─────────────────────────────────────────┘
               ↓ (推理)
┌─────────────────────────────────────────┐
│     TransferStepRegistry（推理层）       │
│  - loadStepsFromOntology()              │
│  - getTotalSteps()                      │
│  - getStepName()                        │
│  - canRollback()                        │
└─────────────────────────────────────────┘
               ↓ (元数据)
┌─────────────────────────────────────────┐
│  TransferBusinessReasoningService       │
│  （推理 + 执行编排）                     │
│  - 调用推理层获取元数据                   │
│  - 执行业务逻辑（客户端智能体）            │
└─────────────────────────────────────────┘
```

---

## 已验证的结论

### ✅ 当前架构已正确分离推理与执行

1. **OWL本体**：
   - ✅ 只定义步骤元数据
   - ✅ 不包含执行逻辑
   - ✅ 定义推理规则

2. **TransferStepRegistry**：
   - ✅ 从OWL推理步骤信息
   - ✅ 提供元数据查询接口
   - ✅ 不包含执行逻辑

3. **TransferBusinessReasoningService**：
   - ✅ 调用推理层获取元数据
   - ✅ 执行业务逻辑（客户端智能体）
   - ✅ 协调推理与执行

---

## 优化历程

### 第一次优化：动态OWL步骤加载
- 消除硬编码步骤名称
- 从OWL本体动态加载步骤元数据

### 第二次优化：动态步骤执行
- 消除硬编码执行循环
- 使用动态循环执行N个步骤

### 第三次优化：配置驱动执行器（可选）
- 创建`GenericStepExecutor`通用执行器
- 使用`StepExecutorFactory`工厂模式
- 从YAML配置创建执行器

### 最终决策：只保留推理层
**用户要求**：
> 只需要推理层

**当前状态**：
- ✅ 恢复到简洁的推理层架构
- ✅ 移除执行层配置文件（YAML、Factory等）
- ✅ 保持OWL推理 + Service执行的简洁模式

---

## 代码动态化建议

### 当前硬编码问题

**用户新需求**：
> ok，但是工程里尽量是动态代码，不要硬编码，因为owl可能会变化

**现存硬编码**：
1. 步骤名称数组（`STEP_NAMES`）- 应从OWL推理
2. 8个硬编码的`executeStepX()`方法 - 应动态化
3. 回退规则（步骤3→步骤1）- 应从OWL推理

### 建议的动态化方案

#### 方案1：动态步骤注册表（推荐）
```java
// 从OWL动态加载步骤
TransferStepRegistry registry = new TransferStepRegistry();
registry.loadStepsFromOntology(ontologyModel);

// 动态获取步骤信息
int totalSteps = registry.getTotalSteps();
String stepName = registry.getStepName(stepNumber);
boolean canRollback = registry.canRollback(stepNumber);
```

#### 方案2：动态步骤执行
```java
// 替代8个硬编码方法
for (int i = 1; i <= registry.getTotalSteps(); i++) {
    ReasoningStep step = registry.executeStep(i, request, model, order);
    result.addStep(step);
}
```

#### 方案3：从OWL推理回退规则
```owl
transfer:Step3_CreateCustomerOrder
    :canRollback true ;
    :rollbackToStep 1 .
```

---

## 技术栈

- **Apache Jena**: OWL推理引擎
- **Spring Boot**: 服务框架
- **OWL/Turtle**: 本体语言
- **TM Forum ODA**: 标准参考

---

## 待优化项

根据用户新需求，需要进一步动态化：

1. [ ] 消除`STEP_NAMES`硬编码数组
2. [ ] 消除8个`executeStepX()`硬编码方法
3. [ ] 从OWL动态推理回退规则
4. [ ] 支持OWL文件变更后自动适配
5. [ ] 动态步骤数量（不固定8步）

---

## 总结

### 正确的架构原则
✅ **推理引擎**：只从OWL推理元数据  
✅ **客户端智能体**：组装和执行业务逻辑  
✅ **推理与执行分离**：职责清晰  

### 下一步行动
根据用户新需求"尽量动态代码，不要硬编码"，需要：
1. 实现动态步骤注册表
2. 消除硬编码的步骤方法
3. 从OWL完全动态推理所有配置

---

*会话时间：2025-12-09*  
*讨论重点：推理与执行架构分离、动态化代码实现*
