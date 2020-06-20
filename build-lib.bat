@echo off
FOR %%F IN (src/ext/LIB_*.java) DO (
	ECHO Compiling %%F...
	javac -cp src src/ext/%%F -encoding utf8 -d bin/lib
)
cd bin/lib
FOR %%F IN (ext/LIB_*.class) DO (
	ECHO Creating %%~nF.jar...
	jar cvfe %%~nF.jar ext.%%~nF ./ext
)
rmdir /s /q ext
rmdir /s /q main
echo Done.