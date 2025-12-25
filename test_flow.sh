#!/bin/bash

# 定义颜色
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 服务地址
USER_SERVICE_URL="http://localhost:8081/api/user"
CONTENT_SERVICE_URL="http://localhost:8082/api/content"

# 生成随机后缀以避免用户名冲突
RANDOM_SUFFIX=$(date +%s)
ADMIN_USER="admin_${RANDOM_SUFFIX}"
NORMAL_USER="user_${RANDOM_SUFFIX}"
INTRUDER_USER="intruder_${RANDOM_SUFFIX}"
PASSWORD="password123"

# --- 辅助函数 ---

print_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

check_success() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}成功${NC}"
    else
        echo -e "${RED}失败${NC}"
        # 发生错误时尝试清理
        cleanup
        exit 1
    fi
}

# 预期失败的检查
check_fail() {
    if [ $? -ne 0 ]; then
        echo -e "${GREEN}预期内的失败 (成功拦截)${NC}"
    else
        echo -e "${RED}失败: 此操作应该被禁止但成功执行了${NC}"
        cleanup
        exit 1
    fi
}

# 提取普通 JSON 对象字段
extract_json_field() {
    echo "$1" | python3 -c "import sys, json; print(json.load(sys.stdin).get('$2', ''))" 2>/dev/null
}

# 提取 ApiResponse 中的 data 字段 (支持嵌套)
extract_api_data() {
    echo "$1" | python3 -c "import sys, json; res=json.load(sys.stdin); print(res.get('data', {}).get('$2', ''))" 2>/dev/null
}

# 提取 User 对象的 status 字段
extract_user_status() {
    echo "$1" | python3 -c "import sys, json; res=json.load(sys.stdin); print(res.get('data', {}).get('status', ''))" 2>/dev/null
}

# 轮询等待函数
poll_for_task() {
    local desc="$1"
    local cmd="$2"
    local extractor="$3"
    local max_retries=10
    local wait_sec=2
    
    echo "正在等待 $desc ..." >&2
    for ((i=1; i<=max_retries; i++)); do
        RES=$(eval "$cmd")
        ID=$(echo "$RES" | python3 -c "$extractor" 2>/dev/null)
        
        if [ -n "$ID" ]; then
            echo -e "${GREEN}找到任务: $ID (尝试次数: $i)${NC}" >&2
            echo "$ID"
            return 0
        fi
        echo "  ... 未找到，等待 ${wait_sec}s (尝试 $i/$max_retries)" >&2
        sleep $wait_sec
    done
    
    echo -e "${RED}超时: 未能找到 $desc${NC}" >&2
    echo "Last Response: $RES" >&2
    cleanup
    exit 1
}

cleanup() {
    print_step "清理临时文件"
    rm -f test_cover.jpg test_video.mp4 test_image.jpg
}

# ==========================================
# 步骤 0: 准备测试资源
# ==========================================
print_step "步骤 0: 生成测试用多媒体文件"

echo -ne "\x47\x49\x46\x38\x39\x61\x01\x00\x01\x00\x80\x00\x00\xff\xff\xff\x00\x00\x00\x21\xf9\x04\x01\x00\x00\x00\x00\x2c\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02\x44\x01\x00\x3b" > test_cover.jpg
cp test_cover.jpg test_image.jpg
echo "This is a dummy video file content" > test_video.mp4
echo -e "已生成: test_cover.jpg, test_image.jpg, test_video.mp4"

check_success

# ==========================================
# 步骤 1: 用户服务 - 注册与状态验证
# ==========================================

# --- 注册管理员 ---
print_step "步骤 1.1: 注册管理员用户 ($ADMIN_USER)"
curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$ADMIN_USER\", \"email\": \"${ADMIN_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"admin\"]}"
check_success

# --- 管理员登录 ---
print_step "步骤 1.2: 管理员登录"
ADMIN_LOGIN_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$ADMIN_USER\", \"password\": \"$PASSWORD\"}")
ADMIN_TOKEN=$(extract_api_data "$ADMIN_LOGIN_RES" "token")

if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}无法获取管理员 Token${NC}"; echo "Response: $ADMIN_LOGIN_RES"; exit 1
fi

# --- 注册普通用户 ---
print_step "步骤 1.3: 注册普通用户 ($NORMAL_USER)"
USER_REGISTER_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"email\": \"${NORMAL_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}")
echo "$USER_REGISTER_RES"
USER_ID=$(extract_api_data "$USER_REGISTER_RES" "id")
if [ -z "$USER_ID" ]; then
    echo -e "${RED}无法获取普通用户 ID${NC}"; exit 1
fi
check_success

# --- 普通用户登录 ---
print_step "步骤 1.4: 普通用户登录"
USER_LOGIN_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"password\": \"$PASSWORD\"}")
USER_TOKEN=$(extract_api_data "$USER_LOGIN_RES" "token")

if [ -z "$USER_TOKEN" ]; then
    echo -e "${RED}无法获取用户 Token${NC}"; exit 1
fi

# --- 验证初始状态 (PENDING) ---
print_step "步骤 1.5: 验证用户初始状态 (应为 PENDING)"
USER_INFO_RES=$(curl -s -X GET "${USER_SERVICE_URL}/me" -H "Authorization: Bearer $USER_TOKEN")
USER_STATUS=$(extract_user_status "$USER_INFO_RES")

if [ "$USER_STATUS" == "PENDING" ]; then
    echo -e "${GREEN}验证通过: 用户状态为 PENDING${NC}"
else
    echo -e "${RED}验证失败: 用户状态为 $USER_STATUS (预期: PENDING)${NC}"; exit 1
fi

# ==========================================
# 步骤 2: 用户服务 - 工作流审批与权限检查
# ==========================================

# --- 管理员查询任务 ---
print_step "步骤 2.1: 管理员查询用户入职任务 (轮询)"
EXTRACT_TASK_PY="import sys, json; res=json.load(sys.stdin); data=res.get('data', []); print(data[0]['id'] if isinstance(data, list) and len(data)>0 else '')"
# 使用 USER_ID (businessKey) 过滤任务
TASK_CMD="curl -s -X GET \"${USER_SERVICE_URL}/process/tasks?assignee=admin&businessKey=${USER_ID}\" -H \"Authorization: Bearer $ADMIN_TOKEN\""
TASK_ID=$(poll_for_task "用户入职任务" "$TASK_CMD" "$EXTRACT_TASK_PY")

# --- 越权检查 ---
print_step "步骤 2.2: 越权检查 (普通用户尝试审批自己)"
curl -s -f -X POST "${USER_SERVICE_URL}/process/tasks/${TASK_ID}/complete" \
    -H "Authorization: Bearer $USER_TOKEN"
check_fail

# --- 管理员审批 ---
print_step "步骤 2.3: 管理员批准用户 ($TASK_ID)"
curl -s -X POST "${USER_SERVICE_URL}/process/tasks/${TASK_ID}/complete" \
    -H "Authorization: Bearer $ADMIN_TOKEN"
check_success

# --- 验证审批后状态 (ACTIVE) ---
print_step "步骤 2.4: 验证审批后用户状态 (应为 ACTIVE)"
sleep 1 # 等待异步更新
USER_INFO_RES_AFTER=$(curl -s -X GET "${USER_SERVICE_URL}/me" -H "Authorization: Bearer $USER_TOKEN")
USER_STATUS_AFTER=$(extract_user_status "$USER_INFO_RES_AFTER")

if [ "$USER_STATUS_AFTER" == "ACTIVE" ]; then
    echo -e "${GREEN}验证通过: 用户状态已更新为 ACTIVE${NC}"
else
    echo -e "${RED}验证失败: 用户状态为 $USER_STATUS_AFTER (预期: ACTIVE)${NC}"; exit 1
fi

# ==========================================
# 步骤 3: 内容服务 - 发布多类型内容 (批准、拒绝、视频)
# ==========================================

# --- 发布图文 (批准) ---
print_step "步骤 3.1: 用户发布图文 (POST) - 预期批准"
IMAGE_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" -H "Authorization: Bearer $USER_TOKEN" -F "file=@test_image.jpg")
IMAGE_ID=$(extract_json_field "$IMAGE_RES" "id")
POST_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{ \"content\": \"Approved Image Post\", \"contentType\": \"POST\", \"mediaIds\": [$IMAGE_ID] }")
POST_ID=$(extract_json_field "$POST_RES" "id")
check_success

# --- 发布文章 (拒绝) ---
print_step "步骤 3.2: 用户发布文章 (ARTICLE) - 预期拒绝"
COVER_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" -H "Authorization: Bearer $USER_TOKEN" -F "file=@test_cover.jpg")
COVER_ID=$(extract_json_field "$COVER_RES" "id")
ARTICLE_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{ \"title\": \"Suspicious Article\", \"content\": \"Bad words here\", \"contentType\": \"ARTICLE\", \"summary\": \"Sum\", \"coverId\": $COVER_ID }")
ARTICLE_ID=$(extract_json_field "$ARTICLE_RES" "id")
check_success

# --- 发布视频 ---
print_step "步骤 3.3: 用户发布视频 (VIDEO)"
VIDEO_MEDIA_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" -H "Authorization: Bearer $USER_TOKEN" -F "file=@test_video.mp4")
VIDEO_MEDIA_ID=$(extract_json_field "$VIDEO_MEDIA_RES" "id")
VIDEO_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{ \"title\": \"My Vlog\", \"content\": \"Fun times\", \"contentType\": \"VIDEO\", \"coverId\": $COVER_ID, \"mainMediaId\": $VIDEO_MEDIA_ID }")
VIDEO_ID=$(extract_json_field "$VIDEO_RES" "id")
check_success

# ==========================================
# 步骤 4: 内容审核流程
# ==========================================

print_step "步骤 4.1: 获取审核任务列表"
REVIEW_RES=$(curl -s -X GET "${CONTENT_SERVICE_URL}/review/tasks" -H "Authorization: Bearer $ADMIN_TOKEN")

# 定义辅助 Python 脚本来根据 contentId 提取 taskId
find_task_for_content() {
    echo "$1" | python3 -c "import sys, json; data=json.load(sys.stdin); print(next((t['taskId'] for t in data if str(t.get('contentId')) == '$2'), ''))"
}

# --- 处理任务 1: 批准 POST ---
T1=$(find_task_for_content "$REVIEW_RES" "$POST_ID")
if [ -n "$T1" ]; then
    print_step "步骤 4.2: 批准任务 $T1 (POST ID: $POST_ID)"
    curl -s -X POST "${CONTENT_SERVICE_URL}/review/tasks/${T1}" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"approved": true}'
    check_success
else
    echo -e "${RED}未找到 POST 内容的审核任务 (ID: $POST_ID)${NC}"
fi

# --- 处理任务 2: 拒绝 ARTICLE ---
T2=$(find_task_for_content "$REVIEW_RES" "$ARTICLE_ID")
if [ -n "$T2" ]; then
    print_step "步骤 4.3: 拒绝任务 $T2 (ARTICLE ID: $ARTICLE_ID)"
    curl -s -X POST "${CONTENT_SERVICE_URL}/review/tasks/${T2}" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"approved": false}'
    check_success
else
    echo -e "${RED}未找到 ARTICLE 内容的审核任务 (ID: $ARTICLE_ID)${NC}"
fi

# --- 处理任务 3: 批准 VIDEO ---
T3=$(find_task_for_content "$REVIEW_RES" "$VIDEO_ID")
if [ -n "$T3" ]; then
    print_step "步骤 4.4: 批准任务 $T3 (VIDEO ID: $VIDEO_ID)"
    curl -s -X POST "${CONTENT_SERVICE_URL}/review/tasks/${T3}" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"approved": true}'
    check_success
else
    echo -e "${RED}未找到 VIDEO 内容的审核任务 (ID: $VIDEO_ID)${NC}"
fi

# ==========================================
# 步骤 5: 最终状态与权限验证
# ==========================================
print_step "步骤 5: 验证最终内容可见性与一致性"

echo "等待异步更新..."
sleep 2

# 用户查看自己的列表
FINAL_USER_LIST=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list?size=100" -H "Authorization: Bearer $USER_TOKEN")
echo "用户内容列表: $FINAL_USER_LIST"

echo "验证 PUBLISHED 状态 (POST)..."
echo "$FINAL_USER_LIST" | python3 -c "import sys, json; data=json.load(sys.stdin).get('content', []); exit(0 if any(c.get('id') == $POST_ID and c.get('status') == 'PUBLISHED' for c in data) else 1)"
check_success

echo "验证 REJECTED 状态 (ARTICLE)..."
echo "$FINAL_USER_LIST" | python3 -c "import sys, json; data=json.load(sys.stdin).get('content', []); exit(0 if any(c.get('id') == $ARTICLE_ID and c.get('status') == 'REJECTED' for c in data) else 1)"
check_success

echo "验证 PUBLISHED 状态 (VIDEO)..."
echo "$FINAL_USER_LIST" | python3 -c "import sys, json; data=json.load(sys.stdin).get('content', []); exit(0 if any(c.get('id') == $VIDEO_ID and c.get('status') == 'PUBLISHED' for c in data) else 1)"
check_success

# 入侵者检查
print_step "步骤 5.1: 权限隔离检查"
curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$INTRUDER_USER\", \"email\": \"${INTRUDER_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}" > /dev/null
INTRUDER_LOGIN=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$INTRUDER_USER\", \"password\": \"$PASSWORD\"}")
INTRUDER_TOKEN=$(extract_api_data "$INTRUDER_LOGIN" "token")

INTRUDER_VIEW=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list" -H "Authorization: Bearer $INTRUDER_TOKEN")

echo "检查入侵者是否能看到被拒绝的内容..."
if [[ "$INTRUDER_VIEW" == *"Suspicious Article"* ]]; then
    echo -e "${RED}失败: 入侵者看到了不该看到的内容${NC}"; exit 1
else
    echo -e "${GREEN}成功: 权限隔离有效${NC}"
fi

# ==========================================
# 总结
# ==========================================
cleanup
echo -e "\n${GREEN}=== 所有测试用例执行成功 ===${NC}"
echo -e "${BLUE}1. BPMN Onboarding: OK${NC}"
echo -e "${BLUE}2. Content Lifecycle (Approve/Reject): OK${NC}"
echo -e "${BLUE}3. Multi-Media (Image/Video): OK${NC}"
echo -e "${BLUE}4. Role Based Access Control: OK${NC}"
echo -e "${BLUE}5. Service Discovery (Nacos Integration): OK (Implied by cross-service calls)${NC}"
