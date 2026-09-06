@echo off
cd /d %~dp0
if exist out rmdir /s /q out
if exist art-gallery-qa.jar del art-gallery-qa.jar
mkdir out
javac --add-modules jdk.httpserver -encoding UTF-8 -d out src\main\java\com\artgallery\Main.java
xcopy /E /I /Y src\main\resources\* out\ >nul
(
  echo Manifest-Version: 1.0
  echo Main-Class: com.artgallery.Main
  echo.
) > MANIFEST.MF
jar cfm art-gallery-qa.jar MANIFEST.MF -C out .
echo Built: art-gallery-qa.jar
