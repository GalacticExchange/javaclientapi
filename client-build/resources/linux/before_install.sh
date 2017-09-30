#!/bin/bash

echo "before install"

# check OS version
. /etc/os-release
if [ "$ID" != "ubuntu" ] && [ "$ID" != "centos" ]; then
   echo "ClusterGX is not supported by your type of OS"
   echo "Please contact us and we will provide it in the next release"
   exit 1	
fi

# check VirtualBox existence 
if ! test -e "/usr/bin/virtualbox"; then
   echo "Dependency problem"
   echo "Package VirtualBox is not installed."
   exit 1
fi

# check VirtualBox existence 
if  test -e "/usr/bin/virtualbox"; then
	# check VirtualBox version
	VERSION="$(VBoxManage -v | tail -1 | awk '{print $NF}')"
	VERSION=${VERSION##* }
	MAJOR=$(echo $VERSION | head -c 1)
	MINOR=${VERSION#*.} 
	MINOR=${MINOR%.*}
	if [ $MAJOR -lt 4 ]; then
	   echo "Dependency problem"
	   echo "You use the old version of VirtualBox. Version 4.3.0 or higher is required."
	   exit 1
	elif [ $MAJOR -eq 4 ] && [ $MINOR -lt 3 ]; then 
	   echo "Dependency problem"
	   echo "You use the old version of VirtualBox. Version 4.3.0 or higher is required."
	   exit 1
	fi
fi

