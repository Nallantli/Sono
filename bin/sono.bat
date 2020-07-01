@echo off
java "-XX:+UseStringDeduplication" "-Dfile.encoding=UTF-8" -jar "%~dp0/res/sono.jar" %*