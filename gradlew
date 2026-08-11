#!/usr/bin/env sh
# Gradle Wrapper bootstrap
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading gradle-wrapper.jar..."
  mkdir -p "$APP_HOME/gradle/wrapper"
  curl -sL -o "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" \
    || wget -q -O "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

# Extract distribution URL
DIST_URL=$(grep distributionUrl "$PROPS" | cut -d= -f2 | tr -d '\r' | sed 's/\\//g')
GRADLE_VER=$(echo "$DIST_URL" | sed -n 's/.*gradle-\(.*\)-bin.zip/\1/p')
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-$GRADLE_VER-bin"

# Use java to run wrapper
exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
