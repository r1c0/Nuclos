@echo off
setlocal

rem mvn install:install-file  -Dfile=path-to-your-artifact-jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar 
call mvn install:install-file  -Dfile=WinRegistry-4.4.jar -DgroupId=mynuclos -DartifactId=WinRegistry -Dversion=4.4 -Dpackaging=jar 

