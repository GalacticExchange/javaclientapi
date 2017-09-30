#!/bin/bash

echo "after install"

. /etc/os-release

USER_HOME=/home/$SUDO_USER
SERVICE_HOME="/etc/supervisord.d/gexd.conf"
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

#install supervisor
if ! test -e "/etc/supervisord.conf"; then
	echo "installing supervisor"
	easy_install pip
	pip install supervisor

	mv /usr/lib/gex/supervisord /etc/rc.d/init.d/supervisord
	echo_supervisord_conf > supervisord.conf
	cp supervisord.conf /etc/supervisord.conf
	mkdir -p /etc/supervisord.d/cC
	if ! grep -q "files = /etc/supervisord.d/*.conf" /etc/supervisord.conf; then
		echo "[include]" >> /etc/supervisord.conf
		echo "files = /etc/supervisord.d/*.conf" >> /etc/supervisord.conf
	fi

	chmod +x /etc/rc.d/init.d/supervisord
	chkconfig --add supervisord
	chkconfig supervisord on
	sed -i "s/gexuser/$SUDO_USER/g" $SERVICE_HOME
	service supervisord restart
else
	echo "supervisor is already installed"
	sed -i "s/gexuser/$SUDO_USER/g" $SERVICE_HOME
	service supervisord restart 
	supervisorctl update
fi

chmod +x /usr/bin/unetbootin
echo "ClusterGX has been installed successfully!"




