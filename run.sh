#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
java --add-modules jdk.httpserver -jar art-gallery-qa.jar
