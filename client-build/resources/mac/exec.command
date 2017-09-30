#!/usr/bin/osascript

on run argv
try
  do shell script item 1 of argv with administrator privileges
  on error errmsg number errNum
  return errNum
end try
return 0
end run

