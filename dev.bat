@echo off
REM 开发脚本入口，实际逻辑在 dev.ps1
REM 用法: dev.bat build / dev.bat runClient / dev.bat runServer
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev.ps1" %*
exit /b %ERRORLEVEL%
