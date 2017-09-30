#!/bin/bash

echo "after upgrade"

. /etc/os-release
VERSION=$ID

if [ "$VERSION" = "ubuntu" ]; then
USER_HOME=$HOME
SERVICE_HOME="/etc/supervisor/conf.d/gexd.conf"
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

else
USER_HOME=/home/$SUDO_USER
SERVICE_HOME="/etc/supervisord.d/gexd.conf"
sed -i "s/gexuser/$SUDO_USER/g" $SERVICE_HOME 
service supervisord restart 
supervisorctl update
fi

USERNAME=$(stat -c %U $USER_HOME)

chown -R root /etc/gex
chown root /usr/bin/gex 
chown -R root /usr/lib/gex
chmod -R a=rx,u=rwx /etc/gex 
chmod a=rx,u=rwx /usr/bin/gex 
chmod -R a=rx,u=rwx /usr/lib/gex

if [ -d "$USER_HOME/Desktop" ]; then
 ln -s /usr/lib/gex/ui/ClusterGX /usr/bin/clustergx
 cp /usr/share/applications/gex.desktop $USER_HOME/Desktop
 chown $USERNAME:$(id -g -n $USERNAME) $USER_HOME/Desktop/gex.desktop
 chmod +x $USER_HOME/Desktop/gex.desktop
fi

echo "ClusterGX has been updated successfully!"


