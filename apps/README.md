# apps/

Put the app you want to test here (`.apk` for Android, `.app` or `.ipa` for iOS), then point the framework at it:

```bash
mvn test -Dplatform=android -Dapp.path=apps/my-app.apk
```

App binaries are git-ignored on purpose: they are large and may be private.
Without `app.path` the framework tests the built-in Settings app of the device, so the sample tests run on any phone.
