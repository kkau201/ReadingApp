# ReadingApp

Search and book details load from the Google Books API. Those calls need an API key restricted to this Android app. The Firebase key in `google-services.json` is only for Auth and Firestore.

`google-services.json` is part of the app and stays in git. `secrets.properties` is the Books API key and stays on your machine.

## Books API key

1. In your Google Cloud project, enable **Books API**.
2. Create an API key.
3. Restrict it:
   - **Android apps:** package `com.example.readingapp` and the SHA-1 of the keystore that signs the build.
   - **API restrictions:** Books API only.
4. Put the key in `secrets.properties` at the project root:

```
BOOKS_API_KEY=your_key_here
```

5. Rebuild the app. Gradle copies the key into the build, so a running app does not see a change until the next build.

For a debug build, the SHA-1 comes from your local debug keystore:

```
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
```

A release build signed with another keystore needs its own SHA-1 row on the same key.

## Why the signing certificate is sent

Google accepts the key only from the Android app named in the restriction. Each Books request sends two headers:

- `X-Android-Package` is the app package name.
- `X-Android-Cert` is the SHA-1 of the certificate that signed the installed APK.

The app reads that signature at runtime and hashes it, so the value matches the build that is actually running. If either header does not match the Cloud Console row, Google returns 403.

## To do

- [ ] Add Google auth
- [ ] Add delete account
