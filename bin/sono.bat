@echo off
java "-XX:+AggressiveOpts" "-Dfile.encoding=UTF-8" -cp "%~dp0/res/sono.jar" client.SonoClient %*