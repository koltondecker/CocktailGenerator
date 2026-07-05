# Database (Supabase / Postgres)

Schema, RLS policies, and the `match_cocktails` RPC that powers "what can I make?".

## Layout

Standard [Supabase CLI](https://supabase.com/docs/guides/cli) layout — this
directory is what the Supabase Dashboard's GitHub integration expects.

```
supabase/
├── config.toml                # local dev config
└── migrations/
    ├── 20260704000001_initial_schema.sql
    ├── 20260704000002_rls_policies.sql
    ├── 20260704000003_match_cocktails_rpc.sql
    └── 20260704000004_seed_common_ingredients.sql
```

Migrations apply in filename order. `20260704000004_seed_common_ingredients.sql`
is idempotent (`on conflict do update`) so it's safe to re-run.

## Local dev

```bash
supabase init      # only once
supabase start     # local Postgres + Auth + Studio at http://localhost:54323
supabase db reset  # applies everything under supabase/migrations in order
supabase status    # shows the local anon key + URL for android/local.properties
```

## Deploying to a hosted project

Two options:

1. **GitHub integration** (recommended). In the Supabase Dashboard →
   Project → Integrations → GitHub, connect this repo. New migrations
   under `supabase/migrations/` deploy on push to `main`.
2. **CLI push**. `supabase link --project-ref <ref>` once, then
   `supabase db push` when migrations change.

## Schema highlights

- `ingredient_categories`, `ingredients` — canonical catalog.
  `ingredients.is_common` drives the pantry checklist.
- `cocktails` + `cocktail_ingredients` — normalized recipes.
- `user_pantry`, `user_favorites`, `user_notes` — per-user state, RLS-guarded.
- `match_cocktails(missing_allowed int, filters jsonb)` — returns cocktails
  whose non-optional ingredients missing from the caller's pantry are
  `<= missing_allowed`, ordered by fewest missing.

The cocktail corpus itself is loaded by the Python scraper — see
[`../scraper/README.md`](../scraper/README.md).
