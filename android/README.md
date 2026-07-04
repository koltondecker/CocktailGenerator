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

## Layout

```
android/
├── app/
│   └── src/main/java/com/koltondecker/cocktailgenerator/
│       ├── CocktailApp.kt          # Application (Hilt entrypoint)
│       ├── MainActivity.kt         # single-activity host
│       ├── data/                   # Supabase client, repositories
│       ├── domain/                 # models, use cases
│       └── ui/
│           ├── navigation/         # AppNav.kt
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
