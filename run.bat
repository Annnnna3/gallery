@echo off
cd /d %~dp0
java --add-modules jdk.httpserver -jar art-gallery-qa.jar
