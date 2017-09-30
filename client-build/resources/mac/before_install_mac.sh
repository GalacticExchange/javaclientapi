#!/bin/bash

if [ "$USER" == "root" ]; then
osascript -e 'tell app "System Events" to display dialog "To install ClusterGX package use .dmg file or run Installer as a not-root user" buttons {"OK"}'
exit 1
fi

if test -e "/Library/LaunchDaemons/io.galacticexchange.gexd.plist"; then
    launchctl stop /Library/LaunchDaemons/io.galacticexchange.gexd.plist
    launchctl unload /Library/LaunchDaemons/io.galacticexchange.gexd.plist
fi

osascript -e 'quit app "ClusterGX"'

if test -e "/Library/Application Support/gex/java"; then
    rm -rf /Library/Application\ Support/gex/java
fi
