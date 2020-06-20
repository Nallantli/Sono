@echo off
java "-XX:+UseSerialGC" "-Dfile.encoding=UTF-8" -jar "%~dp0/res/sono.jar" %*