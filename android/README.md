# Android app

Kotlin + Jetpack Compose (Material 3), Hilt DI, Supabase Kotlin SDK.

## Setup

1. Open the `android/` directory in Android Studio Iguana or newer.
2. Copy `local.properties.example` to `local.properties` and fill in:

   ```properties
   sdk.dir=/path/to/Android/Sdk
   SUPABASE_URL=http://10.0.2.2:54321
   SUPABASE_ANON_KEY=eyJ...   # from `supabase status` in ../db
   ```

3. `./gradlew installDebug`

## Google Sign-in (optional for v1)

The "Continue with Google" button on the auth screen is hidden when
`GOOGLE_WEB_CLIENT_ID` is blank in `local.properties`, so email/password works
without any Google setup. To enable Google:

1. In Google Cloud Console, create an **OAuth 2.0 Client ID** of type
   **Web application** and copy the client ID. Put that value in
   `local.properties` as `GOOGLE_WEB_CLIENT_ID` — the *web* client ID is what
   Credential Manager / Supabase expects, not the Android one.
2. Create a second OAuth 2.0 Client ID of type **Android** with:
   - Package name: `com.koltondecker.cocktailgenerator`
   - SHA-1 fingerprint of your debug keystore
     (`keytool -list -v -keystore ~/.android/debug.keystore` — default password
     is `android`)
3. In the Supabase Dashboard → Authentication → Providers → Google, enable
   Google and paste the **web** client ID (and its client secret) there too.
4. Rebuild — the button appears automatically.

## Layout

```
android/
├── app/
│   └── src/main/java/com/koltondecker/cocktailgenerator/
│       ├── CocktailApp.kt          # Application (Hilt entrypoint)
│       ├── MainActivity.kt         # single-activity host
│       ├── data/                   # Supabase client, repositories
│       │   └── repository/         # AuthRepository, ...
│       ├── domain/                 # models, use cases
│       └── ui/
│           ├── navigation/         # SignedInNav
│           ├── session/            # SessionViewModel (hoisted app-level)
│           ├── theme/              # Material 3 theme
│           └── screens/            # Auth, Home, Pantry, Browse, Detail, Favorites
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml       # version catalog
```

## Architecture

- **MVVM** with a thin `data / domain / ui` split.
- **DI:** Hilt at the Application boundary; screens get their view models via `hiltViewModel()`.
- **Networking:** Supabase Kotlin SDK — `postgrest-kt`, `auth-kt`, `storage-kt`, `realtime-kt`.
- **Local cache:** Room for pantry mirror + last cocktail results; DataStore for prefs.
- **Images:** Coil (Compose-native).
- **Async:** Coroutines + Flow throughout; no RxJava.
