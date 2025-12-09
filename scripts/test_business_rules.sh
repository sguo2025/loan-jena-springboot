#!/bin/bash
# 业务规则验证测试脚本

BASE_URL="http://localhost:8080/api/business-rules"

echo "=========================================="
echo "业务规则验证测试"
echo "=========================================="
echo ""

# 测试1: 查询所有规则
echo "【测试1】查询所有业务规则"
echo "-------------------------------------------"
curl -X GET "${BASE_URL}/rules" \
  -H "Content-Type: application/json" | jq .
echo ""
echo ""

# 测试2: 查询涉诈规则详情
echo "【测试2】查询涉诈检查规则详情"
echo "-------------------------------------------"
curl -X GET "${BASE_URL}/rule/FraudCustomerCheckRule" \
  -H "Content-Type: application/json" | jq .
echo ""
echo ""

# 测试3: 查询欠费规则详情
echo "【测试3】查询欠费检查规则详情"
echo "-------------------------------------------"
curl -X GET "${BASE_URL}/rule/ArrearsCheckRule" \
  -H "Content-Type: application/json" | jq .
echo ""
echo ""

# 测试4: 涉诈客户检查 - 涉诈状态
echo "【测试4】涉诈客户检查 - 涉诈状态（应拦截）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/fraud" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST001",
    "custName": "张三",
    "custStatus": "FRAUD",
    "isSource": true
  }' | jq .
echo ""
echo ""

# 测试5: 涉诈客户检查 - 正常状态
echo "【测试5】涉诈客户检查 - 正常状态（应通过）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/fraud" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST002",
    "custName": "李四",
    "custStatus": "NORMAL",
    "isSource": true
  }' | jq .
echo ""
echo ""

# 测试6: 欠费检查 - 欠费状态
echo "【测试6】欠费检查 - 欠费状态（应拦截）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/arrears" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST003",
    "custName": "王五",
    "arrearsStatus": "ARREARS",
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试7: 欠费检查 - 无欠费状态
echo "【测试7】欠费检查 - 无欠费状态（应通过）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/arrears" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST004",
    "custName": "赵六",
    "arrearsStatus": "NO_ARREARS",
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试8: 综合验证 - 涉诈客户
echo "【测试8】综合验证 - 涉诈客户（应拦截，仅涉诈检查）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/all" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST005",
    "custName": "孙七",
    "custStatus": "FRAUD",
    "arrearsStatus": "NO_ARREARS",
    "isSource": true,
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试9: 综合验证 - 欠费客户
echo "【测试9】综合验证 - 欠费客户（应拦截，欠费检查）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/all" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST006",
    "custName": "周八",
    "custStatus": "NORMAL",
    "arrearsStatus": "ARREARS",
    "isSource": true,
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试10: 综合验证 - 涉诈+欠费客户
echo "【测试10】综合验证 - 涉诈+欠费客户（应拦截，仅涉诈检查）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/all" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST007",
    "custName": "吴九",
    "custStatus": "FRAUD",
    "arrearsStatus": "ARREARS",
    "isSource": true,
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试11: 综合验证 - 正常客户
echo "【测试11】综合验证 - 正常客户（应通过）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/all" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST008",
    "custName": "郑十",
    "custStatus": "NORMAL",
    "arrearsStatus": "NO_ARREARS",
    "isSource": true,
    "hasSubscription": true
  }' | jq .
echo ""
echo ""

# 测试12: 目标客户涉诈检查
echo "【测试12】目标客户涉诈检查 - 涉诈状态（应拦截）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/fraud" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST009",
    "custName": "钱十一",
    "custStatus": "FRAUD",
    "isSource": false
  }' | jq .
echo ""
echo ""

# 测试13: 无订阅的欠费检查
echo "【测试13】欠费检查 - 无订阅（应通过）"
echo "-------------------------------------------"
curl -X POST "${BASE_URL}/validate/arrears" \
  -H "Content-Type: application/json" \
  -d '{
    "custId": "CUST010",
    "custName": "陈十二",
    "arrearsStatus": "ARREARS",
    "hasSubscription": false
  }' | jq .
echo ""
echo ""

echo "=========================================="
echo "测试完成"
echo "=========================================="
