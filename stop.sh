#!/bin/bash

# ============================================================
# 汽车售后维修服务管理平台 - 停止脚本 (Mac/Linux)
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# 打印横幅
echo -e "${CYAN}"
echo "============================================================"
echo "    停止所有服务"
echo "============================================================"
echo -e "${NC}"

# 停止函数
stop_service() {
    local service_name=$1
    local port=$2
    local pidfile=".pids/${service_name}.pid"
    
    # 方法1: 通过PID文件停止
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile" 2>/dev/null)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo -e "${YELLOW}停止 $service_name (PID: $pid)...${NC}"
            kill "$pid" 2>/dev/null
            sleep 1
            # 如果还在运行，强制杀死
            if kill -0 "$pid" 2>/dev/null; then
                kill -9 "$pid" 2>/dev/null
            fi
            echo -e "${GREEN}✓ $service_name 已停止${NC}"
        fi
        rm -f "$pidfile"
    fi
    
    # 方法2: 通过端口停止 (备用方案)
    local port_pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$port_pid" ]; then
        echo -e "${YELLOW}发现端口 $port 上的进程 (PID: $port_pid)，正在停止...${NC}"
        kill "$port_pid" 2>/dev/null
        sleep 1
        if kill -0 "$port_pid" 2>/dev/null; then
            kill -9 "$port_pid" 2>/dev/null
        fi
        echo -e "${GREEN}✓ 端口 $port 上的进程已停止${NC}"
    fi
}

# 停止所有服务
echo -e "${YELLOW}正在停止所有服务...${NC}"
echo ""

# 停止后端
stop_service "backend" 8080

# 停止管理端
stop_service "admin" 8848

# 停止前端
stop_service "frontend" 5173

# 停止日志监控进程
echo -e "${YELLOW}停止日志监控进程...${NC}"
pkill -f "tail -f .logs" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 日志监控进程已停止${NC}"
fi

# 清理所有PID文件
if [ -d ".pids" ]; then
    rm -f .pids/*.pid 2>/dev/null
    echo -e "${GREEN}✓ PID 文件已清理${NC}"
fi

# 额外检查：确保所有端口都已释放
echo ""
echo -e "${YELLOW}检查端口状态...${NC}"

PORTS=(8080 8848 5173)
PORT_NAMES=("后端" "管理端" "前端")
ALL_STOPPED=true

for i in "${!PORTS[@]}"; do
    port=${PORTS[$i]}
    name=${PORT_NAMES[$i]}
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}警告: 端口 $port ($name) 仍被占用${NC}"
        ALL_STOPPED=false
        # 尝试强制杀死
        PID=$(lsof -ti:$port 2>/dev/null)
        if [ -n "$PID" ]; then
            kill -9 $PID 2>/dev/null
            echo -e "${YELLOW}已强制终止进程 $PID${NC}"
        fi
    else
        echo -e "${GREEN}✓ 端口 $port ($name) 已释放${NC}"
    fi
done

echo ""
if [ "$ALL_STOPPED" = true ]; then
    echo -e "${GREEN}============================================================"
    echo "    所有服务已停止"
    echo "============================================================${NC}"
else
    echo -e "${YELLOW}============================================================"
    echo "    服务已停止 (部分端口可能需要稍等片刻)"
    echo "============================================================${NC}"
fi

echo ""
