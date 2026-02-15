@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

echo ============================================================
echo    CCZU-WuXin 安装包构建脚本
echo    常州大学竞赛信息聚合平台
echo ============================================================
echo.

REM ============================================================
REM 前置条件检查
REM ============================================================

REM --- 检查 JDK 17+ ---
REM 优先使用 JPACKAGE_HOME，其次 JAVA_HOME，最后 PATH 中的 java
set "JDK_HOME="

if defined JPACKAGE_HOME (
    if exist "%JPACKAGE_HOME%\bin\jpackage.exe" (
        set "JDK_HOME=%JPACKAGE_HOME%"
        echo [√] 使用 JPACKAGE_HOME: %JPACKAGE_HOME%
    )
)

if not defined JDK_HOME (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" (
            set "JDK_HOME=%JAVA_HOME%"
            echo [√] 使用 JAVA_HOME: %JAVA_HOME%
        )
    )
)

if not defined JDK_HOME (
    REM 在常见安装目录中搜索 JDK 17+
    for /d %%d in (
        "C:\Program Files\Java\jdk-*"
        "C:\Program Files\Eclipse Adoptium\jdk-*"
        "C:\Program Files\Microsoft\jdk-*"
        "C:\Program Files\BellSoft\LibericaJDK-*"
        "%USERPROFILE%\.jdks\*"
    ) do (
        if exist "%%d\bin\jpackage.exe" (
            set "JDK_HOME=%%d"
        )
    )
    if defined JDK_HOME (
        echo [√] 自动发现 JDK: !JDK_HOME!
    )
)

if not defined JDK_HOME (
    echo [×] 未找到 JDK 17+（需要 jpackage 工具）
    echo.
    echo     请安装 JDK 17 或更高版本：
    echo     - Eclipse Temurin: https://adoptium.net/
    echo     - Oracle JDK:     https://jdk.java.net/
    echo.
    echo     安装后设置环境变量：
    echo     set JPACKAGE_HOME=C:\path\to\jdk-17
    echo.
    echo     或重新运行此脚本。
    pause
    exit /b 1
)

set "JPACKAGE=%JDK_HOME%\bin\jpackage.exe"
set "JAVA=%JDK_HOME%\bin\java.exe"

REM 验证 JDK 版本 >= 17
for /f "tokens=2 delims=." %%v in ('"%JAVA%" -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%v"
)

echo [i] JDK 路径: %JDK_HOME%

REM --- 检查 Maven ---
set "MVN_CMD="

where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
    echo [√] Maven 已在 PATH 中
) else if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
        echo [√] 使用 MAVEN_HOME: %MAVEN_HOME%
    )
) else if defined M2_HOME (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        set "MVN_CMD=%M2_HOME%\bin\mvn.cmd"
        echo [√] 使用 M2_HOME: %M2_HOME%
    )
) else (
    REM 在常见目录搜索 Maven
    for /d %%d in (
        "C:\apache-maven-*"
        "C:\Program Files\apache-maven-*"
        "%USERPROFILE%\apache-maven-*"
    ) do (
        if exist "%%d\bin\mvn.cmd" (
            set "MVN_CMD=%%d\bin\mvn.cmd"
        )
    )
    if defined MVN_CMD (
        echo [√] 自动发现 Maven: !MVN_CMD!
    )
)

if not defined MVN_CMD (
    echo [×] 未找到 Maven
    echo.
    echo     请安装 Maven 3.6+：
    echo     https://maven.apache.org/download.cgi
    echo.
    echo     安装后设置环境变量 MAVEN_HOME 或将 bin 目录加入 PATH。
    pause
    exit /b 1
)

echo.

REM ============================================================
REM 步骤 1：Maven 编译打包
REM ============================================================
echo [1/4] 编译打包 Spring Boot JAR...
echo.

REM 使用发现的 JDK 进行编译
set "JAVA_HOME=%JDK_HOME%"
call "%MVN_CMD%" -f "%~dp0pom.xml" clean package -DskipTests -q

if %errorlevel% neq 0 (
    echo.
    echo [×] Maven 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo [√] JAR 编译成功
echo.

REM ============================================================
REM 步骤 2：准备打包目录
REM ============================================================
echo [2/4] 准备打包目录...

set "STAGING=%~dp0build\staging"
set "OUTPUT=%~dp0build\output"

if exist "%STAGING%" rmdir /s /q "%STAGING%"
if exist "%OUTPUT%" rmdir /s /q "%OUTPUT%"
mkdir "%STAGING%"
mkdir "%OUTPUT%"

copy "%~dp0target\wuxin-1.0.0.jar" "%STAGING%\" >nul

echo [√] 打包目录准备完成
echo.

REM ============================================================
REM 步骤 3：生成安装包
REM ============================================================

REM 检查是否有 WiX Toolset（生成 exe 安装包需要）
set "HAS_WIX=0"
where candle >nul 2>&1
if %errorlevel% equ 0 (
    set "HAS_WIX=1"
) else if exist "C:\Program Files (x86)\WiX Toolset v3*\bin\candle.exe" (
    set "HAS_WIX=1"
)

if "%HAS_WIX%"=="1" (
    echo [3/4] 生成 EXE 安装包（检测到 WiX Toolset）...
    echo.

    "%JPACKAGE%" ^
        --input "%STAGING%" ^
        --name "CCZU-WuXin" ^
        --main-jar wuxin-1.0.0.jar ^
        --main-class org.springframework.boot.loader.JarLauncher ^
        --type exe ^
        --app-version 1.2.0 ^
        --vendor "CCZU-WuXin" ^
        --description "常州大学竞赛信息聚合平台" ^
        --win-dir-chooser ^
        --win-shortcut ^
        --win-menu ^
        --win-console ^
        --dest "%OUTPUT%"

    if %errorlevel% equ 0 (
        echo.
        echo [√] EXE 安装包生成成功！
        echo     位置: %OUTPUT%\CCZU-WuXin-1.2.0.exe
    ) else (
        echo [×] 安装包生成失败，回退到 app-image 模式...
        goto :app_image
    )
) else (
    echo [3/4] 未检测到 WiX Toolset，使用 app-image 模式...
    echo.
    echo     提示：安装 WiX Toolset 可生成标准 EXE 安装包
    echo     下载：https://github.com/wixtoolset/wix3/releases
    echo.

    :app_image
    "%JPACKAGE%" ^
        --input "%STAGING%" ^
        --name "CCZU-WuXin" ^
        --main-jar wuxin-1.0.0.jar ^
        --main-class org.springframework.boot.loader.JarLauncher ^
        --type app-image ^
        --app-version 1.2.0 ^
        --vendor "CCZU-WuXin" ^
        --description "常州大学竞赛信息聚合平台" ^
        --win-console ^
        --dest "%OUTPUT%"

    if %errorlevel% neq 0 (
        echo.
        echo [×] 打包失败！请检查错误信息。
        pause
        exit /b 1
    )

    echo [√] 应用目录生成成功
    echo.

    REM 压缩为 zip
    echo [4/4] 压缩为 ZIP 分发包...

    set "ZIP_FILE=%OUTPUT%\CCZU-WuXin-1.2.0-portable.zip"

    powershell -NoProfile -Command "Compress-Archive -Path '%OUTPUT%\CCZU-WuXin\*' -DestinationPath '%ZIP_FILE%' -Force"

    if %errorlevel% equ 0 (
        echo.
        echo [√] ZIP 分发包生成成功！
        echo     位置: %ZIP_FILE%
        echo.
        echo     使用方法：
        echo     1. 解压到任意目录
        echo     2. 双击 CCZU-WuXin.exe 启动
        echo     3. 浏览器自动打开 http://localhost:8888
    ) else (
        echo.
        echo [√] 应用目录已生成（ZIP 压缩跳过）
        echo     位置: %OUTPUT%\CCZU-WuXin\
        echo     双击 CCZU-WuXin.exe 启动
    )
)

echo.
echo ============================================================
echo    构建完成！
echo ============================================================
echo.
echo    邮件通知配置（可选）：
echo    编辑 %USERPROFILE%\.cczu-wuxin\config.yml
echo.
pause
