# loan-jena-springboot

## 工程代码介绍

### 📋 项目概览
这是一个 **基于 Apache Jena + Spring Boot 的贷款申请评估系统**，使用语义网络技术和SPARQL查询进行智能决策。

---

### 🏗️ 架构层次

```
LoanJenaApplication (主程序)
    ↓
LoanController (REST 接口层)
    ↓
LoanReasoningService (业务逻辑层)
    ↓
Apache Jena RDF/SPARQL (语义推理引擎)
```

---

### 🔄 核心业务流程

#### **1. 请求模型 (`LoanApplicationRequest`)**
```
输入参数：
  ├─ applicantId: 申请人ID (String)
  ├─ age: 年龄 (int)
  ├─ creditScore: 信用评分 (int)
  └─ isStudent: 是否学生 (boolean)
```

#### **2. REST 接口 (`LoanController`)**
- **端点**: `POST /api/loan/apply`
- **功能**: 接收贷款申请请求
- **返回**: 评估结果 (`"Accepted"` 或 `"Rejected"`)

#### **3. 评估逻辑 (`LoanReasoningService`)**

三个主要步骤：

**第一步 - 构建 RDF 数据模型**
```
创建 RDF 资源表示申请人信息：
├─ Applicant (申请人)
│  ├─ hasAge: 30
│  ├─ hasCreditScore: 700
│  └─ type: Student (如果是学生)
└─ Application (贷款申请)
   ├─ applicant: → Applicant
   └─ type: Application
```

**第二步 - 定义 SPARQL 查询规则**
```
评估规则：
1. 基础条件检查 (meetsBasic):
   ├─ age > 17
   └─ creditScore >= 600

2. 学生身份检查 (isStu):
   └─ 是否为学生

3. 最终决策:
   └─ IF (meetsBasic AND NOT isStu) 
      THEN "Accepted" 
      ELSE "Rejected"
```

**第三步 - 执行 SPARQL 查询**
```
Query Flow:
RDF Model → SPARQL Query → QueryExecution 
  → ResultSet → 提取 status 结果
```

---

### 📊 评估决策规则

| 条件 | 结果 |
|------|------|
| 年龄 > 17 && 信用评分 ≥ 600 && **非学生** | ✅ **Accepted** |
| 其他所有情况 | ❌ **Rejected** |

**特殊规则**：学生身份会导致拒绝（防止学生申请贷款）

---

### 📚 技术栈

| 组件 | 用途 |
|------|------|
| **Spring Boot 3.2.5** | Web 框架 |
| **Apache Jena 4.8.0** | RDF/语义网络处理 |
| **jena-core** | RDF 数据模型 |
| **jena-arq** | SPARQL 查询引擎 |
| **SpringDoc OpenAPI 2.0.4** | API 文档生成 |

---

### 🚀 快速开始

#### **编译项目**
```bash
mvn clean compile -DskipTests
```

#### **运行方式**
```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

---

### 🔗 API 使用

#### **贷款申请接口**
```bash
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{"applicantId":"Bob","age":30,"creditScore":700,"isStudent":false}'

# 响应: "Accepted"
```

---

### 📖 Swagger 文档

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API 文档 JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

在浏览器中打开 Swagger UI 地址，可以直观查看 API 文档并测试接口。

---

### 💡 设计特点

1. **语义网络方案**: 使用 RDF 表示知识，SPARQL 进行逻辑推理
2. **灵活可扩展**: 规则可在不修改代码的情况下调整
3. **API 文档化**: 集成 Swagger，便于测试和文档维护
4. **分层架构**: Controller → Service → 语义引擎，职责清晰