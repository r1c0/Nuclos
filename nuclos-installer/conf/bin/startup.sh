#!/bin/sh

echo "startup.sh start set variables"

# Nuclos Server Script
export CATALINA_HOME=${server.tomcat.dir}
export JRE_HOME=${server.java.home}
export JAVA_OPTS=-Xmx${server.heap.size}m

echo "startup.sh finish set variables"

exec "$CATALINA_HOME"/bin/startup.sh "$@"
