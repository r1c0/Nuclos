@echo off
if "%1" == "" goto usage

set login=%1
set basedir=%~dp0..

sqlplus %login% @%basedir%\db\utils\drop-nucleus-schema.sql <%basedir%\db\utils\quit
goto end

:usage
echo Usage:   drop-nucleus-schema ^<oracle-login^>
echo Example: drop-nucleus-schema nucleus/nucleus@oracle

:end
