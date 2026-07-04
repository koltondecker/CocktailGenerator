# Database (Supabase / Postgres)

Schema, RLS policies, and the `match_cocktails` RPC that powers "what can I make?".

## Local dev

```bash
cd db
supabase init             # only once
supabase start            # local Postgres + Auth + Studio at http://localhost:54323
supabase db reset         # applies everything under supabase/migrations
```

Migrations live under `supabase/migrations/` (Supabase CLI convention). Filenames are timestamp-prefixed so they apply in order.

## Layout

```
db/
├── supabase/
│   ├── config.toml
│   └── migrations/
│       ├── 20260704000001_initial_schema.sql
│       ├── 20260704000002_rls_policies.sql
│       └── 20260704000003_match_cocktails_rpc.sql
└── seed/
    └── categories_and_common_ingredients.sql
```

## Schema highlights

- `ingredient_categories`, `ingredients` — canonical catalog. `ingredients.is_common` drives the pantry checklist.
- `cocktails` + `cocktail_ingredients` — normalized recipes.
- `user_pantry`, `user_favorites`, `user_notes` — per-user state, RLS-guarded.
- `match_cocktails(missing_allowed int, filters jsonb)` — returns cocktails whose non-optional ingredients missing from the caller's pantry are `<= missing_allowed`, ordered by fewest missing.

## Applying the seed data

```bash
psql "$SUPABASE_LOCAL_URL" -f seed/categories_and_common_ingredients.sql
```

The cocktail corpus itself is loaded by the Python scraper — see `../scraper/README.md`.
