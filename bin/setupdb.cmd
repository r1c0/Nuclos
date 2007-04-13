@echo off
if "%1" == "" goto usage

set login=%1
set basedir=%~dp0..

call %basedir%\bin\drop-nucleus-schema.cmd %login%

sqlplus %login% @%basedir%\db\nucleus-ddl.sql <%basedir%\db\utils\quit

sqlplus %login% @%basedir%\db\nucleus-dml.sql <%basedir%\db\utils\quit

echo DB Setup complete. Now the app server can be restarted.
echo Don't forget to import the layouts with the import-layouts command.
goto end

:usage
echo Usage:   setupdb ^<oracle-login^>
echo Example: setupdb nucleus/nucleus@oracle

:end
