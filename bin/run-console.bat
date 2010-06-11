@echo on
setlocal

set JAVA=%JAVA_HOME%\bin\java

set CP=%CLASSPATH%;.\conf

set CP=%CP%;.\lib\nuclos-client.jar
set CP=%CP%;.\lib\nuclos-xerlin.jar
set CP=%CP%;.\lib\novabit-common.jar
set CP=%CP%;.\lib\log4j-1.2.8.jar
set CP=%CP%;.\lib\jlfgr-1_0.jar
set CP=%CP%;.\lib\commons-lang-2.0.jar
set CP=%CP%;.\lib\commons-beanutils-1.5.jar
set CP=%CP%;.\lib\commons-collections-3.2.jar
set CP=%CP%;.\lib\commons-logging-1.1.jar

set CP=%CP%;.\lib\jboss-j2ee.jar
set CP=%CP%;.\lib\jboss-client.jar
set CP=%CP%;.\lib\jboss-common-client.jar
set CP=%CP%;.\lib\jboss-system-client.jar
set CP=%CP%;.\lib\jbosssx-client.jar
set CP=%CP%;.\lib\jboss-transaction-client.jar
set CP=%CP%;.\lib\jbossmq-client.jar
set CP=%CP%;.\lib\concurrent.jar
set CP=%CP%;.\lib\jnp-client.jar

set CP=%CP%;.\lib\InsaPerformanceLogger.jar
set CP=%CP%;.\lib\jh.jar
set CP=%CP%;.\lib\StrutLayout1.2.1.jar
set CP=%CP%;.\lib\thirdparty.jar
set CP=%CP%;.\lib\webdavlib.jar
set CP=%CP%;.\lib\dom4j-full.jar
set CP=%CP%;.\lib\xercesImpl.jar
set CP=%CP%;.\lib\xmlParserAPIs.jar
set CP=%CP%;.\lib\davfilechooser.jar
set CP=%CP%;.\lib\itext-1.3.jar
set CP=%CP%;.\lib\poi-2.5-final-20040302.jar
set CP=%CP%;.\lib\jasperreports-3.1.2.jar
set CP=%CP%;.\lib\jawin.jar
set CP=%CP%;.\lib\jxl.jar
set CP=%CP%;.\lib\BrowserLauncher2-1_3.jar
set CP=%CP%;.\lib\commons-codec-1.3.jar

rem !!!! logging unter c:\temp funktioniert bei Terminalservern nicht !!!!!

%JAVA% -ea -ms64m -mx128m -classpath "%CP%" org.nuclos.client.console.NucleusConsole %*
