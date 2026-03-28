@echo off
chcp 65001 >nul
echo ==========================================
echo   轻墨项目 - 测试运行脚本
 echo ==========================================
echo.

setlocal

:: 检查 gradlew.bat 是否存在
if not exist "gradlew.bat" (
    echo ❌ 错误: 未找到 gradlew.bat 文件
    echo 💡 提示: 请在项目根目录运行此脚本
    pause
    exit /b 1
)

echo [1/3] 正在同步项目依赖...
echo 运行: gradlew clean build --dry-run
echo.
call gradlew.bat clean build --dry-run >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 依赖同步失败
    pause
    exit /b 1
)
echo ✅ 依赖同步成功

echo.
echo [2/3] 正在运行单元测试...
echo 运行: gradlew test
echo.
call gradlew.bat test
if %errorlevel% neq 0 (
    echo ❌ 单元测试失败
    echo 💡 提示: 请查看测试报告了解详细信息
    echo 报告位置: app\build\reports\tests\testDebugUnitTest\index.html
    pause
    exit /b 1
)
echo ✅ 单元测试通过

echo.
echo [3/3] 正在运行代码检查...
echo 运行: gradlew codeCheck
echo.
call gradlew.bat codeCheck
if %errorlevel% neq 0 (
    echo ❌ 代码检查失败
    echo 💡 提示: 运行 gradlew codeFix 自动修复代码风格问题
    pause
    exit /b 1
)
echo ✅ 代码检查通过

echo.
echo ==========================================
echo   测试运行完成！
echo ==========================================
echo.
echo 🎉 所有测试通过，代码质量良好

echo.
echo 📊 测试报告:
echo   - 单元测试: app\build\reports\tests\testDebugUnitTest\index.html
echo   - 代码检查: app\build\reports\detekt\detekt.html
echo   - Lint 检查: app\build\reports\lint-results.html
echo.
pause
