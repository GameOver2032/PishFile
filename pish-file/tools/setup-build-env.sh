#!/bin/bash
# آماده‌سازی محیط ساخت (لینوکس/CI): JDK 21 + Gradle 8.9 + Android SDK 35 در /opt
set -eux
export DEBIAN_FRONTEND=noninteractive

sudo apt-get update -qq
sudo apt-get install -y -qq openjdk-21-jdk-headless unzip zip curl

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version

mkdir -p /opt/dl /opt/android-sdk/cmdline-tools /opt/fonts
cd /opt/dl

# --- Gradle 8.9 ---
if [ ! -d /opt/dl/gradle-8.9 ]; then
  [ -f gradle.zip ] || curl -sSL -o gradle.zip https://services.gradle.org/distributions/gradle-8.9-bin.zip
  unzip -q -o gradle.zip -d /opt/dl
fi
ln -sfn /opt/dl/gradle-8.9 /opt/dl/gradle

# --- Android cmdline-tools ---
if [ ! -d /opt/android-sdk/cmdline-tools/latest ]; then
  [ -f cmdtools.zip ] || curl -sSL -o cmdtools.zip https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
  unzip -q -o cmdtools.zip -d /opt/android-sdk/cmdline-tools
  mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest
fi

export ANDROID_SDK_ROOT=/opt/android-sdk
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk --licenses >/dev/null 2>&1 || true
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platform-tools" "platforms;android-35" "build-tools;35.0.0"

# --- فونت وزیرمتن ---
if [ ! -d /opt/dl/vazir ]; then
  [ -f /opt/dl/vazir.zip ] || curl -sSL -o /opt/dl/vazir.zip https://github.com/rastikerdar/vazirmatn/releases/download/v33.003/vazirmatn-v33.003.zip
  unzip -q -o /opt/dl/vazir.zip -d /opt/dl/vazir
fi
find /opt/dl/vazir -name "*.ttf" | head -5

echo "INSTALL_DONE"
java -version 2>&1 | head -1
/opt/dl/gradle/bin/gradle --version 2>&1 | head -8

# --- ساخت gradle wrapper برای پروژه ---
if [ -d /home/user/pish-file ]; then
  cd /home/user/pish-file
  export GRADLE_USER_HOME=/opt/gradle-home
  /opt/dl/gradle/bin/gradle wrapper --gradle-version 8.9 --distribution-type bin 2>&1 | tail -3
  ls -la /home/user/pish-file/gradle/wrapper/ 2>&1
  echo "WRAPPER_DONE"
fi
