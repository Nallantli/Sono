@echo off
echo Compiling class files...
javac -sourcepath src src/client/SonoClient.java -encoding utf8 -d bin/temp
cd bin/temp
echo Creating sono.jar...
jar cfe ../res/sono.jar client.SonoClient .
cd ..
rmdir /s /q temp
echo Done.