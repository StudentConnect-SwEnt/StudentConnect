#!/usr/bin/env bash

# This script should be run just once to create the keystore to sign the release APK

set -euo pipefail

KEYSTORE_PATH="release.keystore"
ALIAS="release"
CREDS_FILE="release-keystore-credentials.txt"
BASE64_FILE="release.keystore.base64"

# 1. If the keystore already exists, do nothing
if [[ -f "$KEYSTORE_PATH" ]]; then
  echo "Keystore already exists at: $KEYSTORE_PATH"
  echo "Nothing to do."
  exit 0
fi

echo "Creating new release keystore..."

# 2. Generate strong random passwords
STORE_PASS="$(openssl rand -base64 32)"

# 3. Create keystore
if ! keytool -genkeypair -v -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$STORE_PASS" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=StudentConnect Release,O=StudentConnect-SwEnt,C=CH" > /dev/null 2>&1; then
  echo "Keytool failed"
  exit 1
fi

echo "Keystore created at: $KEYSTORE_PATH"

# 4. Save passwords to a local file (never commit this)
cat > "$CREDS_FILE" <<EOF
ANDROID_KEYSTORE_PASSWORD=$STORE_PASS
EOF
chmod 600 "$CREDS_FILE"

echo "Credentials saved to: $CREDS_FILE"

# 5. Export base64 version for GitHub Secrets
base64 -w0 "$KEYSTORE_PATH" 2>/dev/null > "$BASE64_FILE" || base64 "$KEYSTORE_PATH" > "$BASE64_FILE"

echo "Base64 keystore exported to: $BASE64_FILE"
echo "Add the values in $CREDS_FILE and $BASE64_FILE to GitHub Secrets."
