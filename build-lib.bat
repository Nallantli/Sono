@echo off
FOR %%F IN (src/ext/LIB_*.java) DO (
	ECHO Compiling %%F...
	javac -cp src src/ext/%%F -encoding utf8 -d bin/lib
	cd bin/lib
	ECHO Creating %%~nF.jar...
	jar cvfe %%~nF.jar ext.%%~nF ./ext
	cd ../..
)
rmdir /s /q "bin/lib/ext"
rmdir /s /q "bin/lib/main"
echo Done.