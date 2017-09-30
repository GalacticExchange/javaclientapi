#!/bin/sh
[ $# -eq 0 ] && {
  echo "$(basename $0): no command" >&2
  exit 1
}
"$@" || {
  echo "failed: $?"
  exec $SHELL
}