#!/bin/bash

JSH_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

JSH_JAR="$JSH_ROOT/target/jsh-1.0-SNAPSHOT.jar"

if [ ! -f "$JSH_JAR" ]; then
    ( cd "$JSH_ROOT" && mvn package ) || exit 1
fi

java -jar "$JSH_JAR" "$@"