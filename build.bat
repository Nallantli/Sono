@echo off
echo Compiling class files...
javac -cp src src/main/Main.java -encoding utf8 -d bin
cd bin
echo Creating SonoLang.jar...
jar cvfe SonoLang.jar main.Main ./main
rmdir /s /q main
echo Done.