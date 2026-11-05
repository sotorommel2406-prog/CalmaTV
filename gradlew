#!/bin/sh
# Gradle start up script for POSIX systems (Calma TV)
app_path=$0
while [ -h "$app_path" ]; do
    ls=$(ls -ld "$app_path")
    link=${ls#*' -> '}
    case $link in
        /*) app_path=$link ;;
        *) app_path=$(dirname "$app_path")/$link ;;
    esac
done
APP_HOME=$(cd "$(dirname "$app_path")" > /dev/null && pwd -P) || exit 1

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
    command -v java > /dev/null 2>&1 || {
        echo "ERROR: JAVA_HOME no está definido y no se encontró 'java' en el PATH." >&2
        exit 1
    }
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$CLASSPATH" ]; then
    echo "ERROR: falta gradle/wrapper/gradle-wrapper.jar (ver README.md)." >&2
    exit 1
fi

exec "$JAVACMD" -Xmx64m -Xms64m $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=gradlew" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
