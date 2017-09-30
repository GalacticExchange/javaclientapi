#!/bin/bash

echo "after remove"

. /etc/os-release
VERSION=$ID
if [ "$VERSION" = "ubuntu" ]; then
USER_HOME=$HOME
else
USER_HOME=/home/$SUDO_USER
fi

if test -e "$USER_HOME/Desktop/gex.desktop"; then
    rm /$USER_HOME/Desktop/gex.desktop
fi

if test -e "$USER_HOME/.gex"; then
    rm -rf /$USER_HOME/.gex
fi

if test -e "/etc/gex"; then
    rm -rf /etc/gex
fi

find /var/log -name 'gex*.log' -delete

if test -e "$USER_HOME/.config/ClusterGX"; then
    rm -r "$USER_HOME/.config/ClusterGX"
fi
