#!/bin/bash
echo ""
echo "Do you wish to uninstall ClusterGX (Yes/No)?"
read ANSW
if [ $(echo "$ANSW" | tr '[:upper:]' '[:lower:]') != "yes" ]; then
	echo "Aborting uninstall. (answer: '$ANSW')".
	exit 2;
fi
echo ""

DIR="$(cd "$(dirname "$0")" && pwd)"
if test -e "/Library/Application Support/gex/uninstall.sh"; then
	DIR="/Library/Application Support/gex/uninstall.sh"
elif test -e "$DIR/.data/uninstall.sh"; then
	DIR="$DIR/.data/uninstall.sh"
else
	echo "Unable to find uninstallation files"
	exit 1
fi

sudo sh "$DIR"
echo "ClusterGX uninstall was successfully completed."
echo ""
exit 0

