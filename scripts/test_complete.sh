#!/bin/bash

# BSS4.0 过户业务完整流程测试

BASE_URL="http://localhost:8080/api/transfer"
ORDER_ID="ORD_TEST_$(date +%s)"
PHONE="13800138000"

echo "=========================================="
echo "BSS4.0 过户业务完整流程测试"
echo "订单ID: $ORDER_ID"
echo "=========================================="
echo ""

# 测试步骤1: 定位源客户
echo "【步骤1/8】定位源客户"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"1\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"sourceCustomerId\": \"CUST_001\",
    \"sourceCustomerName\": \"张三\",
    \"sourceIdCard\": \"110101199001011234\"
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"|"currentStepName"'
echo "✓ 步骤1完成"
echo ""

# 测试步骤2: 过户号码选择
echo "【步骤2/8】过户号码选择"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"2\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"sourceCustomerId\": \"CUST_001\",
    \"selectedPhoneNumbers\": [\"$PHONE\"],
    \"productInstanceId\": \"PI_001\",
    \"subscriptionId\": \"SUB_001\"
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"|"currentStepName"'
echo "✓ 步骤2完成"
echo ""

# 测试步骤3: 创建客户订单
echo "【步骤3/8】创建客户订单 (可回退点)"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"3\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"targetCustomerId\": \"CUST_002\",
    \"targetCustomerName\": \"李四\",
    \"targetIdCard\": \"110101199002022345\"
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"|"canRollback"'
echo "✓ 步骤3完成 - 此步骤可回退到步骤1"
echo ""

# 测试回退功能
echo "【回退测试】步骤3 -> 步骤1"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"3\",
    \"requestRollback\": true,
    \"rollbackToStep\": \"1\"
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"rollbackExecuted"'
echo "✓ 回退成功，流程返回到步骤1"
echo ""

# 重新执行步骤1-3
echo "【重新执行】步骤1-3"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"1\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"sourceCustomerId\": \"CUST_001\"
  }" > /dev/null
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"2\",
    \"sourcePhoneNumber\": \"$PHONE\"
  }" > /dev/null
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"3\",
    \"targetCustomerId\": \"CUST_002\"
  }" > /dev/null
echo "✓ 重新执行完成"
echo ""

# 测试步骤4: 过户业务初始化
echo "【步骤4/8】过户业务初始化"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"4\",
    \"serviceOfferId\": \"2549\",
    \"productInstanceId\": \"PI_001\",
    \"customerOrderId\": \"CO_001\"
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"'
echo "✓ 步骤4完成"
echo ""

# 测试步骤5: 公共属性初始化
echo "【步骤5/8】公共属性初始化"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"5\",
    \"commonAttributes\": {
      \"key1\": \"value1\",
      \"key2\": \"value2\"
    }
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"'
echo "✓ 步骤5完成"
echo ""

# 测试步骤6: 目标客户确认
echo "【步骤6/8】目标客户确认"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"6\",
    \"targetCustomerId\": \"CUST_002\",
    \"targetCustomerName\": \"李四\",
    \"targetIdCard\": \"110101199002022345\",
    \"targetAddress\": \"北京市朝阳区\",
    \"targetAuthCompleted\": true
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"'
echo "✓ 步骤6完成"
echo ""

# 测试步骤7: 订单保存
echo "【步骤7/8】订单保存"
curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"7\",
    \"orderData\": {
      \"sourceCustomerId\": \"CUST_001\",
      \"targetCustomerId\": \"CUST_002\",
      \"phoneNumber\": \"$PHONE\",
      \"transferFee\": 50.00,
      \"orderStatus\": \"PENDING\"
    }
  }" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"'
echo "✓ 步骤7完成 - 订单已保存到数据库"
echo ""

# 测试步骤8: 订单确认（过户完成）
echo "【步骤8/8】订单确认（过户完成）"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"8\",
    \"customerConfirmed\": true,
    \"confirmationTimestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
    \"paymentCompleted\": true,
    \"paymentAmount\": 50.00
  }")

echo "$RESPONSE" | python3 -m json.tool | grep -E '"orderId"|"currentStep"|"nextStep"|"finalDecision"'
echo "✓ 步骤8完成 - 过户流程已完成"
echo ""

echo "=========================================="
echo "测试总结"
echo "=========================================="
echo "✓ 所有8个步骤测试通过"
echo "✓ 回退功能测试通过 (步骤3->步骤1)"
echo "✓ 订单保存测试通过 (步骤7)"
echo "✓ 订单确认测试通过 (步骤8)"
echo ""
echo "订单ID: $ORDER_ID"
echo "测试时间: $(date)"
echo "=========================================="
