@echo off
if "%1" == "" goto usage

set login=%1
exp %login% owner=nucleus file=nucleus.dmp

:usage
echo Usage:   export-schema ^<oracle-login^>
echo Example: export-schema nucleus/nucleus@oracle

:end
