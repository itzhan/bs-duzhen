@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM 汽车售后维修服务管理平台 - 启动脚本 (Windows)
REM ============================================================

REM 项目根目录
set "PROJECT_ROOT=%~dp0"
cd /d "%PROJECT_ROOT%"

REM 创建必要的目录
if not exist .logs mkdir .logs
if not exist .pids mkdir .pids

REM 打印横幅
echo.
echo ============================================================
echo     汽车售后维修服务管理平台
echo     Car Maintenance Service Management Platform
echo ============================================================
echo.

REM ============================================================
REM 1. 检查必需的工具
REM ============================================================
echo [1/7] 检查必需工具...
echo.

REM 检查 Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请先安装 Java 17+
    pause
    exit /b 1
)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%i
echo [√] Java %JAVA_VERSION%
echo.

REM 检查 Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Maven，请先安装 Maven
    pause
    exit /b 1
)
for /f "tokens=3" %%i in ('mvn -version ^| findstr /i "Apache Maven"') do set MAVEN_VERSION=%%i
echo [√] Maven %MAVEN_VERSION%
echo.

REM 检查 Node.js
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Node.js，请先安装 Node.js 16+
    pause
    exit /b 1
)
for /f "delims=" %%i in ('node -v') do set NODE_VERSION=%%i
echo [√] Node.js %NODE_VERSION%
echo.

REM 检查 pnpm
where pnpm >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未找到 pnpm，正在通过 npm 安装...
    call npm install -g pnpm
    if %errorlevel% neq 0 (
        echo [错误] pnpm 安装失败
        pause
        exit /b 1
    )
    echo [√] pnpm 安装成功
) else (
    for /f "delims=" %%i in ('pnpm -v') do set PNPM_VERSION=%%i
    echo [√] pnpm %PNPM_VERSION%
)
echo.

REM 检查 MySQL 客户端
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 MySQL 客户端，请先安装 MySQL
    pause
    exit /b 1
)
echo [√] MySQL 客户端已安装
echo.

REM ============================================================
REM 2. 检查 MySQL 服务
REM ============================================================
echo [2/7] 检查 MySQL 服务...
echo.

sc query MySQL >nul 2>&1
if %errorlevel% neq 0 (
    sc query MySQL80 >nul 2>&1
    if %errorlevel% neq 0 (
        echo [警告] 无法检测 MySQL 服务状态，请手动确保 MySQL 正在运行
    ) else (
        set MYSQL_SERVICE=MySQL80
    )
) else (
    set MYSQL_SERVICE=MySQL
)

if defined MYSQL_SERVICE (
    sc query !MYSQL_SERVICE! | findstr "RUNNING" >nul
    if %errorlevel% neq 0 (
        echo [警告] MySQL 服务未运行，尝试启动...
        net start !MYSQL_SERVICE!
        timeout /t 3 /nobreak >nul
    ) else (
        echo [√] MySQL 服务正在运行
    )
)
echo.

REM 测试 MySQL 连接
mysql -h localhost -P 3306 -u root -pab123168 -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] MySQL 连接失败，请检查用户名和密码
    pause
    exit /b 1
)
echo [√] MySQL 连接成功
echo.

REM ============================================================
REM 3. 检查并创建数据库
REM ============================================================
echo [3/7] 检查数据库...
echo.

mysql -h localhost -P 3306 -u root -pab123168 -e "SHOW DATABASES LIKE 'car_maintenance';" 2>nul | findstr "car_maintenance" >nul
if %errorlevel% neq 0 (
    echo [警告] 数据库不存在，正在创建并导入数据...
    
    if not exist "sql\init.sql" (
        echo [错误] 未找到 sql\init.sql 文件
        pause
        exit /b 1
    )
    
    if not exist "sql\data.sql" (
        echo [错误] 未找到 sql\data.sql 文件
        pause
        exit /b 1
    )
    
    mysql -h localhost -P 3306 -u root -pab123168 < sql\init.sql
    if %errorlevel% neq 0 (
        echo [错误] 数据库初始化失败
        pause
        exit /b 1
    )
    
    mysql -h localhost -P 3306 -u root -pab123168 car_maintenance < sql\data.sql
    if %errorlevel% neq 0 (
        echo [错误] 测试数据导入失败
        pause
        exit /b 1
    )
    
    echo [√] 数据库创建并导入成功
) else (
    echo [√] 数据库已存在
)
echo.

REM ============================================================
REM 4. 检查并安装依赖
REM ============================================================
echo [4/7] 检查项目依赖...
echo.

REM 后端依赖
if not exist "backend\target\classes\com\carmaintenance\CarMaintenanceApplication.class" (
    echo [警告] 编译后端项目...
    cd backend
    call mvn clean compile -DskipTests
    if %errorlevel% neq 0 (
        echo [错误] 后端编译失败
        pause
        exit /b 1
    )
    cd ..
    echo [√] 后端编译完成
) else (
    echo [√] 后端已编译
)
echo.

REM 管理端依赖
if not exist "admin\node_modules" (
    echo [警告] 安装管理端依赖...
    cd admin
    call pnpm install
    if %errorlevel% neq 0 (
        echo [错误] 管理端依赖安装失败
        pause
        exit /b 1
    )
    cd ..
    echo [√] 管理端依赖安装完成
) else (
    echo [√] 管理端依赖已安装
)
echo.

REM 前端依赖
if not exist "frontend\node_modules" (
    echo [警告] 安装前端依赖...
    cd frontend
    call pnpm install
    if %errorlevel% neq 0 (
        echo [错误] 前端依赖安装失败
        pause
        exit /b 1
    )
    cd ..
    echo [√] 前端依赖安装完成
) else (
    echo [√] 前端依赖已安装
)
echo.

REM ============================================================
REM 5. 检查端口冲突
REM ============================================================
echo [5/7] 检查端口占用...
echo.

set PORTS=8080 8848 5173
set PORT_NAMES=后端 管理端 前端
set INDEX=0

for %%p in (%PORTS%) do (
    netstat -ano | findstr ":%%p " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [警告] 端口 %%p 已被占用
        set /p KILL_PORT="是否终止占用该端口的进程? (y/n): "
        if /i "!KILL_PORT!"=="y" (
            for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr "LISTENING"') do (
                taskkill /F /PID %%a >nul 2>&1
                echo [√] 已终止进程 %%a
            )
            timeout /t 1 /nobreak >nul
        ) else (
            echo [错误] 端口 %%p 被占用，无法启动服务
            pause
            exit /b 1
        )
    ) else (
        echo [√] 端口 %%p 可用
    )
)
echo.

REM ============================================================
REM 6. 启动服务
REM ============================================================
echo [6/7] 启动服务...
echo.

REM 启动后端 (红色窗口)
echo [警告] 启动后端服务...
start "后端服务 (端口 8080)" /min cmd /k "color 0C && cd /d %PROJECT_ROOT%\backend && mvn spring-boot:run"
timeout /t 2 /nobreak >nul
echo [√] 后端已启动
echo.

REM 启动管理端 (蓝色窗口)
echo [警告] 启动管理端...
start "管理端 (端口 8848)" /min cmd /k "color 0B && cd /d %PROJECT_ROOT%\admin && pnpm dev"
timeout /t 2 /nobreak >nul
echo [√] 管理端已启动
echo.

REM 启动前端 (绿色窗口)
echo [警告] 启动前端...
start "前端 (端口 5173)" /min cmd /k "color 0A && cd /d %PROJECT_ROOT%\frontend && pnpm dev"
timeout /t 2 /nobreak >nul
echo [√] 前端已启动
echo.

REM ============================================================
REM 7. 等待服务就绪
REM ============================================================
echo [7/7] 等待服务就绪...
echo.

REM 等待后端 (最多60秒)
set BACKEND_READY=0
for /l %%i in (1,1,60) do (
    netstat -ano | findstr ":8080 " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [√] 后端服务已启动 (端口 8080)
        set BACKEND_READY=1
        goto :backend_done
    )
    timeout /t 1 /nobreak >nul
    echo|set /p="."
)
:backend_done
if %BACKEND_READY% equ 0 (
    echo [警告] 后端服务启动超时 (60秒)
)
echo.

REM 等待管理端 (最多30秒)
set ADMIN_READY=0
for /l %%i in (1,1,30) do (
    netstat -ano | findstr ":8848 " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [√] 管理端已启动 (端口 8848)
        set ADMIN_READY=1
        goto :admin_done
    )
    timeout /t 1 /nobreak >nul
    echo|set /p="."
)
:admin_done
if %ADMIN_READY% equ 0 (
    echo [警告] 管理端启动超时 (30秒)
)
echo.

REM 等待前端 (最多30秒)
set FRONTEND_READY=0
for /l %%i in (1,1,30) do (
    netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [√] 前端已启动 (端口 5173)
        set FRONTEND_READY=1
        goto :frontend_done
    )
    timeout /t 1 /nobreak >nul
    echo|set /p="."
)
:frontend_done
if %FRONTEND_READY% equ 0 (
    echo [警告] 前端启动超时 (30秒)
)
echo.

REM ============================================================
REM 8. 显示访问信息
REM ============================================================
echo.
echo ============================================================
echo     服务启动成功！
echo ============================================================
echo.

echo 访问地址:
echo   后端 API:    http://localhost:8080
echo   管理端:      http://localhost:8848
echo   前端:        http://localhost:5173
echo.

echo 测试账号:
echo   系统管理员:  admin / 123456
echo   服务顾问:    advisor1 / 123456
echo   维修技师:    tech1 / 123456
echo   仓库管理员:  warehouse1 / 123456
echo.

echo 提示: 关闭对应的命令窗口即可停止服务
echo.

pause
