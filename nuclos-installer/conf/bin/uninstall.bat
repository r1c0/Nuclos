@echo off
rem Nuclos Server Script
if "%OS%" == "Windows_NT" setlocal
call "${server.java.home}\bin\java.exe" -jar "${server.home}\bin\uninstaller.jar" "${server.home}\nuclos.xml"
