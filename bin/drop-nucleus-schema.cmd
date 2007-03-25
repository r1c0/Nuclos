set login=nucleus/nucleus@oracle
set basedir=%~dp0..

sqlplus %login% @%basedir%\db\utils\drop-nucleus-schema.sql <%basedir%\db\utils\quit
