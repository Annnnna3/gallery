#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out art-gallery-qa.jar MANIFEST.MF
mkdir -p out
javac --add-modules jdk.httpserver -encoding UTF-8 -d out src/main/java/com/artgallery/Main.java
cp -R src/main/resources/* out/
printf 'Manifest-Version: 1.0\nMain-Class: com.artgallery.Main\n\n' > MANIFEST.MF
jar cfm art-gallery-qa.jar MANIFEST.MF -C out .
echo "Built: art-gallery-qa.jar"
