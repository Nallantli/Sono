#!/bin/bash
echo "Compiling class files..."
javac -cp "bin/external/*" -sourcepath src "src/server/SonoServer.java" -encoding utf8 -d "bin/temp"
cd bin/temp
echo "Creating SonoServer.jar..."
jar cfe "../res/SonoServer.jar" server.SonoServer .
cd ..
rm -rf temp
echo "Done."