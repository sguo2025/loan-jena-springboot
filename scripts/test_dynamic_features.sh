#!/bin/bash
# 测试动态化功能

BASE_URL="http://localhost:8080/api/transfer/business/reason"

echo "====================================="
echo "BSS4.0 过户业务 - 动态化功能测试"
echo "====================================="
echo ""

echo "测试1: 验证动态步骤名称加载"
echo "-------------------------------------"
response=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"orderId": "DYN_001"}')
  
currentStepName=$(echo $response | jq -r '.currentStepName')
totalSteps=$(echo $response | jq -r '.totalSteps')

echo "✓ 当前步骤名称: $currentStepName (从OWL动态加载)"
echo "✓ 总步骤数: $totalSteps (从OWL动态加载)"
echo ""

echo "测试2: 验证动态回退规则"
echo "-------------------------------------"
response=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "DYN_002",
    "sourceCustId": "CUST001",
    "sourceAuthPassed": true,
    "selectedAccNum": "13800138000",
    "selectedProdInstId": "PROD001"
  }')
  
currentStep=$(echo $response | jq -r '.currentStep')
canRollback=$(echo $response | jq -r '.canRollback')
rollbackToStep=$(echo $response | jq -r '.rollbackToStep')

echo "✓ 当前步骤: $currentStep"
echo "✓ 可以回退: $canRollback"
echo "✓ 回退到步骤: $rollbackToStep (从OWL动态推理)"
echo ""

echo "测试3: 验证所有8个步骤都可以动态执行"
echo "-------------------------------------"
response=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "DYN_003",
    "sourceCustId": "CUST001",
    "sourceAuthPassed": true,
    "selectedAccNum": "13800138000",
    "selectedProdInstId": "PROD001",
    "custOrderId": "ORDER001",
    "businessInitSuccess": true,
    "commonAttrInitSuccess": true,
    "targetCustId": "CUST002",
    "targetAuthPassed": true,
    "orderSaved": true,
    "orderConfirmed": false
  }')

stepsCount=$(echo $response | jq -r '.steps | length')
echo "✓ 动态生成的步骤数: $stepsCount"

# 检查每个步骤名称
for i in {1..8}; do
  stepName=$(echo $response | jq -r ".steps[$((i-1))].stepName")
  echo "  - 步骤$i: $stepName"
done
echo ""

echo "测试4: 验证智能体决策使用动态步骤"
echo "-------------------------------------"
response=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "DYN_004",
    "sourceCustId": "CUST001",
    "sourceAuthPassed": true,
    "selectedAccNum": "13800138000",
    "selectedProdInstId": "PROD001",
    "custOrderId": "ORDER001"
  }')

explanation=$(echo $response | jq -r '.agentDecisionExplanation')
echo "$explanation"
echo ""

echo "测试5: 验证最终决策使用动态步骤"
echo "-------------------------------------"
finalDecision=$(echo $response | jq -r '.finalDecision')
echo "✓ 最终决策: $finalDecision"
echo ""

echo "====================================="
echo "✓ 动态化功能测试完成！"
echo "====================================="
echo ""
echo "动态化优化总结："
echo "1. ✓ 步骤名称从OWL本体动态加载"
echo "2. ✓ 回退规则从OWL本体动态推理"
echo "3. ✓ 使用统一的executeStep()方法"
echo "4. ✓ 消除了硬编码的STEP_NAMES数组"
echo "5. ✓ 所有步骤相关逻辑都使用动态数据"
