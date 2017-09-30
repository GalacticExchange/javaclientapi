#!/bin/bash
echo "Clean previous installation"
find /Users/admin/gex_n -name '*.dmg' -delete
find /Users/admin/gex_n -name '*.tool' -delete
rm /Users/admin/gex_n/build/ClusterGX.pkg
rm /Users/admin/gex_n/.data/uninstall.sh
mkdir -p /Users/admin/gex_n/.data

echo "Creating package"
cp "/Volumes/VMware Shared Folders/vagrant/gex_n.pkgproj" /Users/admin/gex_n/gex_n.pkgproj
/usr/local/bin/packagesbuild -v /Users/admin/gex_n/gex_n.pkgproj

echo "Sign package"
productsign --sign "Developer ID Installer: Stan Kladko" '/Users/admin/gex_n/build/ClusterGX.pkg' '/Users/admin/gex_n/build/ClusterGX_s.pkg'
rm '/Users/admin/gex_n/build/ClusterGX.pkg'
mv '/Users/admin/gex_n/build/ClusterGX_s.pkg' '/Users/admin/gex_n/build/ClusterGX.pkg'
echo "Creating DMG"

echo "Copy files"
cp "/Volumes/VMware Shared Folders/vagrant/.data/uninstall.sh" /Users/admin/gex_n/.data
cp "/Volumes/VMware Shared Folders/vagrant/uninstall.tool" /Users/admin/gex_n
cp "/Volumes/VMware Shared Folders/vagrant/Vagrant_Uninstall.tool" /Users/admin/gex_n
cp "/Volumes/VMware Shared Folders/vagrant/VirtualBox_Uninstall.tool" /Users/admin/gex_n

chmod 777 /Users/admin/gex_n/uninstall.tool
chmod 777 /Users/admin/gex_n/VirtualBox_Uninstall.tool
chmod 777 /Users/admin/gex_n/Vagrant_Uninstall.tool
chmod 777 /Users/admin/gex_n/.data/uninstall.sh

codesign --force --verify --verbose --sign "Developer ID Application: Stan Kladko" "/Users/admin/gex_n/VirtualBox_Uninstall.tool"
codesign --force --verify --verbose --sign "Developer ID Application: Stan Kladko" "/Users/admin/gex_n/Vagrant_Uninstall.tool"
codesign --force --verify --verbose --sign "Developer ID Application: Stan Kladko" "/Users/admin/gex_n/uninstall.tool"
codesign --force --verify --verbose --sign "Developer ID Application: Stan Kladko" "/Users/admin/gex_n/.data/uninstall.sh"

appdmg "/Volumes/VMware Shared Folders/vagrant/dmg.json" /Users/admin/gex_n/{dmgname}.dmg
cp /Users/admin/gex_n/{dmgname}.dmg "/Volumes/VMware Shared Folders/vagrant/"


