#!/bin/bash

# BSS4.0 过户业务推理测试脚本
# 测试8步骤完整流程

BASE_URL="http://localhost:8080/api/transfer"
ORDER_ID="ORD_TEST_$(date +%s)"

echo "=========================================="
echo "BSS4.0 过户业务推理测试"
echo "订单ID: $ORDER_ID"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试步骤1: 定位源客户
echo -e "${BLUE}步骤1: 定位源客户${NC}"
echo "查询并鉴权源客户..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceSearchValue\": \"李三狗\",
    \"sourceSearchType\": \"B\",
    \"sourceCustId\": \"931282308426\",
    \"sourceCustName\": \"李三狗\",
    \"sourceCertType\": \"1\",
    \"sourceCertNumber\": \"440113199012018939\",
    \"sourceCommonRegionId\": \"930013101\",
    \"sourceApproveLevel\": \"29B\",
    \"sourceAuthPassed\": true
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤1完成${NC}"
echo ""
sleep 2

# 测试步骤2: 过户号码选择
echo -e "${BLUE}步骤2: 过户号码选择${NC}"
echo "查询产品实例并选择号码..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceCustName\": \"李三狗\",
    \"sourceCertType\": \"1\",
    \"sourceCertNumber\": \"440113199012018939\",
    \"sourceAuthPassed\": true,
    \"totalSubscriptionCount\": 2,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"selectedProdId\": \"100000045\",
    \"selectedProdName\": \"宽带\",
    \"selectedRoleId\": \"10200001\",
    \"selectedOfferInstId\": \"6133000050208\"
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤2完成${NC}"
echo ""
sleep 2

# 测试步骤3: 创建客户订单
echo -e "${BLUE}步骤3: 创建客户订单 (可回退点)${NC}"
echo "创建客户订单..."
CUST_ORDER_ID="CUST_ORD_$(date +%s)"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceCustName\": \"李三狗\",
    \"sourceCertType\": \"1\",
    \"sourceCertNumber\": \"440113199012018939\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"orderCreateTime\": \"$(date '+%Y-%m-%d %H:%M:%S')\",
    \"channelId\": \"CH001\",
    \"sceneCode\": \"TRANSFER\",
    \"operatorId\": \"OP12345\"
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤3完成${NC}"
echo -e "${YELLOW}⚠️  此步骤可回退到步骤1${NC}"
echo ""
sleep 2

# 测试回退功能
echo -e "${YELLOW}测试回退功能...${NC}"
read -p "是否测试回退到步骤1? (y/n): " test_rollback
if [ "$test_rollback" = "y" ]; then
    echo ""
    echo -e "${BLUE}执行回退: 步骤3 -> 步骤1${NC}"
    curl -s -X POST "$BASE_URL/business/reason" \
      -H "Content-Type: application/json" \
      -d "{
        \"orderId\": \"$ORDER_ID\",
        \"currentStep\": 3,
        \"custOrderId\": \"$CUST_ORDER_ID\",
        \"needRollback\": true,
        \"rollbackToStep\": 1,
        \"rollbackReason\": \"客户需要更换过户号码\"
      }" | jq '.'
    
    echo ""
    echo -e "${GREEN}✓ 回退成功，流程返回到步骤1${NC}"
    echo ""
    exit 0
fi

# 测试步骤4: 过户业务初始化
echo -e "${BLUE}步骤4: 过户业务初始化${NC}"
echo "初始化产品实例受理..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"serviceOfferId\": \"2549\",
    \"businessInitSuccess\": true
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤4完成${NC}"
echo ""
sleep 2

# 测试步骤5: 公共属性初始化
echo -e "${BLUE}步骤5: 公共属性初始化${NC}"
echo "初始化公共属性..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"businessInitSuccess\": true,
    \"commonAttrInitSuccess\": true
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤5完成${NC}"
echo ""
sleep 2

# 测试步骤6: 目标客户确认
echo -e "${BLUE}步骤6: 目标客户确认${NC}"
echo "查询并鉴权目标客户..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"businessInitSuccess\": true,
    \"commonAttrInitSuccess\": true,
    \"targetSearchValue\": \"张四\",
    \"targetSearchType\": \"B\",
    \"targetCustId\": \"931282308427\",
    \"targetCustName\": \"张四\",
    \"targetCertType\": \"1\",
    \"targetCertNumber\": \"360311199511062517\",
    \"targetAuthPassed\": true
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤6完成${NC}"
echo ""
sleep 2

# 测试步骤7: 订单保存
echo -e "${BLUE}步骤7: 订单保存${NC}"
echo "保存订单..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceCustName\": \"李三狗\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"businessInitSuccess\": true,
    \"commonAttrInitSuccess\": true,
    \"targetCustId\": \"931282308427\",
    \"targetCustName\": \"张四\",
    \"targetAuthPassed\": true,
    \"orderSaved\": true,
    \"orderSaveTime\": \"$(date '+%Y-%m-%d %H:%M:%S')\"
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤7完成${NC}"
echo ""
sleep 2

# 测试步骤8: 订单确认
echo -e "${BLUE}步骤8: 订单确认${NC}"
echo "收银台确认订单..."
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"sourceCustId\": \"931282308426\",
    \"sourceCustName\": \"李三狗\",
    \"sourceAuthPassed\": true,
    \"selectedAccNum\": \"931LZ0040613023\",
    \"selectedProdInstId\": \"613300070614\",
    \"custOrderId\": \"$CUST_ORDER_ID\",
    \"businessInitSuccess\": true,
    \"commonAttrInitSuccess\": true,
    \"targetCustId\": \"931282308427\",
    \"targetCustName\": \"张四\",
    \"targetAuthPassed\": true,
    \"orderSaved\": true,
    \"orderConfirmed\": true,
    \"orderConfirmTime\": \"$(date '+%Y-%m-%d %H:%M:%S')\",
    \"paymentAmount\": 0.00,
    \"payChannel\": \"COUNTER\",
    \"receiptNo\": \"RCP$(date +%Y%m%d%H%M%S)\"
  }" | jq '.'

echo ""
echo -e "${GREEN}✓ 步骤8完成${NC}"
echo ""
echo "=========================================="
echo -e "${GREEN}过户流程全部完成！${NC}"
echo "订单ID: $ORDER_ID"
echo "客户订单号: $CUST_ORDER_ID"
echo "=========================================="
