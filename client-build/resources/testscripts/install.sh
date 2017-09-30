#!/bin/bash

if ! grep -q "deb https://dl.bintray.com/khotkevych/deb $(lsb_release -c -s) main" /etc/apt/sources.list; then
    echo "deb https://dl.bintray.com/khotkevych/deb $(lsb_release -c -s) main" | sudo tee -a /etc/apt/sources.list
fi
apt-get -y --force-yes update
apt-get -y --force-yes install gextest

./test.sh


