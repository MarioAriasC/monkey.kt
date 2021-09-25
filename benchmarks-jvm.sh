#!/usr/bin/env bash
./gradlew monkey-jvm:installDist
./monkey-jvm/build/install/monkey-jvm/bin/monkey-jvm "$1"