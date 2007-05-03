@echo off
setlocal

set basedir=%~dp0..
set username=%1
set password=%2

call %basedir%\bin\run-console.cmd %username% %password% -importmasterdatalayouts %basedir%\src\de\novabit\nucleus\client\masterdata\layoutml
call %basedir%\bin\run-console.cmd %username% %password% -importgenericobjectlayouts %basedir%\src\de\novabit\nucleus\client\genericobject\layoutml
