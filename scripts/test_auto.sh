#!/bin/bash

# BSS4.0 过户业务推理自动化测试脚本

BASE_URL="http://localhost:8080/api/transfer"
ORDER_ID="ORD_AUTO_$(date +%s)"
PHONE="13800138000"

echo "=========================================="
echo "BSS4.0 过户业务推理自动化测试"
echo "订单ID: $ORDER_ID"
echo "=========================================="
echo ""

# 测试步骤1: 定位源客户
echo "步骤1: 定位源客户"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"1\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"sourceCustomerId\": \"CUST_001\",
    \"sourceCustomerName\": \"张三\",
    \"sourceIdCard\": \"110101199001011234\"
  }")

CURRENT_STEP=$(echo $RESPONSE | grep -o '"currentStep":"[^"]*"' | cut -d'"' -f4)
NEXT_STEP=$(echo $RESPONSE | grep -o '"nextStep":"[^"]*"' | cut -d'"' -f4)
SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)

echo "当前步骤: $CURRENT_STEP"
echo "下一步骤: $NEXT_STEP"
echo "推理成功: $SUCCESS"
echo ""

if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤1完成"
    echo ""
else
    echo "✗ 步骤1失败"
    exit 1
fi

# 测试步骤2: 过户号码选择
echo "步骤2: 过户号码选择"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"2\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"sourceCustomerId\": \"CUST_001\",
    \"selectedPhoneNumbers\": [\"$PHONE\"],
    \"productInstanceId\": \"PI_001\",
    \"subscriptionId\": \"SUB_001\"
  }")

CURRENT_STEP=$(echo $RESPONSE | grep -o '"currentStep":"[^"]*"' | cut -d'"' -f4)
NEXT_STEP=$(echo $RESPONSE | grep -o '"nextStep":"[^"]*"' | cut -d'"' -f4)
SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)

echo "当前步骤: $CURRENT_STEP"
echo "下一步骤: $NEXT_STEP"
echo "推理成功: $SUCCESS"
echo ""

if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤2完成"
    echo ""
else
    echo "✗ 步骤2失败"
    exit 1
fi

# 测试步骤3: 创建客户订单
echo "步骤3: 创建客户订单 (可回退点)"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"3\",
    \"sourcePhoneNumber\": \"$PHONE\",
    \"targetCustomerId\": \"CUST_002\",
    \"targetCustomerName\": \"李四\",
    \"targetIdCard\": \"110101199002022345\"
  }")

CURRENT_STEP=$(echo $RESPONSE | grep -o '"currentStep":"[^"]*"' | cut -d'"' -f4)
NEXT_STEP=$(echo $RESPONSE | grep -o '"nextStep":"[^"]*"' | cut -d'"' -f4)
CAN_ROLLBACK=$(echo $RESPONSE | grep -o '"canRollback":[^,}]*' | cut -d':' -f2)
SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)

echo "当前步骤: $CURRENT_STEP"
echo "下一步骤: $NEXT_STEP"
echo "可回退: $CAN_ROLLBACK"
echo "推理成功: $SUCCESS"
echo ""

if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤3完成"
    if [ "$CAN_ROLLBACK" = "true" ]; then
        echo "⚠️  此步骤可回退到步骤1"
    fi
    echo ""
else
    echo "✗ 步骤3失败"
    exit 1
fi

# 测试回退功能
echo "测试回退功能: 步骤3 -> 步骤1"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"3\",
    \"requestRollback\": true,
    \"rollbackToStep\": \"1\"
  }")

CURRENT_STEP=$(echo $RESPONSE | grep -o '"currentStep":"[^"]*"' | cut -d'"' -f4)
ROLLBACK_SUCCESS=$(echo $RESPONSE | grep -o '"rollbackExecuted":[^,}]*' | cut -d':' -f2)

echo "回退后当前步骤: $CURRENT_STEP"
echo "回退成功: $ROLLBACK_SUCCESS"
echo ""

if [ "$ROLLBACK_SUCCESS" = "true" ]; then
    echo "✓ 回退成功，流程返回到步骤$CURRENT_STEP"
    echo ""
else
    echo "✗ 回退失败"
    exit 1
fi

# 继续测试步骤4-8
echo "继续测试步骤4: 获取目标客户信息"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"4\",
    \"targetCustomerId\": \"CUST_002\",
    \"targetCustomerName\": \"李四\",
    \"targetIdCard\": \"110101199002022345\",
    \"targetAddress\": \"北京市朝阳区\"
  }")

SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)
if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤4完成"
    echo ""
else
    echo "✗ 步骤4失败"
    exit 1
fi

echo "步骤5: 过户资格审核"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"5\",
    \"authorizationMethod\": \"SMS_CODE\",
    \"authorizationCode\": \"123456\",
    \"authorizationCompleted\": true
  }")

SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)
if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤5完成"
    echo ""
else
    echo "✗ 步骤5失败"
    exit 1
fi

echo "步骤6: 过户费用支付"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"6\",
    \"paymentAmount\": 50.00,
    \"paymentMethod\": \"ALIPAY\",
    \"paymentCompleted\": true,
    \"paymentTransactionId\": \"PAY_$(date +%s)\"
  }")

SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)
if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤6完成"
    echo ""
else
    echo "✗ 步骤6失败"
    exit 1
fi

echo "步骤7: 订单保存"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"7\",
    \"orderData\": {
      \"sourceCustomerId\": \"CUST_001\",
      \"targetCustomerId\": \"CUST_002\",
      \"phoneNumber\": \"$PHONE\",
      \"transferFee\": 50.00
    }
  }")

SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)
if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤7完成 - 订单已保存"
    echo ""
else
    echo "✗ 步骤7失败"
    exit 1
fi

echo "步骤8: 订单确认（过户完成）"
RESPONSE=$(curl -s -X POST "$BASE_URL/business/reason" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"currentStep\": \"8\",
    \"customerConfirmed\": true,
    \"confirmationTimestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"
  }")

SUCCESS=$(echo $RESPONSE | grep -o '"success":[^,}]*' | cut -d':' -f2)
NEXT_STEP=$(echo $RESPONSE | grep -o '"nextStep":"[^"]*"' | cut -d'"' -f4)

if [ "$SUCCESS" = "true" ]; then
    echo "✓ 步骤8完成 - 过户流程已完成"
    echo ""
else
    echo "✗ 步骤8失败"
    exit 1
fi

echo "=========================================="
echo "测试总结"
echo "=========================================="
echo "✓ 所有8个步骤测试通过"
echo "✓ 回退功能测试通过"
echo "✓ 订单保存和确认测试通过"
echo ""
echo "订单ID: $ORDER_ID"
echo "测试时间: $(date)"
echo "=========================================="
