@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM 汽车售后维修服务管理平台 - Docker 一键启动 (Windows)
REM ============================================================

REM 项目根目录
set "PROJECT_ROOT=%~dp0"
cd /d "%PROJECT_ROOT%"

echo.
echo ============================================================
echo     汽车售后维修服务管理平台
echo     Car Maintenance Service Management Platform
echo ============================================================
echo.

REM ============================================================
REM 1. 检查 Docker 是否安装
REM ============================================================
echo [1/3] 检查 Docker 环境...
echo.

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Docker，请先安装 Docker Desktop
    echo   下载地址: https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
)

for /f "delims=" %%i in ('docker --version') do echo [√] %%i
echo.

REM 检查 Docker 是否正在运行
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] Docker 未运行，正在尝试启动 Docker Desktop...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo [信息] 等待 Docker 启动...
    set DOCKER_READY=0
    for /l %%i in (1,1,60) do (
        docker info >nul 2>&1
        if !errorlevel! equ 0 (
            set DOCKER_READY=1
            goto :docker_ok
        )
        timeout /t 2 /nobreak >nul
        echo|set /p="."
    )
    :docker_ok
    echo.
    if !DOCKER_READY! equ 0 (
        echo [错误] Docker 启动超时，请手动启动 Docker Desktop 后重试
        pause
        exit /b 1
    )
    echo [√] Docker 已启动
    echo.
)

REM 检查 docker compose
docker compose version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker Compose 不可用，请确认 Docker Desktop 已正确安装
    pause
    exit /b 1
)
for /f "delims=" %%i in ('docker compose version') do echo [√] %%i
echo.

REM ============================================================
REM 2. 启动所有服务
REM ============================================================
echo [2/3] 启动所有服务...
echo.

docker compose up -d
if %errorlevel% neq 0 (
    echo.
    echo [错误] 服务启动失败，请检查上方错误信息
    pause
    exit /b 1
)
echo.

REM ============================================================
REM 3. 等待服务就绪
REM ============================================================
echo [3/3] 等待服务就绪...
echo.

REM 等待后端就绪 (最多90秒)
echo [信息] 等待后端服务启动...
set BACKEND_READY=0
for /l %%i in (1,1,90) do (
    curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/auth/login 2>nul | findstr "405 401 200 500" >nul
    if !errorlevel! equ 0 (
        set BACKEND_READY=1
        goto :backend_ready
    )
    timeout /t 1 /nobreak >nul
    echo|set /p="."
)
:backend_ready
echo.
if %BACKEND_READY% equ 1 (
    echo [√] 后端服务已就绪
) else (
    echo [警告] 后端服务启动较慢，可稍后访问
)
echo.

REM ============================================================
REM 显示访问信息
REM ============================================================
echo.
echo ============================================================
echo     所有服务已启动！
echo ============================================================
echo.
echo   访问地址:
echo   ┌──────────┬─────────────────────────────┐
echo   │ 用户前端 │ http://localhost:5173        │
echo   ├──────────┼─────────────────────────────┤
echo   │ 管理后台 │ http://localhost:8848        │
echo   ├──────────┼─────────────────────────────┤
echo   │ 后端 API │ http://localhost:8080        │
echo   └──────────┴─────────────────────────────┘
echo.
echo   测试账号 (密码均为 123456):
echo   ┌──────────────┬──────────────────────────┐
echo   │ 系统管理员   │ admin                    │
echo   ├──────────────┼──────────────────────────┤
echo   │ 顾客         │ customer1 ~ customer8    │
echo   ├──────────────┼──────────────────────────┤
echo   │ 维修技师     │ tech1 ~ tech3            │
echo   └──────────────┴──────────────────────────┘
echo.
echo   常用命令:
echo     查看日志:  docker compose logs -f
echo     停止服务:  双击 stop.bat
echo     重新构建:  docker compose up -d --build
echo.
echo ============================================================
echo.
pause
