@echo off
FOR %%F IN (src/ext/*.java) DO (
	ECHO Compiling %%F...
	javac -cp src src/ext/%%F -encoding utf8 -d bin/lib
)
cd bin/lib
FOR %%F IN (ext/*.class) DO (
	ECHO Creating %%~nF.jar...
	jar cvfe %%~nF.jar ext.%%~nF ./ext
)
rmdir /s /q ext
rmdir /s /q main
echo Done.