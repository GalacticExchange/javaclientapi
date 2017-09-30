#!/bin/bash

echo "before upgrade"

GEXD_PID=$(ps o pid,cmd -C java |grep gexd)
if [ ! -z "$GEXD_PID" ]; then 
  supervisorctl stop gexd
fi

supervisorctl update

GEXD_PID=$(supervisorctl avail | grep gexd)
if [ ! -z "$GEXD_PID" ]; then 
  supervisorctl remove gexd
fi

. /etc/os-release

VERSION=$ID
if [ "$VERSION" = "ubuntu" ]; then
USER_HOME=$HOME
if test -e "/etc/supervisor/conf.d/gexd.conf"; then
    rm /etc/supervisor/conf.d/gexd.conf
fi
else
USER_HOME=/home/$SUDO_USER
if test -e "/etc/supervisord.d/gexd.conf"; then
    rm /etc/supervisord.d/gexd.conf
fi
fi

pkill --oldest ClusterGX

if test -e "/usr/bin/clustergx"; then
    unlink /usr/bin/clustergx
fi

if test -e "$USER_HOME/Desktop/gex.desktop"; then
    rm $USER_HOME/Desktop/gex.desktop
fi



