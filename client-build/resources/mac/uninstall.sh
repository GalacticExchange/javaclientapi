#!/bin/bash

if [ "$EUID" -ne 0 ];then 
  echo ""
  echo "Please run as sudo"
  exit
fi

pkill 'ClusterGX'

if test -e "/$HOME/.gex/node"; then
   echo "Uninstalling node ... "
   sudo -u $SUDO_USER gex node uninstall -f
   echo "Node is uninstalled."
fi

if test -e "/Library/LaunchDaemons/io.galacticexchange.gexd.plist"; then
    echo "Stopping the gexd service ... "
    launchctl stop /Library/LaunchDaemons/io.galacticexchange.gexd.plist
    launchctl unload /Library/LaunchDaemons/io.galacticexchange.gexd.plist
    rm /Library/LaunchDaemons/io.galacticexchange.gexd.plist
    echo "Service stopped."
fi

echo "Removing ClusterGX files ... "

if test -e "/etc/gex"; then
    rm -rf /etc/gex
fi

if test -e "/usr/local/bin/gex"; then
    rm /usr/local/bin/gex
fi

if test -e "/$HOME/.gex"; then
    rm -rf /$HOME/.gex
fi

if test -e "/Library/Application Support/gex"; then
    rm -rf /Library/Application\ Support/gex
fi

if test -e "/Applications/ClusterGX.app"; then
    rm -rf /Applications/ClusterGX.app
fi

if test -e "/$HOME/Library/Application Support/ClusterGX"; then
    rm -rf "/$HOME/Library/Application Support/ClusterGX"
fi

pkgutil --forget io.galacticexchange.gex 

exit 0