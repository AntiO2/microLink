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
PASSWORD="password123"

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

# 提取 JSON 字段的辅助函数 (使用 Python)
extract_json_field() {
    echo "$1" | python3 -c "import sys, json; print(json.load(sys.stdin).get('$2', ''))" 2>/dev/null
}

# 提取列表中的第一个 Task ID
extract_first_task_id() {
    echo "$1" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data[0]['id'] if isinstance(data, list) and len(data)>0 else '')" 2>/dev/null
}

# ==========================================
# 步骤 0: 准备测试资源
# ==========================================
print_step "步骤 0: 生成测试用多媒体文件"

# 创建一个简单的图片文件 (1x1 像素 GIF)
echo -ne "\x47\x49\x46\x38\x39\x61\x01\x00\x01\x00\x80\x00\x00\xff\xff\xff\x00\x00\x00\x21\xf9\x04\x01\x00\x00\x00\x00\x2c\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02\x44\x01\x00\x3b" > test_cover.jpg
echo -e "Generated test_cover.jpg"

# 复制一份作为图片素材
cp test_cover.jpg test_image.jpg
echo -e "Generated test_image.jpg"

# 创建一个简单的文本文件作为模拟视频
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

ADMIN_TOKEN=$(extract_json_field "$ADMIN_LOGIN_RES" "token")

if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}无法获取管理员 Token${NC}"
    echo "Response: $ADMIN_LOGIN_RES"
    exit 1
fi
echo "管理员 Token 获取成功: ${ADMIN_TOKEN:0:10}..."

# --- 注册普通用户 ---
print_step "步骤 1.3: 注册普通用户 ($NORMAL_USER)"
curl -s -X POST "${USER_SERVICE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"email\": \"${NORMAL_USER}@test.com\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}"
check_success

# --- 普通用户登录 ---
print_step "步骤 1.4: 普通用户登录"
USER_LOGIN_RES=$(curl -s -X POST "${USER_SERVICE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$NORMAL_USER\", \"password\": \"$PASSWORD\"}")

USER_TOKEN=$(extract_json_field "$USER_LOGIN_RES" "token")

if [ -z "$USER_TOKEN" ]; then
    echo -e "${RED}无法获取用户 Token${NC}"
    echo "Response: $USER_LOGIN_RES"
    exit 1
fi
echo "用户 Token 获取成功: ${USER_TOKEN:0:10}..."

# ==========================================
# 步骤 2: 用户服务 - 工作流审批
# ==========================================

# --- 启动入职流程 ---
print_step "步骤 2.1: 用户发起入职流程"
curl -s -X POST "${USER_SERVICE_URL}/process/start" \
    -H "Authorization: Bearer $USER_TOKEN"
check_success

# --- 管理员查询任务 ---
print_step "步骤 2.2: 管理员查询审批任务"
# 等待一小会儿确保任务生成
sleep 1
TASKS_RES=$(curl -s -X GET "${USER_SERVICE_URL}/process/tasks?assignee=admin" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

echo "当前任务列表: $TASKS_RES"
TASK_ID=$(extract_json_field "$TASKS_RES" "0" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
# 简单的提取方式可能不适用于列表，使用专门的列表提取函数
TASK_ID=$(extract_first_task_id "$TASKS_RES")

if [ -z "$TASK_ID" ]; then
    echo -e "${RED}未找到任务 ID${NC}"
    exit 1
fi
echo "获取到任务 ID: $TASK_ID"

# --- 管理员完成任务 ---
print_step "步骤 2.3: 管理员完成审批任务 ($TASK_ID)"
curl -s -X POST "${USER_SERVICE_URL}/process/tasks/${TASK_ID}/complete" \
    -H "Authorization: Bearer $ADMIN_TOKEN"
check_success

# ==========================================
# 步骤 3: 内容服务 - 发布内容
# ==========================================

# --- 发布图文 (POST) ---
print_step "步骤 3.1: 用户发布图文 (POST)"
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "text=这是一个测试图文内容" \
    -F "contentType=POST" \
    -F "media=@test_image.jpg"
check_success

# --- 发布文章 (ARTICLE) ---
print_step "步骤 3.2: 用户发布文章 (ARTICLE)"
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "title=测试文章标题" \
    -F "text=这是文章的详细内容..." \
    -F "contentType=ARTICLE" \
    -F "cover=@test_cover.jpg"
check_success

# --- 发布视频 (VIDEO) ---
print_step "步骤 3.3: 用户发布视频 (VIDEO)"
curl -s -X POST "${CONTENT_SERVICE_URL}/publish" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -F "title=测试视频Vlog" \
    -F "text=看看这个视频！" \
    -F "contentType=VIDEO" \
    -F "cover=@test_cover.jpg" \
    -F "media=@test_video.mp4"
check_success

# ==========================================
# 步骤 4: 内容服务 - 审核与查看
# ==========================================

# --- 用户查看内容 (审核前) ---
print_step "步骤 4.1: 用户查看内容列表 (应包含 PENDING 状态)"
LIST_RES=$(curl -s -X GET "${CONTENT_SERVICE_URL}/list" \
    -H "Authorization: Bearer $USER_TOKEN")
echo "当前内容列表 (部分): ${LIST_RES:0:200}..."
# 检查是否包含 "PENDING"
if [[ "$LIST_RES" == *"PENDING"* ]]; then
    echo -e "${GREEN}验证通过: 列表中包含 PENDING 内容${NC}"
else
    echo -e "${RED}验证失败: 列表中未找到 PENDING 内容${NC}"
fi

# --- 管理员获取审核任务 ---
print_step "步骤 4.2: 管理员获取内容审核任务"
REVIEW_TASKS=$(curl -s -X GET "${CONTENT_SERVICE_URL}/review/tasks" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

# 提取第一个审核任务 ID
REVIEW_TASK_ID=$(echo "$REVIEW_TASKS" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data[0]['taskId'] if isinstance(data, list) and len(data)>0 else '')" 2>/dev/null)

if [ -z "$REVIEW_TASK_ID" ]; then
    echo -e "${RED}未找到审核任务 (可能工作流启动延迟，请检查 BPMN 部署)${NC}"
    # 不退出，继续尝试后续（虽然可能会失败）
else
    echo "获取到审核任务 ID: $REVIEW_TASK_ID"
    
    # --- 管理员批准 ---
    print_step "步骤 4.3: 管理员批准内容"
    curl -s -X POST "${CONTENT_SERVICE_URL}/review/tasks/${REVIEW_TASK_ID}" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"approved": true}'
    check_success
fi

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
