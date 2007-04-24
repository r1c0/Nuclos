@echo off

rem examples:
rem run-console nucleus nucleus -showjobs
rem run-console nucleus nucleus -unschedulejob \"TimelimitJob 10:30\"
rem Note that you have to escape the quotation mark with a backslash on the comm

pushd %~dp0\..
call ant run.console -Dconsole-args="%*"
popd
