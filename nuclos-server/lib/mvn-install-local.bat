@echo off
setlocal

rem mvn install:install-file  -Dfile=path-to-your-artifact-jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar 
call mvn install:install-file  -Dfile=javaparser-1.0.8.jar -DgroupId=mynuclos -DartifactId=javaparser -Dversion=1.0.8 -Dpackaging=jar 
call mvn install:install-file  -Dfile=ojdbc6.jar -DgroupId=mynuclos -DartifactId=ojdbc -Dversion=6 -Dpackaging=jar 
call mvn install:install-file  -Dfile=sqljdbc.jar -DgroupId=mynuclos -DartifactId=sqljdbc -Dversion=1.0 -Dpackaging=jar 
call mvn install:install-file  -Dfile=jconn3.jar -DgroupId=mynuclos -DartifactId=jconn -Dversion=3 -Dpackaging=jar 

