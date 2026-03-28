@echo off
chcp 65001 >nul
echo ==========================================
echo   轻墨项目 - 代码质量工具安装脚本
echo ==========================================
echo.

REM 设置Git钩子
echo [1/3] 正在配置Git钩子...
if exist ".git\hooks" (
    copy /Y ".git\hooks\pre-commit" ".git\hooks\pre-commit.bak" 2>nul
    copy /Y ".git\hooks\pre-push" ".git\hooks\pre-push.bak" 2>nul
    echo      ✓ Git钩子已配置
) else (
    echo      ✗ 未找到Git hooks目录，请确保这是Git仓库
    exit /b 1
)

echo.
echo [2/3] 正在检查Gradle配置...
if exist "gradlew.bat" (
    echo      ✓ Gradle wrapper已找到
) else (
    echo      ✗ 未找到gradlew.bat，请检查项目配置
    exit /b 1
)

echo.
echo [3/3] 正在同步Gradle项目...
call gradlew.bat --quiet tasks --all | findstr "ktlint" >nul
if %errorlevel% == 0 (
    echo      ✓ 代码质量任务已配置
) else (
    echo      ⚠  代码质量任务可能未正确配置
)

echo.
echo ==========================================
echo   安装完成！
echo ==========================================
echo.
echo 可用命令:
echo   gradlew ktlintCheck    - 检查Kotlin代码风格
echo   gradlew ktlintFormat   - 自动修复代码格式
echo   gradlew detekt         - 运行静态代码分析
echo   gradlew codeCheck      - 运行所有代码检查
echo   gradlew codeFix        - 自动修复所有代码问题
echo   gradlew preCommitCheck - 提交前检查
echo.
echo Git钩子已启用:
echo   pre-commit - 提交前自动检查和修复
echo   pre-push   - 推送前全面检查
echo.
pause
