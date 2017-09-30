#!/bin/sh
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
if test -e "/usr/lib/gex/java/bin/java"; then
    java="/usr/lib/gex/java/bin/java"
elif test -e "/Library/Application Support/gex/java/Contents/Home/bin/java"; then
    java="/Library/Application Support/gex/java/Contents/Home/bin/java"
else
    java=java
    if test -n "$JAVA_HOME"; then
        java="$JAVA_HOME/bin/java"
    fi
fi
exec "$java" $java_args -jar $MYSELF "$@"
exit 1 
