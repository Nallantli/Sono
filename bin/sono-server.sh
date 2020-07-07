#!/bin/bash
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
java "-XX:+UseStringDeduplication" "-Dfile.encoding=UTF-8" -cp "$SCRIPTPATH/res/SonoServer.jar:$SCRIPTPATH/external/*" server.SonoServer $*