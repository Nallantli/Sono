@echo off
echo Building Libraries...
javac -cp src src/ext/Console.java -encoding utf8 -d bin/lib
javac -cp src src/ext/FileIO.java -encoding utf8 -d bin/lib
cd bin/lib
echo Creating Console.jar...
jar cvfe Console.jar ext.Console ./ext
echo Creating FileIO.jar...
jar cvfe FileIO.jar ext.FileIO ./ext
rmdir /s /q ext
rmdir /s /q main
echo Done.