#!/usr/bin/expect -f

set gexpassword "PH_GEX_PASSWD1"
set userpassword "PH_GEX_PASSWD1"
set username "corp4"
set starting_timeout 120
set timeout 10

#if 0 {
eval spawn "gex login $username"
expect {
  	timeout { send_user "\nlLogin timeout failure\n"; exit 1 }
  	"*assword:*" { send -- "$gexpassword\r" }
  	eof { send_user "\nLogin failed\n"; exit 1 }
}

set timeout 300
eval spawn "gex node install"
expect {
	timeout {  send_user "\nNode install timeout failure\n"; exit 1 }
 	"*] password for *" { send -- "$userpassword\r"; exp_continue }
   
}

set timeout 10
# wait until node gets JOINED state or an error appears
while { 1 } {
	eval spawn "gex node state"
	expect {
		timeout { send_user "\nNode state timeout failure\n"; exit 1 }
	  	-re "Node status:\[ \t\]*(STARTING|INSTALLING)" { send_user -- "\n ... WAITING $starting_timeout s ...\r"; sleep $starting_timeout }
	  	-re "Node status:\[ \t\]*JOINED" { break }
	  	eof { send_user "\nWrong node state\n"; exit 1 }
	}
}

set timeout 120
eval spawn "gex node uninstall"
expect {
  	timeout { send_user "\nNode uninstall timeout failure\n"; exit 1 } 
  	-re ".*Y\/n.*" { send -- "Y\r"; exp_continue }
}