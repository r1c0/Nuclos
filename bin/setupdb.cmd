set login=nucleus/nucleus@oracle
set basedir=%~dp0..

sqlplus %login% @%basedir%\db\utils\drop-nucleus-schema.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\nucleus-ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\nucleus-dml.sql <%basedir%\db\utils\quit
call %basedir%\bin\import-layouts.cmd
