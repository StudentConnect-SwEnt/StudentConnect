#!/bin/bash

# Script to get SHA-1 fingerprint for Firebase Google Sign-In setup
# Run this from the project root directory

echo "======================================"
echo "Getting SHA-1 Fingerprints for Firebase"
echo "======================================"
echo ""

echo "DEBUG SHA-1 Fingerprint (for development):"
echo "-------------------------------------------"
./gradlew signingReport 2>/dev/null | grep -A 2 "Variant: debug" | grep "SHA-1:" | head -n 1
echo ""

echo "RELEASE SHA-1 Fingerprint (for production):"
echo "--------------------------------------------"
./gradlew signingReport 2>/dev/null | grep -A 2 "Variant: release" | grep "SHA-1:" | head -n 1
echo ""

echo "======================================"
echo "Next Steps:"
echo "======================================"
echo "1. Copy the DEBUG SHA-1 fingerprint above"
echo "2. Go to Firebase Console: https://console.firebase.google.com/"
echo "3. Select your project: studentconnect-d2dd9"
echo "4. Go to Project Settings (gear icon) > General"
echo "5. Scroll down to 'Your apps' section"
echo "6. Find your Android app (com.github.se.studentconnect)"
echo "7. Click 'Add fingerprint' and paste the SHA-1"
echo "8. Download the new google-services.json file"
echo "9. Replace app/google-services.json with the new file"
echo "10. Rebuild and run your app"
echo ""

