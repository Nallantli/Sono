#!/bin/bash
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
java "-Dfile.encoding=UTF-8" -cp "$SCRIPTPATH/SonoClient.jar:$SCRIPTPATH/external/*" client.SonoClient $*