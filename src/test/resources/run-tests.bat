@echo off
REM run-tests.bat - Windows批处理脚本

echo ======================================
echo 运行所有单元测试
echo ======================================

REM 运行测试
call mvn clean test

REM 检查测试结果
if %errorlevel% == 0 (
    echo ✅ 所有测试通过！

    REM 生成测试报告
    call mvn surefire-report:report

    echo ======================================
    echo 测试报告位置: target\site\surefire-report.html
    echo ======================================
) else (
    echo ❌ 测试失败，请检查错误信息
    exit /b 1
)

pause