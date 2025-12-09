# 流程推理 API 文档

## 概述

基于 OWL 本体的流程推理模型 REST API，用于从 OWL 文件中推理业务流程步骤的结构关系。

**注意**：本服务只执行逻辑推理，不执行业务调度、不运行流程实例、不做编排控制。

## 基础信息

- **Base URL**: `http://localhost:8080/api/process-reasoning`
- **Content-Type**: `application/json`
- **OWL 文件位置**: `src/main/resources/owl/transfer_order_ontology.owl`

## API 端点

### 1. 获取完整流程信息

获取流程的所有步骤、总步数、开始步骤和结束步骤。

**请求**
```
GET /api/process-reasoning/flow
```

**响应示例**
```json
{
  "totalSteps": 10,
  "startStepUri": "https://iwhalecloud.com/ontology/transfer#LocateSourceCustomerStep",
  "endStepUri": "https://iwhalecloud.com/ontology/transfer#ConfirmOrderStep",
  "steps": [...]
}
```

### 2. 获取流程总步数

返回流程中的步骤总数。

**请求**
```
GET /api/process-reasoning/flow/total-steps
```

**响应示例**
```json
10
```

### 3. 获取流程开始步骤

返回流程的第一个步骤（没有前驱的步骤）。

**请求**
```
GET /api/process-reasoning/flow/start-step
```

**响应示例**
```json
{
  "stepUri": "https://iwhalecloud.com/ontology/transfer#LocateSourceCustomerStep",
  "stepName": "LocateSourceCustomerStep",
  "label": "定位源客户步骤",
  "stepCode": "LOCATE_SOURCE",
  "previousSteps": [],
  "nextSteps": ["..."],
  "requiredEntities": ["..."],
  "producedEntities": ["..."],
  "mappedComponent": "...",
  "usedAPI": "...",
  "etomRef": "..."
}
```

### 4. 获取流程结束步骤

返回流程的最后一个步骤（没有后继的步骤）。

**请求**
```
GET /api/process-reasoning/flow/end-step
```

**响应同上**

### 5. 获取指定步骤详细信息

根据步骤URI或本地名称获取步骤的详细信息。

**请求**
```
GET /api/process-reasoning/step/{stepUri}
```

**路径参数**
- `stepUri`: 步骤URI或本地名称
  - 完整URI: `https://iwhalecloud.com/ontology/transfer#LocateSourceCustomerStep`
  - 本地名称: `LocateSourceCustomerStep`

**响应示例**
```json
{
  "stepUri": "https://iwhalecloud.com/ontology/transfer#LocateSourceCustomerStep",
  "stepName": "LocateSourceCustomerStep",
  "label": "定位源客户步骤",
  "comment": null,
  "stepNumber": null,
  "previousSteps": [],
  "nextSteps": [],
  "requiredEntities": [
    "https://iwhalecloud.com/ontology/transfer#SourceCustomer"
  ],
  "producedEntities": [
    "https://iwhalecloud.com/ontology/transfer#SourceCustomer"
  ],
  "mappedComponent": "https://iwhalecloud.com/ontology/transfer#PartyManagementComponent",
  "usedAPI": "https://iwhalecloud.com/ontology/transfer#ICustomerLocationService_listCustomers",
  "etomRef": "CRM.Customer Management > Identify Customer",
  "stepCode": "LOCATE_SOURCE"
}
```

**响应字段说明**
- `stepUri`: 步骤的完整URI
- `stepName`: 步骤的本地名称
- `label`: 步骤的标签（通常是中文描述）
- `comment`: 步骤的注释
- `stepNumber`: 步骤编号（如果可以从URI中提取）
- `previousSteps`: 前驱步骤URI列表（上一步）
- `nextSteps`: 后继步骤URI列表（下一步）
- `requiredEntities`: 该步骤所需的实体类型列表
- `producedEntities`: 该步骤产出的实体类型列表
- `mappedComponent`: 映射的ODA组件
- `usedAPI`: 使用的API
- `etomRef`: eTOM参考
- `stepCode`: 步骤代码

### 6. 获取指定步骤的前驱步骤

获取指定步骤的所有前驱步骤（上一步）信息。

**请求**
```
GET /api/process-reasoning/step/{stepUri}/previous
```

**响应示例**
```json
[
  {
    "stepUri": "...",
    "stepName": "...",
    ...
  }
]
```

### 7. 获取指定步骤的后继步骤

获取指定步骤的所有后继步骤（下一步）信息。

**请求**
```
GET /api/process-reasoning/step/{stepUri}/next
```

**响应示例**
```json
[
  {
    "stepUri": "...",
    "stepName": "...",
    ...
  }
]
```

## 数据模型

### ProcessFlowInfo

流程信息对象

| 字段 | 类型 | 描述 |
|------|------|------|
| totalSteps | Integer | 流程总步数 |
| startStepUri | String | 流程开始步骤URI |
| endStepUri | String | 流程结束步骤URI |
| steps | List<ProcessStepInfo> | 所有流程步骤列表 |

### ProcessStepInfo

流程步骤信息对象

| 字段 | 类型 | 描述 |
|------|------|------|
| stepUri | String | 步骤URI |
| stepName | String | 步骤名称 |
| label | String | 步骤标签 |
| comment | String | 步骤描述 |
| stepNumber | Integer | 步骤编号 |
| previousSteps | List<String> | 上一步骤URI列表 |
| nextSteps | List<String> | 下一步骤URI列表 |
| requiredEntities | List<String> | 所需实体类型列表 |
| producedEntities | List<String> | 产出实体类型列表 |
| mappedComponent | String | 映射的ODA组件 |
| usedAPI | String | 使用的API |
| etomRef | String | eTOM引用 |
| stepCode | String | 步骤代码 |

## 测试示例

使用提供的测试脚本：
```bash
./test_reasoning_api.sh
```

或手动测试：
```bash
# 获取流程总步数
curl http://localhost:8080/api/process-reasoning/flow/total-steps

# 获取特定步骤信息
curl http://localhost:8080/api/process-reasoning/step/LocateSourceCustomerStep

# 获取完整流程信息
curl http://localhost:8080/api/process-reasoning/flow | python3 -m json.tool
```

## 推理逻辑说明

本服务使用 Apache Jena 框架：
1. 加载 OWL 本体文件（Turtle 格式）
2. 使用推理机 (OWL_MEM_RULE_INF) 进行逻辑推理
3. 查询 ProcessStep 的子类
4. 提取限制条件（Restriction）中的信息：
   - `requiresEntity`: 所需实体
   - `producesEntity`: 产出实体
   - `precedes`: 后继步骤
   - `mapsToComponent`: 映射组件
   - `usesAPI`: 使用的API
5. 通过反向查询推理前驱步骤
6. 识别开始和结束步骤

## 注意事项

1. 服务启动时会自动加载 OWL 文件
2. 如果 OWL 文件格式错误，服务将启动失败
3. 当前实现只读取本体信息，不修改
4. 不执行任何业务逻辑或流程调度
5. 返回的所有URI都是完整的命名空间URI
