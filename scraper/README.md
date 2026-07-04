# Cocktail Scraper

Python data pipeline that seeds the Supabase catalog from public sources.

## Stack

- Python 3.12, [uv](https://docs.astral.sh/uv/) for env + deps
- `httpx` for HTTP, `selectolax` for HTML parsing, `rapidfuzz` for ingredient dedup
- `supabase-py` for upserts

## Setup

```bash
cd scraper
uv sync
cp .env.example .env    # fill in SUPABASE_URL + SUPABASE_SERVICE_ROLE_KEY
```

The service-role key bypasses RLS — required for the scraper to write to catalog tables. **Never ship the service-role key inside the Android app.**

## Run

```bash
# Seed from TheCocktailDB (~600 cocktails, ~500 ingredients).
uv run python -m cocktail_scraper.seed

# Expand from a specific source.
uv run python -m cocktail_scraper.run --source thecocktaildb
uv run python -m cocktail_scraper.run --source diffords    # not implemented yet
```

## Adding a new source

1. Create `src/cocktail_scraper/sources/<name>.py` with a class that subclasses `RecipeSource`.
2. Implement `iter_recipes() -> Iterator[Recipe]`.
3. Register it in `sources/__init__.py`.
4. Respect `robots.txt`, throttle, and cache raw responses in `cache/`.

## Ingredient normalization

Recipes use free-text ingredient names ("fresh lime juice", "juice of one lime").
`normalize.py` maps each raw name to a canonical `ingredients.id` via:

1. Exact case-insensitive match on `name`
2. Exact match on any element of `aliases`
3. Fuzzy match (RapidFuzz) with threshold 88 — matches below the threshold go to `unresolved.csv` for manual review
