#!/usr/bin/env zsh
# $0 can be used to pass down the called hook's name

PATH_TO_SCRIPT=$(readlink -f "$0")
PATH_TO_SCRIPT_DIR=$(dirname "$PATH_TO_SCRIPT")

if [ -d .git ]; then
  echo "GIT directory detected, removing commit-msg hook"
  rm .git/hooks/commit-msg
else
  echo "This is not a GIT dir"
  exit 1
fi;
