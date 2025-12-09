# 📁 项目结构说明

## 目录树

```
loan-jena-springboot/
│
├── 📄 pom.xml                          # Maven项目配置文件
│
├── 📚 文档文件
│   ├── README.md                       # 项目主文档
│   ├── README_ODA_TRANSFER.md          # ⭐ TM Forum ODA系统完整文档
│   ├── README_TRANSFER.md              # 原始转账系统文档
│   ├── QUICK_START.md                  # 快速入门指南
│   ├── TEST_RESULTS.md                 # ⭐ 详细测试结果报告
│   ├── DELIVERY_SUMMARY.md             # ⭐ 项目交付总结
│   └── PROJECT_STRUCTURE.md            # 本文件 - 项目结构说明
│
├── 🔧 测试脚本
│   ├── test_all_scenarios.sh           # ⭐ 5场景完整测试脚本
│   └── test_transfer.sh                # 原始简单测试脚本
│
├── 📁 src/main/java/com/example/loanjena/
│   │
│   ├── 🎯 controller/                  # REST API控制器层
│   │   ├── LoanController.java        # 原贷款推理API
│   │   └── TransferOrderController.java  # ⭐ 过户订单推理API
│   │
│   ├── 📦 model/                       # 数据模型层
│   │   ├── LoanApplicationRequest.java   # 贷款申请请求模型
│   │   ├── TransferOrderRequest.java     # ⭐ 过户订单请求模型
│   │   ├── ReasoningResult.java          # ⭐ 推理结果模型
│   │   └── ReasoningStep.java            # ⭐ 推理步骤模型
│   │
│   ├── 🧠 service/                     # 业务服务层
│   │   ├── LoanReasoningService.java     # 原贷款推理服务
│   │   └── TransferReasoningService.java # ⭐ 过户推理服务 (核心)
│   │
│   └── 🚀 LoanJenaApplication.java     # Spring Boot主启动类
│
├── 📁 src/main/resources/              # 资源文件目录
│   ├── application.properties          # Spring Boot配置
│   └── transfer_order_ontology.owl     # ⭐ TM Forum ODA本体文件 (Turtle格式)
│
└── 📁 target/                          # Maven编译输出目录
    └── classes/                        # 编译后的.class文件
```

## 核心文件说明

### 🌟 重点文件

#### 1. TransferReasoningService.java
**路径**: `src/main/java/com/example/loanjena/service/TransferReasoningService.java`

**作用**: 核心推理服务，实现6步推理流程

**关键方法**:
- `loadOntology()` - 加载TM Forum ODA本体
- `performReasoning()` - 执行完整推理流程
- `createInstanceData()` - 步骤1: 创建RDF实例
- `verifyAccountStatus()` - 步骤2: 客户鉴权
- `assessRiskLevel()` - 步骤3: 风险评估
- `checkBalanceSufficiency()` - 步骤4: 余额检查
- `determineOrderType()` - 步骤5: 订单分类
- `makeFinalDecision()` - 步骤6: 最终决策

**代码量**: ~450行  
**依赖**: Apache Jena 4.8.0

---

#### 2. transfer_order_ontology.owl
**路径**: `src/main/resources/transfer_order_ontology.owl`

**作用**: TM Forum ODA标准本体定义

**格式**: Turtle (TTL)  
**命名空间**: `https://iwhalecloud.com/ontology/transfer#`  
**行数**: 752行

**核心类定义**:
```turtle
# 客户类
transfer:SourceCustomer a owl:Class
transfer:TargetCustomer a owl:Class

# 订单类
transfer:TransferOrder a owl:Class

# 记录类
transfer:PaymentRecord a owl:Class
transfer:AuthorizationRecord a owl:Class

# 流程步骤类
transfer:ValidateCustomerStep a owl:Class
transfer:AssessRiskStep a owl:Class
transfer:CheckCreditStep a owl:Class
...

# 业务规则类
transfer:TransferEligibilityRule a owl:Class
transfer:HighRiskCustomerRule a owl:Class
transfer:LargeAmountRule a owl:Class
...
```

**维护方式**:
1. 直接编辑Turtle文件
2. 使用Protégé编辑器
3. 通过API热重载

---

#### 3. TransferOrderController.java
**路径**: `src/main/java/com/example/loanjena/controller/TransferOrderController.java`

**作用**: REST API控制器

**端点**:
- `POST /api/transfer/evaluate` - 评估过户订单
- `POST /api/transfer/reload-ontology` - 热重载本体
- `GET /api/transfer/health` - 健康检查

**代码量**: ~80行

---

#### 4. README_ODA_TRANSFER.md
**路径**: `/README_ODA_TRANSFER.md`

**作用**: 系统完整使用文档

**内容**:
- 系统概述
- 本体文件管理
- TM Forum ODA标准映射
- 推理步骤详解
- 业务规则库
- API文档
- 快速开始指南

**字数**: ~15000字

---

#### 5. test_all_scenarios.sh
**路径**: `/test_all_scenarios.sh`

**作用**: 完整的5场景测试脚本

**测试场景**:
1. 正常小额过户 (APPROVED)
2. VIP大额过户 (PENDING_REVIEW)
3. 余额不足 (REJECTED)
4. 高风险频繁过户 (PENDING_REVIEW)
5. 目标未验证 (PENDING_REVIEW)

**代码量**: ~250行

---

## 文件类型统计

### Java代码
```
TransferReasoningService.java    ~450行  ⭐ 核心推理引擎
TransferOrderController.java     ~80行   REST API
TransferOrderRequest.java        ~100行  数据模型
ReasoningResult.java             ~50行   结果模型
ReasoningStep.java               ~60行   步骤模型
-------------------------------------------
总计                             ~740行
```

### 本体文件
```
transfer_order_ontology.owl      752行   ⭐ TM Forum ODA本体
```

### 文档
```
README_ODA_TRANSFER.md           ~1500行 ⭐ 系统完整文档
TEST_RESULTS.md                  ~800行  ⭐ 测试报告
DELIVERY_SUMMARY.md              ~700行  ⭐ 交付总结
QUICK_START.md                   ~200行  快速入门
-------------------------------------------
总计                             ~3200行
```

### 测试脚本
```
test_all_scenarios.sh            ~250行  ⭐ 完整测试
test_transfer.sh                 ~30行   简单测试
-------------------------------------------
总计                             ~280行
```

## 依赖关系图

```
┌─────────────────────────────────────────────┐
│   TransferOrderController.java              │
│   (REST API Layer)                          │
└───────────────┬─────────────────────────────┘
                │
                │ @Autowired
                ▼
┌─────────────────────────────────────────────┐
│   TransferReasoningService.java             │
│   (Business Logic Layer)                    │
│   ├─ loadOntology()                         │
│   ├─ performReasoning()                     │
│   ├─ createInstanceData()                   │
│   ├─ verifyAccountStatus()                  │
│   ├─ assessRiskLevel()                      │
│   ├─ checkBalanceSufficiency()              │
│   ├─ determineOrderType()                   │
│   └─ makeFinalDecision()                    │
└───────────────┬─────────────────────────────┘
                │
                │ uses
                ▼
┌─────────────────────────────────────────────┐
│   Apache Jena 4.8.0                         │
│   (Reasoning Engine)                        │
│   ├─ OntModel                               │
│   ├─ InfModel                               │
│   ├─ Reasoner (OWL_MEM_RDFS_INF)            │
│   └─ RDF API                                │
└───────────────┬─────────────────────────────┘
                │
                │ reads
                ▼
┌─────────────────────────────────────────────┐
│   transfer_order_ontology.owl               │
│   (Knowledge Base)                          │
│   ├─ Classes (Customer, Order, Record...)  │
│   ├─ Properties (custId, amount...)         │
│   ├─ Rules (Business Rules)                 │
│   └─ TM Forum ODA Mapping                   │
└─────────────────────────────────────────────┘
```

## 数据流图

```
HTTP Request
     │
     ▼
TransferOrderController
     │
     ▼
TransferReasoningService
     │
     ├─ Step 1: createInstanceData()
     │   └─> 创建RDF三元组
     │
     ├─ Step 2: verifyAccountStatus()
     │   └─> 创建AuthorizationRecord
     │
     ├─ Step 3: assessRiskLevel()
     │   └─> 应用风险规则
     │
     ├─ Step 4: checkBalanceSufficiency()
     │   └─> 创建PaymentRecord
     │
     ├─ Step 5: determineOrderType()
     │   └─> 设置orderPriority
     │
     └─ Step 6: makeFinalDecision()
         └─> 设置orderStatus
     │
     ▼
ReasoningResult
     │
     ▼
JSON Response
```

## 技术栈层次

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   - Swagger UI                      │
│   - REST API (Spring MVC)           │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Business Logic Layer              │
│   - TransferReasoningService        │
│   - 6-Step Reasoning Flow           │
│   - Business Rules Engine           │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Semantic Layer                    │
│   - Apache Jena                     │
│   - OWL Reasoner                    │
│   - RDF Triple Store                │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Knowledge Layer                   │
│   - transfer_order_ontology.owl     │
│   - TM Forum ODA Ontology           │
│   - Business Rules (OWL Classes)    │
└─────────────────────────────────────┘
```

## 关键路径

### 开发路径
```
1. pom.xml (依赖配置)
   └─> Apache Jena 4.8.0

2. transfer_order_ontology.owl (本体定义)
   └─> TM Forum ODA标准

3. TransferReasoningService.java (推理实现)
   └─> 6步推理流程

4. TransferOrderController.java (API暴露)
   └─> REST端点

5. test_all_scenarios.sh (测试验证)
   └─> 5个测试场景
```

### 运行时路径
```
mvn spring-boot:run
   └─> LoanJenaApplication.main()
       └─> Spring容器启动
           └─> TransferReasoningService初始化
               └─> loadOntology()
                   └─> 加载transfer_order_ontology.owl
                       └─> 创建OWL推理器
                           └─> 系统就绪 (监听8080端口)
```

## 维护建议

### 修改业务规则
```
1. 编辑 transfer_order_ontology.owl
2. 修改对应的Java方法（如有需要）
3. 调用 POST /api/transfer/reload-ontology
4. 运行 test_all_scenarios.sh 验证
```

### 添加新推理步骤
```
1. 在 TransferReasoningService.java 中添加新方法
2. 在 performReasoning() 中调用新方法
3. 更新 ReasoningStep 的步骤编号
4. 更新文档和测试脚本
```

### 扩展到其他业务
```
1. 复制 transfer_order_ontology.owl 为新本体
2. 创建新的 Service 类（如 AccountMigrationService）
3. 创建新的 Controller 类
4. 复用 ReasoningResult/ReasoningStep 模型
```

---

**最后更新**: 2025-12-09  
**文档版本**: v1.0
