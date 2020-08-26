@echo off
echo Compiling class files...
javac -cp bin-server/external/* -sourcepath src src/server/SonoServer.java -encoding utf8 -d bin-server/temp
cd bin-server/temp
echo Creating SonoServer.jar...
jar cfe ../SonoServer.jar server.SonoServer .
cd ..
rmdir /s /q temp
echo Done.