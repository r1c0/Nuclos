set basedir=%~dp0..

call %basedir%\bin\run-console.cmd nucleus nucleus -importmasterdatalayouts %basedir%\src\de\novabit\nucleus\client\masterdata\layoutml
call %basedir%\bin\run-console.cmd nucleus nucleus -importleasedobjectlayouts %basedir%\src\de\novabit\nucleus\client\genericobject\layoutml
