@echo off
rem Nuclos Server Script
if "%OS%" == "Windows_NT" setlocal
set CATALINA_HOME=${server.tomcat.dir}
set JRE_HOME=${server.java.home}
set JAVA_OPTS=-Xmx${server.heap.size}m
call "%CATALINA_HOME%\bin\catalina.bat" run

