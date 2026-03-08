@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM 汽车售后维修服务管理平台 - 停止脚本 (Windows)
REM ============================================================

REM 项目根目录
set "PROJECT_ROOT=%~dp0"
cd /d "%PROJECT_ROOT%"

REM 打印横幅
echo.
echo ============================================================
echo     停止所有服务
echo ============================================================
echo.

REM 停止函数
:stop_by_port
setlocal
set PORT=%~1
set SERVICE_NAME=%~2

REM 查找占用端口的进程
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo [警告] 正在停止 %SERVICE_NAME% (端口 %PORT%)...
    
    REM 获取所有占用该端口的PID
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
        set PID=%%a
        echo [信息] 发现进程 PID: !PID!
        taskkill /F /PID !PID! >nul 2>&1
        if %errorlevel% equ 0 (
            echo [√] 进程 !PID! 已停止
        ) else (
            echo [警告] 无法停止进程 !PID!
        )
    )
    
    REM 等待端口释放
    timeout /t 2 /nobreak >nul
    
    REM 再次检查
    netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [警告] 端口 %PORT% 仍被占用，尝试强制终止...
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
            taskkill /F /PID %%a >nul 2>&1
        )
    ) else (
        echo [√] %SERVICE_NAME% 已停止
    )
) else (
    echo [√] %SERVICE_NAME% 未运行
)
endlocal
goto :eof

REM ============================================================
REM 停止所有服务
REM ============================================================
echo [信息] 正在停止所有服务...
echo.

REM 停止后端
call :stop_by_port 8080 "后端服务"
echo.

REM 停止管理端
call :stop_by_port 8848 "管理端"
echo.

REM 停止前端
call :stop_by_port 5173 "前端"
echo.

REM 关闭服务窗口 (通过窗口标题)
echo [信息] 关闭服务窗口...
taskkill /FI "WINDOWTITLE eq 后端服务*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 管理端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 前端*" /F >nul 2>&1
echo [√] 服务窗口已关闭
echo.

REM 清理PID文件
if exist ".pids" (
    del /Q .pids\*.pid >nul 2>&1
    echo [√] PID 文件已清理
    echo.
)

REM 最终检查端口状态
echo [信息] 检查端口状态...
echo.

set PORTS=8080 8848 5173
set PORT_NAMES=后端 管理端 前端
set INDEX=0
set ALL_STOPPED=1

for %%p in (%PORTS%) do (
    netstat -ano | findstr ":%%p " | findstr "LISTENING" >nul
    if %errorlevel% equ 0 (
        echo [警告] 端口 %%p 仍被占用
        set ALL_STOPPED=0
    ) else (
        echo [√] 端口 %%p 已释放
    )
)

echo.
if %ALL_STOPPED% equ 1 (
    echo ============================================================
    echo     所有服务已停止
    echo ============================================================
) else (
    echo ============================================================
    echo     服务已停止 (部分端口可能需要稍等片刻)
    echo ============================================================
)
echo.

pause
