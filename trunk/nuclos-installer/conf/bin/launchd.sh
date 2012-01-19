#!/bin/bash
#
# nuclos.${server.name}
# description: Startup / Shutdown Nuclos instance ${server.name}

function shutdown()
{
	date
	echo "Shutdown nuclos.${server.name}"
	$CATALINA_HOME/bin/catalina.sh stop
}

date
export CATALINA_HOME=${server.tomcat.dir}
export CATALINA_PID=/tmp/$$
export JRE_HOME=${server.java.home}
export JAVA_OPTS="-Xmx${server.heap.size}m -Djava.awt.headless=true"

echo "Startup nuclos.${server.name}"

. $CATALINA_HOME/bin/catalina.sh start

# Allow any signal which would kill a process to stop nuclos
trap shutdown HUP INT QUIT ABRT KILL ALRM TERM TSTP

echo "Waiting for 'cat $CATALINA_PID'"
wait `cat $CATALINA_PID`