#!/bin/bash

env="$1"
conf_props="/etc/gex/config.properties"

if [ $env = "-h" ]; then
	echo "Develoment enviroment:\t\t sudo sh change_env.sh dev"
	echo "Local develoment enviroment:\t sudo sh change_env.sh devl"
	echo "Main enviroment:\t\t sudo sh change_env.sh main"
	echo "Production enviroment:\t\t sudo sh change_env.sh prod"
	exit 1
fi

if [ "$(id -u)" != "0" ]; then
	echo "INFO: Yuo should run this script from sudo"
	exit 1
fi


if [ $env = "dev" ]; then
	echo "nodePropertiesFile = node.json
userPropertiesFile = user.json
apiUrl = devapi.gex
rabbitHost = devrabbit.gex
rabbitPort = 5672
proxy = devproxy.gex
webproxy = devwebproxy.gex
hostType = virtualbox
websiteHost = devhub.gex
gatewayIP = 51.1.0.2
rabbitPrefix = gex" > $conf_props

elif [ $env = "devl" ]; then
	echo "nodePropertiesFile = node.json
userPropertiesFile = user.json
apiUrl = devapi.gex
rabbitHost = devrabbit.gex
rabbitPort = 5672
proxy = devproxy.gex
webproxy = devwebproxy.gex
hostType = virtualbox
websiteHost = 0.0.0.0:3000
gatewayIP = 51.1.0.2
rabbitPrefix = gex" > $conf_props

elif [ $env = "main" ]; then
	echo "nodePropertiesFile = node.json
userPropertiesFile = user.json
apiUrl = api.gex
rabbitHost = rabbit.gex
rabbitPort = 5672
proxy = proxy.gex
webproxy = webproxy.gex
hostType = virtualbox
websiteHost = hub.gex
gatewayIP = 51.1.0.2
rabbitPrefix = gex" > $conf_props

elif [ $env = "prod" ]; then
	echo "nodePropertiesFile = node.json
userPropertiesFile = user.json
apiUrl = api.galacticexchange.io
rabbitHost = rabbit.galacticexchange.io
rabbitPort = 443
proxy = proxy.galacticexchange.io
webproxy = webproxy.galacticexchange.io
hostType = virtualbox
websiteHost = hub.galacticexchange.io
gatewayIP = 51.1.0.1
rabbitPrefix = gex" > $conf_props

else
	echo "ERROR: Wrong parameter $env"
fi

supervisorctl restart gexd
eval pkill --oldest ClusterGX
if [ $? = 0 ]; then
	clustergx &
fi
