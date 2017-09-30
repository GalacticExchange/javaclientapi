#!/bin/bash

echo "after install"

. /etc/os-release

SERVICE_HOME="/etc/supervisor/conf.d/gexd.conf"
USERNAME=$(stat -c %U $HOME)
chown -R root /etc/gex
chown root /usr/bin/gex 
chown -R root /usr/lib/gex
chmod -R a=rx,u=rwx /etc/gex 
chmod a=rx,u=rwx /usr/bin/gex 
chmod -R a=rx,u=rwx /usr/lib/gex

if [ -d "$HOME/Desktop" ]; then
 ln -s /usr/lib/gex/ui/ClusterGX /usr/bin/clustergx
 cp /usr/share/applications/gex.desktop $HOME/Desktop
 chown $USERNAME:$(id -g -n $USERNAME) $HOME/Desktop/gex.desktop
 chmod +x $HOME/Desktop/gex.desktop
fi 

cat >> $SERVICE_HOME <<EOL
[program:gexd]
command=/usr/lib/gex/java/bin/java -jar /usr/lib/gex/gexd.jar
user=${SUDO_USER}
autostart=true
autorestart=true
stderr_logfile=/var/log/gex.err.log
stdout_logfile=/var/log/gex.out.log
EOL

RELEASE=$(lsb_release -r -s)
if [ "$RELEASE" = "16.04" ]; then
  systemctl enable supervisor.service
fi

service supervisor restart 
echo "ClusterGX has been installed successfully!"




