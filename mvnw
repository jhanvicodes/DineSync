#!/bin/sh
# Maven Wrapper script
# Licensed to the Apache Software Foundation (ASF)

set -e

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

# Read the distributionUrl from the properties file
DISTRIBUTION_URL=$(grep "^distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')

# Find JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
fi

JAVA_CMD="$JAVA_HOME/bin/java"

# Bootstrap: download maven if needed
exec "$JAVA_CMD" \
  -jar "$MAVEN_WRAPPER_JAR" \
  "$@"