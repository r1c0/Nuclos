#!/bin/sh
#
# nuclos.${server.name}
#
# chkconfig: 2345 90 10
# description:	Startup / Shutdown Nuclos instance ${server.name}

RETVAL=$?
NUCLOS_HOME="${server.home}"

case "$1" in
 start)
        if [ -f $NUCLOS_HOME/bin/startup.sh ];
          then
	    echo $"Starting Nuclos"
            $NUCLOS_HOME/bin/startup.sh
        fi
	;;
 stop)
        if [ -f $NUCLOS_HOME/bin/shutdown.sh ];
          then
	    echo $"Stopping Nuclos"
            $NUCLOS_HOME/bin/shutdown.sh
        fi
 	;;
 *)
 	echo $"Usage: $0 {start|stop}"
	exit 1
	;;
esac

exit $RETVAL