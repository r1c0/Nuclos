set login=nucleus/nucleus@oracle
set basedir=%~dp0..

call %basedir%\bin\drop-nucleus-schema.cmd

sqlplus %login% @%basedir%\db\nucleus-ddl.sql <%basedir%\db\utils\quit

sqlplus %login% @%basedir%\db\nucleus-dml.sql <%basedir%\db\utils\quit

call %basedir%\bin\import-layouts.cmd
