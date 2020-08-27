#!/bin/bash
echo "Compiling class files..".
javac -cp "bin/external/*" -sourcepath src "src/client/SonoClient.java" -encoding utf8 -d "bin/temp"
cd bin/temp
echo "Creating SonoClient.jar..."
jar cfe "../SonoClient.jar" client.SonoClient .
cd ..
rm -rf temp
echo "Done."