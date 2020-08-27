@echo off
echo Compiling class files...
javac -cp bin/external/* -sourcepath src src/client/SonoClient.java -encoding utf8 -d bin/temp
cd bin/temp
echo Creating SonoClient.jar...
jar cfe ../SonoClient.jar client.SonoClient .
cd ..
rmdir /s /q temp
echo Done.