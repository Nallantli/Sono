@echo off
java "-XX:+AggressiveOpts" "-Dfile.encoding=UTF-8" -cp "%~dp0/res/SonoServer.jar;%~dp0/external/*" server.SonoServer %*