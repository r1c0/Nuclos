@echo off
setlocal

rem set JAVA=P:\Javasoft\JRE\1.5.0.04\bin\javaw
set JAVA=%JAVA_HOME%\bin\javaw.exe

set NUCLEUS_DIR=%~dp0
set NUCLEUS_LIBDIR=%NUCLEUS_DIR%\lib

set CP=%NUCLEUS_LIBDIR%\nucleus-client.jar
set CP=%CP%;%NUCLEUS_DIR%\conf
set CP=%CP%;%NUCLEUS_LIBDIR%\nucleus-xerlin.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\nucleus-help.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\novabit-common.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\log4j-1.2.8.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jlfgr-1_0.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\commons-lang-2.0.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\commons-beanutils-1.5.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\commons-collections-3.2.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\commons-digester-1.3.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\commons-logging-1.1.jar

set CP=%CP%;%NUCLEUS_LIBDIR%\jboss-j2ee.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jboss-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jboss-common-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jboss-system-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jbosssx-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jboss-transaction-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jbossmq-client.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\concurrent.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jnp-client.jar

set CP=%CP%;%NUCLEUS_LIBDIR%\InsaPerformanceLogger.jar
rem set CP=%CP%;%NUCLEUS_LIBDIR%\jdbc2_0-stdext.jar
rem set CP=%CP%;%NUCLEUS_LIBDIR%\jfreechart-0.9.8.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jh.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\StrutLayout1.2.1.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\thirdparty.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\webdavlib.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\dom4j-full.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\xercesImpl.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\xmlParserAPIs.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\davfilechooser.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\itext-1.3.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\poi-2.5-final-20040302.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jasperreports-0.6.6.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jawin.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\jxl.jar
set CP=%CP%;%NUCLEUS_LIBDIR%\layer.jar

rem !!!! logging unter c:\temp funktioniert bei Terminalservern nicht !!!!!

start %JAVA% -ea -ms64m -mx192m -cp %CP% de.novabit.nucleus.client.main.Main
