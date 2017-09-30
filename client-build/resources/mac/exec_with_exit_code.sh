#!/bin/bash

CODE=$(/Library/Application\ Support/gex/exec.command "$@")
if [ "$CODE" = "-128" ]; then
   (>&2 echo "Execution error: User canceled.")
fi
if [ "$CODE" != "0" ]; then
   (>&2 echo "Execution terminated with exit code: "$CODE)
   exit 1
fi
