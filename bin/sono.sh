#!/bin/bash
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
java "-XX:+UseStringDeduplication" "-Dfile.encoding=UTF-8" -jar "$SCRIPTPATH/res/sono.jar" $*