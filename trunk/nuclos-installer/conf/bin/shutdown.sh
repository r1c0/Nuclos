#!/bin/sh

# Nuclos Server Script
export CATALINA_HOME=${server.tomcat.dir}
export JRE_HOME=${server.java.home}

exec "$CATALINA_HOME"/bin/shutdown.sh "$@"