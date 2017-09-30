#!/bin/bash

remove_gexd() {
    supervisorctl stop gexd
    supervisorctl remove gexd
}

uninstall_node() {
    if test -e "$USER_HOME/.gex/node"; then
       sudo -u $SUDO_USER gex node uninstall -f
    fi
}

echo "before remove"

. /etc/os-release
VERSION=$ID
if [ "$VERSION" = "ubuntu" ]; then
USER_HOME=$HOME
if test -e "/etc/supervisor/conf.d/gexd.conf"; then
    uninstall_node
    remove_gexd
    rm /etc/supervisor/conf.d/gexd.conf
fi
else
USER_HOME=/home/$SUDO_USER
if test -e "/etc/supervisord.d/gexd.conf"; then
    uninstall_node
    remove_gexd
    rm /etc/supervisord.d/gexd.conf
fi
fi

pkill --oldest ClusterGX

if test -e "/usr/bin/clustergx"; then
    unlink /usr/bin/clustergx
fi





