#!/bin/bash

#unpack java
tar -zxvf /Library/Application\ Support/gex/java.tar.gz -C /Library/Application\ Support/gex/
rm /Library/Application\ Support/gex/java.tar.gz

chmod -R a+x /Library/Application\ Support/gex 
chmod -R a+x /Applications/ClusterGX.app
chmod a+x /usr/local/bin/gex
chmod a+x /usr/local/bin/clustergx
chmod -R a+x /Library/Application\ Support/gex
sed -i '' "s/gexuser/$USER/g" /Library/LaunchDaemons/io.galacticexchange.gexd.plist
chmod 600 /Library/LaunchDaemons/io.galacticexchange.gexd.plist
launchctl load /Library/LaunchDaemons/io.galacticexchange.gexd.plist
