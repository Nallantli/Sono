@echo off
java "-Dfile.encoding=UTF-8" -cp "%~dp0/SonoClient.jar;%~dp0/external/*" client.SonoClient %*