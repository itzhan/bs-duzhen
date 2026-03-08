#!/bin/bash

# ============================================================
# 汽车售后维修服务管理平台 - 启动脚本 (Mac/Linux)
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# 创建必要的目录
mkdir -p .logs .pids

# 打印横幅
echo -e "${CYAN}"
echo "============================================================"
echo "    汽车售后维修服务管理平台"
echo "    Car Maintenance Service Management Platform"
echo "============================================================"
echo -e "${NC}"

# 错误处理函数
error_exit() {
    echo -e "${RED}错误: $1${NC}" >&2
    exit 1
}

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        return 1
    fi
    return 0
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # 端口被占用
    fi
    return 1  # 端口空闲
}

# 等待端口就绪
wait_for_port() {
    local port=$1
    local service_name=$2
    local max_wait=${3:-60}
    local elapsed=0
    
    echo -e "${YELLOW}等待 $service_name 启动 (端口 $port)...${NC}"
    while [ $elapsed -lt $max_wait ]; do
        if check_port $port; then
            echo -e "${GREEN}✓ $service_name 已启动 (端口 $port)${NC}"
            return 0
        fi
        sleep 1
        elapsed=$((elapsed + 1))
        echo -n "."
    done
    echo ""
    echo -e "${RED}✗ $service_name 启动超时 (${max_wait}秒)${NC}"
    return 1
}

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}正在停止所有服务...${NC}"
    
    # 读取并杀死所有PID
    if [ -d .pids ]; then
        for pidfile in .pids/*.pid; do
            if [ -f "$pidfile" ]; then
                pid=$(cat "$pidfile" 2>/dev/null)
                if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
                    kill "$pid" 2>/dev/null
                fi
            fi
        done
    fi
    
    # 杀死tail进程
    pkill -f "tail -f .logs" 2>/dev/null
    
    # 清理PID文件
    rm -f .pids/*.pid
    
    echo -e "${GREEN}所有服务已停止${NC}"
    exit 0
}

# 注册清理函数
trap cleanup SIGINT SIGTERM

# ============================================================
# 1. 检查必需的工具
# ============================================================
echo -e "${BLUE}[1/7] 检查必需工具...${NC}"

# 检查 Java 17+
if check_command java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
        error_exit "需要 Java 17 或更高版本，当前版本: $JAVA_VERSION"
    fi
    echo -e "${GREEN}✓ Java $(java -version 2>&1 | head -n 1)${NC}"
else
    error_exit "未找到 Java，请先安装 Java 17+"
fi

# 检查 Maven
if check_command mvn; then
    echo -e "${GREEN}✓ Maven $(mvn -version | head -n 1 | cut -d' ' -f3)${NC}"
else
    error_exit "未找到 Maven，请先安装 Maven"
fi

# 检查 Node.js
if check_command node; then
    NODE_VERSION=$(node -v | sed 's/v//' | cut -d'.' -f1)
    if [ "$NODE_VERSION" -lt 16 ] 2>/dev/null; then
        error_exit "需要 Node.js 16 或更高版本"
    fi
    echo -e "${GREEN}✓ Node.js $(node -v)${NC}"
else
    error_exit "未找到 Node.js，请先安装 Node.js 16+"
fi

# 检查 pnpm
if check_command pnpm; then
    echo -e "${GREEN}✓ pnpm $(pnpm -v)${NC}"
else
    echo -e "${YELLOW}未找到 pnpm，正在通过 npm 安装...${NC}"
    npm install -g pnpm || error_exit "pnpm 安装失败"
    echo -e "${GREEN}✓ pnpm 安装成功${NC}"
fi

# 检查 MySQL 客户端
if check_command mysql; then
    echo -e "${GREEN}✓ MySQL 客户端已安装${NC}"
else
    error_exit "未找到 MySQL 客户端，请先安装 MySQL"
fi

# ============================================================
# 2. 检查 MySQL 服务
# ============================================================
echo -e "\n${BLUE}[2/7] 检查 MySQL 服务...${NC}"

# 检测操作系统类型
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    if brew services list | grep -q "mysql.*started"; then
        echo -e "${GREEN}✓ MySQL 服务正在运行${NC}"
    else
        echo -e "${YELLOW}MySQL 服务未运行，尝试启动...${NC}"
        brew services start mysql || error_exit "MySQL 服务启动失败"
        sleep 3
    fi
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    if systemctl is-active --quiet mysql || systemctl is-active --quiet mysqld; then
        echo -e "${GREEN}✓ MySQL 服务正在运行${NC}"
    else
        echo -e "${YELLOW}MySQL 服务未运行，尝试启动...${NC}"
        sudo systemctl start mysql 2>/dev/null || sudo systemctl start mysqld 2>/dev/null || error_exit "MySQL 服务启动失败"
        sleep 3
    fi
else
    echo -e "${YELLOW}无法自动检测 MySQL 服务状态，请手动确保 MySQL 正在运行${NC}"
fi

# 测试 MySQL 连接
if mysql -h localhost -P 3306 -u root -pab123168 -e "SELECT 1" >/dev/null 2>&1; then
    echo -e "${GREEN}✓ MySQL 连接成功${NC}"
else
    error_exit "MySQL 连接失败，请检查用户名和密码"
fi

# ============================================================
# 3. 检查并创建数据库
# ============================================================
echo -e "\n${BLUE}[3/7] 检查数据库...${NC}"

DB_EXISTS=$(mysql -h localhost -P 3306 -u root -pab123168 -e "SHOW DATABASES LIKE 'car_maintenance';" 2>/dev/null | grep -c car_maintenance)

if [ "$DB_EXISTS" -eq 0 ]; then
    echo -e "${YELLOW}数据库不存在，正在创建并导入数据...${NC}"
    
    if [ ! -f "sql/init.sql" ]; then
        error_exit "未找到 sql/init.sql 文件"
    fi
    
    if [ ! -f "sql/data.sql" ]; then
        error_exit "未找到 sql/data.sql 文件"
    fi
    
    mysql -h localhost -P 3306 -u root -pab123168 < sql/init.sql || error_exit "数据库初始化失败"
    mysql -h localhost -P 3306 -u root -pab123168 car_maintenance < sql/data.sql || error_exit "测试数据导入失败"
    
    echo -e "${GREEN}✓ 数据库创建并导入成功${NC}"
else
    echo -e "${GREEN}✓ 数据库已存在${NC}"
fi

# ============================================================
# 4. 检查并安装依赖
# ============================================================
echo -e "\n${BLUE}[4/7] 检查项目依赖...${NC}"

# 后端依赖
if [ ! -d "backend/target" ] || [ ! -f "backend/target/classes/com/carmaintenance/CarMaintenanceApplication.class" ]; then
    echo -e "${YELLOW}编译后端项目...${NC}"
    cd backend
    mvn clean compile -DskipTests || error_exit "后端编译失败"
    cd ..
    echo -e "${GREEN}✓ 后端编译完成${NC}"
else
    echo -e "${GREEN}✓ 后端已编译${NC}"
fi

# 管理端依赖
if [ ! -d "admin/node_modules" ]; then
    echo -e "${YELLOW}安装管理端依赖...${NC}"
    cd admin
    pnpm install || error_exit "管理端依赖安装失败"
    cd ..
    echo -e "${GREEN}✓ 管理端依赖安装完成${NC}"
else
    echo -e "${GREEN}✓ 管理端依赖已安装${NC}"
fi

# 前端依赖
if [ ! -d "frontend/node_modules" ]; then
    echo -e "${YELLOW}安装前端依赖...${NC}"
    cd frontend
    pnpm install || error_exit "前端依赖安装失败"
    cd ..
    echo -e "${GREEN}✓ 前端依赖安装完成${NC}"
else
    echo -e "${GREEN}✓ 前端依赖已安装${NC}"
fi

# ============================================================
# 5. 检查端口冲突
# ============================================================
echo -e "\n${BLUE}[5/7] 检查端口占用...${NC}"

PORTS=(8080 8848 5173)
PORT_NAMES=("后端" "管理端" "前端")

for i in "${!PORTS[@]}"; do
    port=${PORTS[$i]}
    name=${PORT_NAMES[$i]}
    if check_port $port; then
        echo -e "${YELLOW}警告: 端口 $port ($name) 已被占用${NC}"
        read -p "是否终止占用该端口的进程? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            PID=$(lsof -ti:$port)
            if [ -n "$PID" ]; then
                kill -9 $PID 2>/dev/null
                echo -e "${GREEN}✓ 已终止进程 $PID${NC}"
                sleep 1
            fi
        else
            error_exit "端口 $port 被占用，无法启动服务"
        fi
    else
        echo -e "${GREEN}✓ 端口 $port ($name) 可用${NC}"
    fi
done

# ============================================================
# 6. 启动服务
# ============================================================
echo -e "\n${BLUE}[6/7] 启动服务...${NC}"

# 启动后端
echo -e "${YELLOW}启动后端服务...${NC}"
cd backend
nohup mvn spring-boot:run > ../.logs/backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > ../.pids/backend.pid
cd ..
echo -e "${GREEN}✓ 后端已启动 (PID: $BACKEND_PID)${NC}"

# 启动管理端
echo -e "${YELLOW}启动管理端...${NC}"
cd admin
nohup pnpm dev > ../.logs/admin.log 2>&1 &
ADMIN_PID=$!
echo $ADMIN_PID > ../.pids/admin.pid
cd ..
echo -e "${GREEN}✓ 管理端已启动 (PID: $ADMIN_PID)${NC}"

# 启动前端
echo -e "${YELLOW}启动前端...${NC}"
cd frontend
nohup pnpm dev > ../.logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../.pids/frontend.pid
cd ..
echo -e "${GREEN}✓ 前端已启动 (PID: $FRONTEND_PID)${NC}"

# 启动日志监控
sleep 2
tail -f .logs/backend.log | sed "s/^/[${RED}后端${NC}] /" &
tail -f .logs/admin.log | sed "s/^/[${BLUE}管理端${NC}] /" &
tail -f .logs/frontend.log | sed "s/^/[${GREEN}前端${NC}] /" &

# ============================================================
# 7. 等待服务就绪
# ============================================================
echo -e "\n${BLUE}[7/7] 等待服务就绪...${NC}"

wait_for_port 8080 "后端服务" 60
wait_for_port 8848 "管理端" 30
wait_for_port 5173 "前端" 30

# ============================================================
# 8. 显示访问信息
# ============================================================
echo -e "\n${CYAN}"
echo "============================================================"
echo "    服务启动成功！"
echo "============================================================"
echo -e "${NC}"

echo -e "${GREEN}访问地址:${NC}"
echo -e "  后端 API:    ${CYAN}http://localhost:8080${NC}"
echo -e "  管理端:      ${CYAN}http://localhost:8848${NC}"
echo -e "  前端:        ${CYAN}http://localhost:5173${NC}"

echo -e "\n${GREEN}测试账号:${NC}"
echo -e "  系统管理员:  ${CYAN}admin / 123456${NC}"
echo -e "  服务顾问:    ${CYAN}advisor1 / 123456${NC}"
echo -e "  维修技师:    ${CYAN}tech1 / 123456${NC}"
echo -e "  仓库管理员:  ${CYAN}warehouse1 / 123456${NC}"

echo -e "\n${YELLOW}日志文件位置:${NC}"
echo -e "  后端:   .logs/backend.log"
echo -e "  管理端: .logs/admin.log"
echo -e "  前端:   .logs/frontend.log"

echo -e "\n${YELLOW}提示: 按 Ctrl+C 停止所有服务${NC}"
echo ""

# 保持脚本运行
wait
