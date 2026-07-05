# Cocktail Generator Android App — Plan

## Context

Greenfield project: a new GitHub repo for an Android app that helps users discover cocktails they can actually make based on what's in their liquor cabinet. Users check off ingredients they have (organized by category), and the app surfaces every cocktail they can make right now — plus "almost there" cocktails one ingredient away. The recipe corpus is seeded from a public API and expanded via a scraping pipeline. Users have accounts so their pantry, favorites, and notes travel with them. The stack is picked for snappy UX and fast iteration.

## Decisions locked in (from clarifying questions)

- **App:** Kotlin + Jetpack Compose (Material 3), Android-only for v1
- **Backend:** Supabase (Postgres + Auth + REST/RPC + Storage)
- **Data source:** Seed from TheCocktailDB API, then Python scrapers to expand
- **V1 features:** favorites + personal notes, "almost there" (1 missing) matches, filters (spirit / ABV / flavor / difficulty). Community ratings/reviews are **out** for v1.

## Repo structure

Single monorepo `cocktail-generator`. Simple to iterate on early; splittable later.

```
cocktail-generator/
├── android/          # Kotlin/Compose app (Gradle)
├── scraper/          # Python data pipeline
├── supabase/         # Postgres schema, RLS, RPCs (standard Supabase layout)
├── docs/             # ADRs, schema diagram, scraping notes
├── .github/workflows # CI for android build + scraper lint
└── README.md
```

## Database schema (Postgres via Supabase)

Core tables — keep normalized so ingredient-based queries stay fast.

- `ingredient_categories` — `id`, `name`, `sort_order` (Spirits, Liqueurs, Mixers, Bitters, Juices, Fresh, Syrups, Garnishes)
- `ingredients` — `id`, `name` (canonical), `category_id`, `aliases text[]`, `is_common bool` (drives the pantry checklist)
- `cocktails` — `id`, `name`, `description`, `glass`, `method` (shaken/stirred/built), `garnish`, `instructions`, `difficulty` (1–5), `abv_estimate`, `flavor_tags text[]`, `source_url`, `source_name`, `image_url`
- `cocktail_ingredients` — `cocktail_id`, `ingredient_id`, `quantity numeric`, `unit`, `is_optional bool`, `position int`
- `user_pantry` — `user_id`, `ingredient_id` (composite PK)
- `user_favorites` — `user_id`, `cocktail_id`
- `user_notes` — `user_id`, `cocktail_id`, `body`, `personal_rating smallint`, `updated_at`

Auth users come from Supabase's built-in `auth.users`. Row-level security (RLS) on every user-scoped table so a user can only read/write their own rows.

**Matching logic — Postgres RPC (server-side, single round-trip):**

- `rpc.match_cocktails(missing_allowed int, filters jsonb)` — returns cocktails where the count of required (non-optional) ingredients not in the user's pantry is `≤ missing_allowed`, plus the missing-ingredient list for each. Ordered by fewest missing, then popularity/favorites.

This one function powers "makeable now" (`missing_allowed=0`) and "almost there" (`missing_allowed=1`) — no client-side ingredient math.

## Android app architecture

- **Language/UI:** Kotlin 2.x, Jetpack Compose + Material 3, Compose Navigation
- **Architecture:** MVVM with a thin `data / domain / ui` split
- **DI:** Hilt (Google's standard, best Compose ergonomics)
- **Networking:** Supabase Kotlin SDK (`io.github.jan-tennert.supabase:postgrest-kt`, `auth-kt`, `storage-kt`)
- **Local cache:** Room for pantry + last-known cocktail results (offline-friendly), DataStore for prefs
- **Images:** Coil (Compose-native)
- **Async:** Kotlin Coroutines + Flow throughout

### Screens (v1)

1. **Auth** — email/password + Google sign-in via Supabase Auth
2. **Home / Discover** — hero carousel of "makeable now" + featured
3. **My Pantry** — categorized ingredient list with sticky category headers, search, toggle chips. Instant local update, background sync.
4. **Browse Cocktails** — tabs: "Can make", "1 away", "All". Filter sheet (spirit, ABV range, flavor tags, difficulty).
5. **Cocktail Detail** — full recipe, ingredient list with your-pantry-highlighted, method, source link, favorite/note buttons.
6. **Favorites** — grid of saved cocktails
7. **Profile / Settings** — account, sign out, unit preference (oz vs ml)

### "Flashy" UX touches to plan for

- Motion: shared-element transitions from card → detail (Compose `SharedTransitionLayout`)
- Themed Material 3 dynamic color, dark mode default
- Skeleton loaders (not spinners) on lists
- Haptics on pantry toggle
- Empty states with illustrations

## Data pipeline (`/scraper`)

- Python 3.12, `uv` for env + deps
- **Stage 1 — Seed:** pull all cocktails + ingredients from TheCocktailDB free API, upsert into Supabase via `postgrest-py`
- **Stage 2 — Normalizer:** ingredient dedup / alias resolution (RapidFuzz for fuzzy matches; manual review file for ambiguous ones)
- **Stage 3 — Scrapers:** modular per-source (Difford's Guide, Liquor.com, Punch, Imbibe). Each scraper is a subclass of a base `RecipeSource` that yields a normalized `Recipe` dataclass. Respect robots.txt, throttle requests, cache raw HTML in `scraper/cache/`.
- Runs manually / on cron (GitHub Actions). Not called from the app.

## Setup / CI

- `supabase/` follows the Supabase CLI convention (`supabase start` for local Postgres + auth); the Supabase Dashboard's GitHub integration auto-deploys migrations from `supabase/migrations/` on push to `main`
- SQL migrations checked in; RLS policies in a dedicated migration
- GitHub Actions: android build + lint, scraper ruff + pytest

## Verification

End-to-end checks before calling v1 done:

1. **Local backend:** `supabase start`, apply migrations, run `scraper/seed_cocktaildb.py`. Verify Postgres has ~600 cocktails and ~400 ingredients.
2. **Auth:** create account in the app, sign out, sign back in, confirm session persists.
3. **Pantry:** toggle 10 common ingredients, kill the app, reopen — pantry state is restored (both offline and after fresh install → confirms cloud sync).
4. **Matching RPC:** with a known pantry (vodka, lime juice, simple syrup, triple sec), calling `match_cocktails(0, {})` returns Kamikaze / similar; `match_cocktails(1, {})` adds Margarita (missing tequila). Verify via SQL and via the app UI.
5. **Favorites + notes:** favorite a cocktail, add a note, verify it round-trips.
6. **Filters:** apply spirit=gin + difficulty≤2, confirm result set matches a hand-verified SQL query.
7. **Perf:** cold start under 2s on a mid-range device; scroll the browse list at 60fps (Compose profiler).
8. **Manual smoke on a physical Android device** before pushing v1.

## Out of scope for v1 (parking lot)

- iOS / cross-platform (revisit with Kotlin Multiplatform if traction)
- Community ratings + reviews (needs moderation)
- Barcode-scan for bottles → pantry
- Shopping-list generator for "almost there" cocktails
- AI-suggested substitutions
- Social / share-recipe features
