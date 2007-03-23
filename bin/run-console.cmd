@echo off

rem examples:
rem run-console elisa elisa -showjobs
rem run-console elisa elisa -unschedulejob \"TimelimitJob 10:30\"
rem Note that you have to escape the quotation mark with a backslash on the comm

pushd %~d0%~p0\..
call ant run.console -Dconsole-args="%*"
popd
