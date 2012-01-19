@echo off
rem Nuclos Client
if "%OS%" == "Windows_NT" setlocal
set NUCLOS_URL=http://localhost:${server.http.port}/${server.name}
set JAVA_HOME=${server.java.home}
set JAVA_OPTS=-Xmx768m
"%JAVA_HOME%\bin\javaw.exe" %JAVA_OPTS% "-Durl.jms=%NUCLOS_URL%/jmsbroker" "-Durl.remoting=%NUCLOS_URL%/remoting" -cp "${server.home}\client\*" org.nuclos.client.main.Main
