@echo off
setlocal

set basedir=%~dp0..
set username=%1
set password=%2
set server=%3

call %basedir%\bin\run-console.cmd %username% %password% %server% -importmasterdatalayouts %basedir%\src\layoutml\masterdata
call %basedir%\bin\run-console.cmd %username% %password% %server% -importgenericobjectlayouts %basedir%\src\layoutml\genericobject
