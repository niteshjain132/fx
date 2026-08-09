#!/usr/bin/env bash
# Build a double-clickable macOS ProjectHub.app (+ optional DMG).
# Must be run on macOS with JDK 21+ (jpackage cannot cross-compile .app from Linux).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MODULE_DIR="$ROOT/projecthub"
DIST_DIR="$MODULE_DIR/target/dist"
ICON_PNG="$MODULE_DIR/packaging/macos/ProjectHub.png"
ICON_ICNS="$MODULE_DIR/packaging/macos/ProjectHub.icns"
APP_NAME="ProjectHub"
BUNDLE_ID="com.cursorws.projecthub"
MAIN_JAR="projecthub-1.0.0-SNAPSHOT.jar"
# Spring Boot 3 executable launcher
MAIN_CLASS="org.springframework.boot.loader.launch.JarLauncher"

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "ERROR: macOS .app packaging must run on a Mac (found $(uname -s))." >&2
  echo "On your MacBook:" >&2
  echo "  cd $ROOT && ./projecthub/scripts/package-macos.sh" >&2
  exit 1
fi

if ! command -v jpackage >/dev/null 2>&1; then
  echo "ERROR: jpackage not found. Install JDK 21+ and ensure it is on PATH." >&2
  exit 1
fi

echo "==> Building Spring Boot fat JAR"
(cd "$ROOT" && mvn -pl projecthub -am clean package -DskipTests)

JAR_PATH="$MODULE_DIR/target/$MAIN_JAR"
if [[ ! -f "$JAR_PATH" ]]; then
  echo "ERROR: expected jar not found: $JAR_PATH" >&2
  exit 1
fi

# Convert PNG → ICNS when iconutil is available
if [[ -f "$ICON_PNG" ]] && command -v iconutil >/dev/null 2>&1; then
  echo "==> Generating $ICON_ICNS"
  ICONSET="$(mktemp -d)/ProjectHub.iconset"
  mkdir -p "$ICONSET"
  for s in 16 32 64 128 256 512 1024; do
    sips -z "$s" "$s" "$ICON_PNG" --out "$ICONSET/icon_${s}x${s}.png" >/dev/null
    if (( s <= 512 )); then
      double=$((s * 2))
      sips -z "$double" "$double" "$ICON_PNG" --out "$ICONSET/icon_${s}x${s}@2x.png" >/dev/null
    fi
  done
  iconutil -c icns "$ICONSET" -o "$ICON_ICNS"
  rm -rf "$(dirname "$ICONSET")"
fi

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/input"
cp "$JAR_PATH" "$DIST_DIR/input/"

ICON_ARGS=()
if [[ -f "$ICON_ICNS" ]]; then
  ICON_ARGS=(--icon "$ICON_ICNS")
elif [[ -f "$ICON_PNG" ]]; then
  ICON_ARGS=(--icon "$ICON_PNG")
fi

echo "==> Creating ProjectHub.app (embeds Java runtime — no JDK required on target Mac)"
jpackage \
  --type app-image \
  --name "$APP_NAME" \
  --app-version "1.0.0" \
  --vendor "cursor-ws" \
  --description "Engineering project dashboard" \
  --input "$DIST_DIR/input" \
  --dest "$DIST_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Xmx512m" \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --mac-package-name "$APP_NAME" \
  --mac-package-identifier "$BUNDLE_ID" \
  "${ICON_ARGS[@]}"

echo "==> Creating ProjectHub-1.0.0.dmg"
jpackage \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "1.0.0" \
  --vendor "cursor-ws" \
  --description "Engineering project dashboard" \
  --input "$DIST_DIR/input" \
  --dest "$DIST_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Xmx512m" \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --mac-package-name "$APP_NAME" \
  --mac-package-identifier "$BUNDLE_ID" \
  "${ICON_ARGS[@]}"

echo
echo "Done."
echo "  App:  $DIST_DIR/$APP_NAME.app"
echo "  DMG:  $DIST_DIR/$APP_NAME-1.0.0.dmg"
echo
echo "Install: open the DMG and drag ProjectHub.app to Applications,"
echo "or:      open \"$DIST_DIR/$APP_NAME.app\""
