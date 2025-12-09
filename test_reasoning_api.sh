#!/bin/bash

# 流程推理 API 测试脚本

BASE_URL="http://localhost:8080/api/process-reasoning"

echo "========================================="
echo "测试基于 OWL 本体的流程推理 API"
echo "========================================="
echo ""

echo "1. 获取流程总步数"
echo "GET $BASE_URL/flow/total-steps"
curl -s "$BASE_URL/flow/total-steps"
echo -e "\n"

echo "2. 获取流程开始步骤"
echo "GET $BASE_URL/flow/start-step"
curl -s "$BASE_URL/flow/start-step" | python3 -m json.tool
echo ""

echo "3. 获取流程结束步骤"
echo "GET $BASE_URL/flow/end-step"
curl -s "$BASE_URL/flow/end-step" | python3 -m json.tool
echo ""

echo "4. 获取特定步骤详细信息 (LocateSourceCustomerStep)"
echo "GET $BASE_URL/step/LocateSourceCustomerStep"
curl -s "$BASE_URL/step/LocateSourceCustomerStep" | python3 -m json.tool
echo ""

echo "5. 获取步骤的前驱步骤 (SelectTransferNumberStep)"
echo "GET $BASE_URL/step/SelectTransferNumberStep/previous"
curl -s "$BASE_URL/step/SelectTransferNumberStep/previous" | python3 -m json.tool
echo ""

echo "6. 获取步骤的后继步骤 (LocateSourceCustomerStep)"
echo "GET $BASE_URL/step/LocateSourceCustomerStep/next"
curl -s "$BASE_URL/step/LocateSourceCustomerStep/next" | python3 -m json.tool
echo ""

echo "7. 获取完整流程信息（仅显示前50行）"
echo "GET $BASE_URL/flow"
curl -s "$BASE_URL/flow" | python3 -m json.tool | head -50
echo ""

echo "========================================="
echo "测试完成"
echo "========================================="
