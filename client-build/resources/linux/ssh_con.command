#!/usr/bin/expect -f
set gexhost [lindex $argv 0];
set gexport [lindex $argv 1];
set gexpassword [lindex $argv 2];
set osname [lindex $argv 3];
set gexproxyhost [lindex $argv 4];
set gexproxyport [lindex $argv 5];
set gexproxyusername [lindex $argv 6];
set gexproxypassword [lindex $argv 7];
set timeout 30
log_user 0

puts "Connecting..."

set sshcommand "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ";
if {($gexproxyhost ne "" && $gexproxyport ne "")} {
    if {$osname eq "linux"} {
      append sshcommand "-o \"ProxyCommand=ncat"
    } else {
      append sshcommand "-o \"ProxyCommand='/Library/Application Support/gex/ncat'"
    }
    append sshcommand " --proxy-type socks5 --proxy $gexproxyhost:$gexproxyport"
    if {($gexproxyusername ne "")} {
        append sshcommand " --proxy-auth $gexproxyusername"
        if {($gexproxypassword ne "")} {
            append sshcommand ":$gexproxypassword"
        }
    }
    append sshcommand " %h %p\" "
}
append sshcommand "-p $gexport $gexhost"

eval spawn $sshcommand
expect {
  timeout { send_user "\nFailed to get password prompt\n"; exit 1 }
  eof { }
  "*assword:*" {
      send -- "$gexpassword\r"
      exp_continue
  }
  -re {.*:.?~.*} {
    send -- "exit\r"
    exp_continue
  }
}

eval spawn $sshcommand
expect {
  timeout { send_user "\nFailed to get password prompt\n"; exit 1 }
  eof { send_user "\nSSH failure for $gexhost:$gexport\n"; exit 1 }
  "*assword:*"
}
send -- "$gexpassword\r"
interact
