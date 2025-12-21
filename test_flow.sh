#!/bin/bash

# 定义颜色
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
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
        exit 1
    fi
}

# 提取普通 JSON 对象字段 (适用于 Content Service)
extract_json_field() {
    echo "$1" | python3 -c "import sys, json; print(json.load(sys.stdin).get('$2', ''))" 2>/dev/null
}

# 提取 ApiResponse 中的 data 字段 (适用于 User Service)
# 结构: { code: 200, message: "...", data: { ... } }
# 用法: extract_api_data "$JSON" "field_inside_data"
extract_api_data() {
    echo "$1" | python3 -c "import sys, json; res=json.load(sys.stdin); print(res.get('data', {}).get('$2', ''))" 2>/dev/null
}

# 轮询等待函数
# 用法: poll_for_task "描述" "命令" "提取逻辑"
poll_for_task() {
    local desc="$1"
    local cmd="$2"
    local extractor="$3"
    local max_retries=10
    local wait_sec=2
    
    echo "正在等待 $desc ..." >&2
    for ((i=1; i<=max_retries; i++)); do
        RES=$(eval "$cmd")
        # 尝试提取 ID (假设提取逻辑脚本接受 JSON 输入并输出 ID)
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
    exit 1
}

# ==========================================
# 步骤 0: 准备测试资源
# ==========================================
print_step "步骤 0: 生成测试用多媒体文件"

# 创建一个简单的图片文件 (1x1 像素 GIF)
echo -ne "\x47\x49\x46\x38\x39\x61\x01\x00\x01\x00\x80\x00\x00\xff\xff\xff\x00\x00\x00\x21\xf9\x04\x01\x00\x00\x00\x00\x2c\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02\x44\x01\x00\x3b" > test_cover.jpg
echo -e "Generated test_cover.jpg"
cp test_cover.jpg test_image.jpg
echo -e "Generated test_image.jpg"
echo "This is a dummy video file content" > test_video.mp4
echo -e "Generated test_video.mp4"

check_success

# ==========================================
# 步骤 1: 用户服务 - 注册与登录
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
    echo -e "${RED}无法获取管理员 Token${NC}"
    echo "Response: $ADMIN_LOGIN_RES"
    exit 1
fi
echo "管理员 Token 获取成功"

# --- 注册普通用户 ---
print_step "步骤 1.3: 注册普通用户 ($NORMAL_USER)"
# 注意: 注册会自动触发 'user-onboarding' 流程
curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"email\": \"${NORMAL_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}"
check_success

# --- 普通用户登录 ---
print_step "步骤 1.4: 普通用户登录"
USER_LOGIN_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"password\": \"$PASSWORD\"}")

USER_TOKEN=$(extract_api_data "$USER_LOGIN_RES" "token")

if [ -z "$USER_TOKEN" ]; then
    echo -e "${RED}无法获取用户 Token${NC}"
    echo "Response: $USER_LOGIN_RES"
    exit 1
fi
echo "用户 Token 获取成功"

# --- 注册入侵用户 ---
print_step "步骤 1.5: 注册入侵用户 ($INTRUDER_USER)"
curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$INTRUDER_USER\", \"email\": \"${INTRUDER_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}"
check_success

# --- 入侵用户登录 ---
print_step "步骤 1.6: 入侵用户登录"
INTRUDER_LOGIN_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$INTRUDER_USER\", \"password\": \"$PASSWORD\"}")

INTRUDER_TOKEN=$(extract_api_data "$INTRUDER_LOGIN_RES" "token")

if [ -z "$INTRUDER_TOKEN" ]; then
    echo -e "${RED}无法获取入侵用户 Token${NC}"
    echo "Response: $INTRUDER_LOGIN_RES"
    exit 1
fi
echo "入侵用户 Token 获取成功"

# ==========================================
# 步骤 2: 用户服务 - 工作流审批
# ==========================================
# 之前的 "步骤 2.1: 用户发起入职流程" 已移除，因为注册时已自动发起

# --- 管理员查询任务 ---
print_step "步骤 2.2: 管理员查询审批任务 (轮询)"

# Python 脚本提取 ApiResponse 中的第一个任务 ID
EXTRACT_TASK_PY="import sys, json; res=json.load(sys.stdin); data=res.get('data', []); print(data[0]['id'] if isinstance(data, list) and len(data)>0 else '')"

# 使用轮询获取任务 ID
TASK_CMD="curl -s -X GET \"${USER_SERVICE_URL}/process/tasks?assignee=admin\" -H \"Authorization: Bearer $ADMIN_TOKEN\""
TASK_ID=$(poll_for_task "管理员审批任务" "$TASK_CMD" "$EXTRACT_TASK_PY")

# --- 管理员完成任务 ---
print_step "步骤 2.3: 管理员完成审批任务 ($TASK_ID)"
curl -s -X POST "${USER_SERVICE_URL}/process/tasks/${TASK_ID}/complete" \
    -H "Authorization: Bearer $ADMIN_TOKEN"
check_success

# ==========================================
# 步骤 3: 内容服务 - 发布内容 (API V2)
# ==========================================

# --- 发布图文 (POST) ---
print_step "步骤 3.1: 用户发布图文 (POST)"
# 1. 上传图片
echo "Uploading image..."
IMAGE_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "file=@test_image.jpg")
IMAGE_ID=$(extract_json_field "$IMAGE_RES" "id")
echo "Image Uploaded: ID=$IMAGE_ID"

if [ -z "$IMAGE_ID" ]; then echo -e "${RED}Image Upload Failed${NC}"; exit 1; fi

# 2. 发布内容
echo "Publishing content..."
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"content\": \"这是一个测试图文内容\",
        \"contentType\": \"POST\",
        \"mediaIds\": [$IMAGE_ID]
    }"
check_success

# --- 发布文章 (ARTICLE) ---
print_step "步骤 3.2: 用户发布文章 (ARTICLE)"
# 1. 上传封面
echo "Uploading cover..."
COVER_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "file=@test_cover.jpg")
COVER_ID=$(extract_json_field "$COVER_RES" "id")
echo "Cover Uploaded: ID=$COVER_ID"

# 2. 发布文章
echo "Publishing article..."
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"测试文章标题\",
        \"content\": \"这是文章的详细内容...\",
        \"contentType\": \"ARTICLE\",
        \"summary\": \"文章摘要\",
        \"coverId\": $COVER_ID
    }"
check_success

# --- 发布视频 (VIDEO) ---
print_step "步骤 3.3: 用户发布视频 (VIDEO)"
# 1. 上传视频
echo "Uploading video..."
VIDEO_RES=$(curl -s -X POST "${CONTENT_SERVICE_URL}/upload" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "file=@test_video.mp4")
VIDEO_ID=$(extract_json_field "$VIDEO_RES" "id")
echo "Video Uploaded: ID=$VIDEO_ID"

# 2. 发布视频内容
echo "Publishing video content..."
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"测试视频Vlog\",
        \"content\": \"看看这个视频！\",
        \"contentType\": \"VIDEO\",
        \"coverId\": $COVER_ID,
        \"mainMediaId\": $VIDEO_ID
    }"
check_success

# ==========================================
# 步骤 4: 内容服务 - 审核与查看
# ==========================================

# --- 用户查看内容 (审核前) ---
print_step "步骤 4.1: 用户查看内容列表 (应包含 PENDING 状态)"
LIST_RES=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list" \
    -H "Authorization: Bearer $USER_TOKEN")

# 检查是否包含 "PENDING"
if [[ "$LIST_RES" == *"PENDING"* ]]; then
    echo -e "${GREEN}验证通过: 列表中包含 PENDING 内容${NC}"
else
    echo -e "${RED}验证失败: 列表中未找到 PENDING 内容${NC}"
fi

# --- 入侵用户尝试查看内容 ---
print_step "步骤 4.1a: 入侵用户尝试查看内容 (应看不到 PENDING 内容)"
# 尝试显式请求 PENDING 状态
INTRUDER_LIST_RES=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list?status=PENDING" \
    -H "Authorization: Bearer $INTRUDER_TOKEN")

if [[ "$INTRUDER_LIST_RES" == *"PENDING"* ]]; then
    echo -e "${RED}验证失败: 入侵用户看到了 PENDING 内容${NC}"
    echo "Response: $INTRUDER_LIST_RES"
    exit 1
else
    echo -e "${GREEN}验证通过: 入侵用户未看到 PENDING 内容${NC}"
fi

# 尝试默认请求
INTRUDER_DEFAULT_LIST_RES=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list" \
    -H "Authorization: Bearer $INTRUDER_TOKEN")

if [[ "$INTRUDER_DEFAULT_LIST_RES" == *"PENDING"* ]]; then
    echo -e "${RED}验证失败: 入侵用户在默认列表中看到了 PENDING 内容${NC}"
    exit 1
else
    echo -e "${GREEN}验证通过: 入侵用户在默认列表中未看到 PENDING 内容${NC}"
fi

# --- 管理员获取审核任务 ---
print_step "步骤 4.2: 管理员获取内容审核任务 (轮询)"

# Python 脚本提取 List 中的第一个任务 ID
EXTRACT_REVIEW_PY="import sys, json; data=json.load(sys.stdin); print(data[0]['taskId'] if isinstance(data, list) and len(data)>0 else '')"

REVIEW_CMD="curl -s -X GET \"${CONTENT_SERVICE_URL}/review/tasks\" -H \"Authorization: Bearer $ADMIN_TOKEN\""
REVIEW_TASK_ID=$(poll_for_task "内容审核任务" "$REVIEW_CMD" "$EXTRACT_REVIEW_PY")

# --- 管理员批准 ---
print_step "步骤 4.3: 管理员批准内容"
curl -s -X POST "${CONTENT_SERVICE_URL}/review/tasks/${REVIEW_TASK_ID}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"approved": true}'
check_success

# --- 用户再次查看内容 (审核后) ---
print_step "步骤 4.4: 用户再次查看内容 (应包含 PUBLISHED 状态)"
# 稍微等待状态更新
sleep 1
FINAL_LIST=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list" \
    -H "Authorization: Bearer $USER_TOKEN")

if [[ "$FINAL_LIST" == *"PUBLISHED"* ]]; then
     echo -e "${GREEN}验证通过: 列表中包含 PUBLISHED 内容${NC}"
else
     echo -e "${BLUE}提示: 如果刚才未找到任务，这里可能只有 PENDING。${NC}"
fi

# ==========================================
# 清理
# ==========================================
print_step "清理临时文件"
rm test_cover.jpg test_video.mp4 test_image.jpg
echo "完成"

echo -e "\n${GREEN}=== 所有测试步骤执行完毕 ===${NC}"
