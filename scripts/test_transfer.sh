#!/bin/bash

# 转账订单推理系统测试脚本

BASE_URL="http://localhost:8080/api/transfer"

echo "=========================================="
echo "转账订单推理系统 - 测试脚本"
echo "=========================================="
echo ""

# 检查服务是否运行
echo "1. 检查服务状态..."
curl -s $BASE_URL/health
echo -e "\n"

# 测试案例 1: 正常订单
echo "=========================================="
echo "测试案例 1: 正常订单（应该通过）"
echo "=========================================="
curl -X POST $BASE_URL/evaluate \
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
  }' | python3 -m json.tool
echo -e "\n"

# 测试案例 2: 高风险订单
echo "=========================================="
echo "测试案例 2: 高风险订单（需要审核）"
echo "=========================================="
curl -X POST $BASE_URL/evaluate \
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
  }' | python3 -m json.tool
echo -e "\n"

# 测试案例 3: 余额不足
echo "=========================================="
echo "测试案例 3: 余额不足（应该拒绝）"
echo "=========================================="
curl -X POST $BASE_URL/evaluate \
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
  }' | python3 -m json.tool
echo -e "\n"

# 测试案例 4: VIP 订单
echo "=========================================="
echo "测试案例 4: VIP 紧急订单（应该通过）"
echo "=========================================="
curl -X POST $BASE_URL/evaluate \
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
  }' | python3 -m json.tool
echo -e "\n"

# 测试案例 5: 账户未验证
echo "=========================================="
echo "测试案例 5: 源账户未验证（需要审核）"
echo "=========================================="
curl -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "T005",
    "fromAccountId": "ACC008",
    "toAccountId": "ACC009",
    "amount": 3000.00,
    "fromAccountType": "personal",
    "fromAccountBalance": 10000.00,
    "fromAccountVerified": false,
    "fromAccountDailyTransferCount": 1,
    "fromAccountRiskScore": 40,
    "toAccountType": "personal",
    "toAccountBalance": 5000.00,
    "toAccountVerified": true,
    "toAccountRiskScore": 20
  }' | python3 -m json.tool
echo -e "\n"

echo "=========================================="
echo "测试完成！"
echo "=========================================="
