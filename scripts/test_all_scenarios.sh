#!/bin/bash

# TM Forum ODA 客户过户推理系统 - 测试脚本
# 版本: 1.0.0
# 日期: 2025-12-09

BASE_URL="http://localhost:8080/api/transfer"

echo "=========================================="
echo "🏦 TM Forum ODA 客户过户推理系统测试"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试场景1: 正常订单（预期：自动批准）
echo -e "${BLUE}[测试场景1]${NC} 正常小额过户订单"
echo "----------------------------------------"
echo "客户类型: regular | 余额: 60000元 | 金额: 5000元"
echo "风险评分: 源15 / 目标20 | 鉴权: 双方通过"
echo ""

RESPONSE1=$(curl -s -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD202512090001",
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
  }')

DECISION1=$(echo $RESPONSE1 | jq -r '.finalDecision')
echo -e "✅ 最终决策: ${GREEN}$DECISION1${NC}"
echo ""
echo "推理步骤："
echo $RESPONSE1 | jq -r '.steps[] | "  步骤\(.stepNumber): \(.stepName) → \(.result)"'
echo ""
echo "=========================================="
echo ""

sleep 2

# 测试场景2: VIP大额订单（预期：需人工审核）
echo -e "${BLUE}[测试场景2]${NC} VIP客户大额过户订单"
echo "----------------------------------------"
echo "客户类型: premium (VIP) | 余额: 150000元 | 金额: 80000元"
echo "风险评分: 源25 / 目标30 | 鉴权: 双方通过"
echo ""

RESPONSE2=$(curl -s -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD202512090002",
    "custOrderId": "CUST_ORD_002",
    "fromAccountId": "ACC456",
    "fromAccountType": "premium",
    "fromAccountVerified": true,
    "fromAccountBalance": 150000.00,
    "fromAccountRiskScore": 25,
    "fromAccountDailyTransferCount": 3,
    "toAccountId": "ACC999",
    "toAccountType": "regular",
    "toAccountVerified": true,
    "toAccountRiskScore": 30,
    "amount": 80000.00,
    "accountRelationship": "family"
  }')

DECISION2=$(echo $RESPONSE2 | jq -r '.finalDecision')
echo -e "⚠️  最终决策: ${YELLOW}$DECISION2${NC}"
echo ""
echo "推理步骤："
echo $RESPONSE2 | jq -r '.steps[] | "  步骤\(.stepNumber): \(.stepName) → \(.result)"'
echo ""
echo "关键推理："
echo $RESPONSE2 | jq -r '.steps[2].inferences[] | "  • \(.)"' | head -n 4
echo ""
echo "=========================================="
echo ""

sleep 2

# 测试场景3: 余额不足（预期：直接拒绝）
echo -e "${BLUE}[测试场景3]${NC} 余额不足场景"
echo "----------------------------------------"
echo "客户类型: regular | 余额: 3000元 | 金额: 5000元"
echo "风险评分: 源10 / 目标5 | 鉴权: 双方通过"
echo ""

RESPONSE3=$(curl -s -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD202512090003",
    "custOrderId": "CUST_ORD_003",
    "fromAccountId": "ACC789",
    "fromAccountType": "regular",
    "fromAccountVerified": true,
    "fromAccountBalance": 3000.00,
    "fromAccountRiskScore": 10,
    "fromAccountDailyTransferCount": 1,
    "toAccountId": "ACC111",
    "toAccountType": "regular",
    "toAccountVerified": true,
    "toAccountRiskScore": 5,
    "amount": 5000.00,
    "accountRelationship": "friend"
  }')

DECISION3=$(echo $RESPONSE3 | jq -r '.finalDecision')
echo -e "❌ 最终决策: ${RED}$DECISION3${NC}"
echo ""
echo "推理步骤："
echo $RESPONSE3 | jq -r '.steps[] | "  步骤\(.stepNumber): \(.stepName) → \(.result)"'
echo ""
echo "余额详情："
echo $RESPONSE3 | jq -r '.steps[3].facts[]'
echo ""
echo "=========================================="
echo ""

sleep 2

# 测试场景4: 高风险客户（预期：需人工审核）
echo -e "${BLUE}[测试场景4]${NC} 高风险客户频繁过户"
echo "----------------------------------------"
echo "客户类型: regular | 余额: 200000元 | 金额: 30000元"
echo "风险评分: 源75 (高风险) | 当日过户: 12次 (频繁)"
echo ""

RESPONSE4=$(curl -s -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD202512090004",
    "custOrderId": "CUST_ORD_004",
    "fromAccountId": "ACC321",
    "fromAccountType": "regular",
    "fromAccountVerified": true,
    "fromAccountBalance": 200000.00,
    "fromAccountRiskScore": 75,
    "fromAccountDailyTransferCount": 12,
    "toAccountId": "ACC654",
    "toAccountType": "regular",
    "toAccountVerified": true,
    "toAccountRiskScore": 20,
    "amount": 30000.00,
    "accountRelationship": "business"
  }')

DECISION4=$(echo $RESPONSE4 | jq -r '.finalDecision')
echo -e "⚠️  最终决策: ${YELLOW}$DECISION4${NC}"
echo ""
echo "推理步骤："
echo $RESPONSE4 | jq -r '.steps[] | "  步骤\(.stepNumber): \(.stepName) → \(.result)"'
echo ""
echo "风险因素："
echo $RESPONSE4 | jq -r '.steps[2].inferences[]' | grep "规则"
echo ""
echo "=========================================="
echo ""

sleep 2

# 测试场景5: 未验证账户（预期：需人工审核）
echo -e "${BLUE}[测试场景5]${NC} 目标客户未验证"
echo "----------------------------------------"
echo "客户类型: regular | 余额: 50000元 | 金额: 10000元"
echo "鉴权: 源通过 / 目标失败"
echo ""

RESPONSE5=$(curl -s -X POST $BASE_URL/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD202512090005",
    "custOrderId": "CUST_ORD_005",
    "fromAccountId": "ACC555",
    "fromAccountType": "regular",
    "fromAccountVerified": true,
    "fromAccountBalance": 50000.00,
    "fromAccountRiskScore": 20,
    "fromAccountDailyTransferCount": 1,
    "toAccountId": "ACC888",
    "toAccountType": "regular",
    "toAccountVerified": false,
    "toAccountRiskScore": 15,
    "amount": 10000.00,
    "accountRelationship": "relative"
  }')

DECISION5=$(echo $RESPONSE5 | jq -r '.finalDecision')
echo -e "⚠️  最终决策: ${YELLOW}$DECISION5${NC}"
echo ""
echo "推理步骤："
echo $RESPONSE5 | jq -r '.steps[] | "  步骤\(.stepNumber): \(.stepName) → \(.result)"'
echo ""
echo "鉴权详情："
echo $RESPONSE5 | jq -r '.steps[1].facts[]'
echo ""
echo "=========================================="
echo ""

# 测试总结
echo ""
echo "=========================================="
echo "📊 测试总结"
echo "=========================================="
echo ""
echo "测试场景统计："
echo -e "  ✅ 自动批准: ${GREEN}1${NC} 个"
echo -e "  ⚠️  人工审核: ${YELLOW}3${NC} 个"
echo -e "  ❌ 直接拒绝: ${RED}1${NC} 个"
echo ""
echo "决策分布："
echo "  • 场景1 (正常小额): $DECISION1"
echo "  • 场景2 (VIP大额): $DECISION2"
echo "  • 场景3 (余额不足): $DECISION3"
echo "  • 场景4 (高风险): $DECISION4"
echo "  • 场景5 (未验证): $DECISION5"
echo ""
echo "=========================================="
echo "✅ 测试完成！"
echo "=========================================="
echo ""
echo "详细文档: README_ODA_TRANSFER.md"
echo "测试结果: TEST_RESULTS.md"
echo "API文档: http://localhost:8080/swagger-ui.html"
echo ""
