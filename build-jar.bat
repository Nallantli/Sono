@echo off
echo Compiling class files...
javac -cp src src/main/Main.java -encoding utf8 -d bin
cd bin
echo Creating sono.jar...
jar cvfe res/sono.jar main.Main ./main
rmdir /s /q main
echo Done.