# Cocktail Generator

Android app that helps you discover cocktails you can actually make based on what's in your liquor cabinet. Check off the ingredients you have on hand, and the app surfaces every cocktail you can make right now — plus "almost there" cocktails one ingredient away.

## Stack

- **App:** Kotlin + Jetpack Compose (Material 3), Android-only for v1
- **Backend:** Supabase (Postgres + Auth + REST/RPC + Storage)
- **Data:** Seeded from [TheCocktailDB](https://www.thecocktaildb.com/api.php), expanded via a Python scraping pipeline

## Repo layout

```
cocktail-generator/
├── android/          # Kotlin/Compose app (Gradle)
├── scraper/          # Python data pipeline (uv)
├── supabase/         # Postgres schema, RLS, RPCs (standard Supabase layout)
├── docs/             # Plan, ADRs, schema notes
└── .github/workflows # CI
```

## Getting started

### Prerequisites

- JDK 17, Android Studio Iguana+ (or Gradle 8.7+)
- Python 3.12, [uv](https://docs.astral.sh/uv/)
- [Supabase CLI](https://supabase.com/docs/guides/cli)

### Local backend

```bash
supabase start          # spins up local Postgres + Auth on localhost
supabase db reset       # applies everything under supabase/migrations
```

Set the local anon key and URL in `android/local.properties` (see `android/README.md`).

### Seed the database

```bash
cd scraper
uv sync
uv run python -m cocktail_scraper.seed
```

### Run the Android app

```bash
cd android
./gradlew installDebug
```

## Plan

See [docs/plan.md](docs/plan.md) for the full v1 plan, schema, and roadmap.

## License

MIT
