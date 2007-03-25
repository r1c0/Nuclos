set login=nucleus/nucleus@oracle
set basedir=%~dp0..

rem drop-schema
sqlplus %login% @%basedir%\db\utils\drop-nucleus-schema.sql <%basedir%\db/utils/quit

rem setup ddl
sqlplus %login% @%basedir%\db\ddl\tables_ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\ddl\views_ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\ddl\sequences_ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\ddl\synonyms_ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\ddl\functions_ddl.sql <%basedir%\db\utils\quit
sqlplus %login% @%basedir%\db\ddl\procedures_ddl.sql <%basedir%\db\utils\quit

rem setup dml
sqlplus %login% @%basedir%\db\dml\nucleus-dml.sql <%basedir%\db\utils\quit

call %basedir%\bin\import-layouts.cmd
