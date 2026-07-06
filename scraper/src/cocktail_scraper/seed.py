"""Seed the Supabase catalog with cocktails + ingredients from TheCocktailDB.

Any ingredient TheCocktailDB reports that doesn't match a canonical name /
alias (or fuzzy-match it) is auto-created as a long-tail row in
`ingredients` (category = Other, is_common = false). That way no cocktail
loses ingredient rows just because our common-ingredient seed doesn't yet
carry the exotic stuff (Blue Curaçao, Falernum, etc.).
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

from dotenv import load_dotenv
from supabase import Client, create_client

from cocktail_scraper.models import Recipe
from cocktail_scraper.normalize import CanonicalIngredient, IngredientResolver
from cocktail_scraper.sources.thecocktaildb import TheCocktailDBSource

# CWD-relative — expects to be run from the `scraper/` directory (README + CI
# workflow both do so).
UNRESOLVED_LOG = Path("cache/unresolved_ingredients.csv")


def main() -> int:
    load_dotenv()
    url = os.environ["SUPABASE_URL"]
    key = os.environ["SUPABASE_SERVICE_ROLE_KEY"]
    client: Client = create_client(url, key)

    other_category_id = _load_other_category_id(client)
    catalog = _load_catalog(client)
    resolver = IngredientResolver(catalog=list(catalog), unresolved_log=UNRESOLVED_LOG)

    source = TheCocktailDBSource()
    ingested = 0
    for recipe in source.iter_recipes():
        _upsert_recipe(client, resolver, other_category_id, recipe)
        ingested += 1
        if ingested % 25 == 0:
            print(f"  ingested {ingested}...", file=sys.stderr)

    print(f"Done. Ingested {ingested} cocktails from TheCocktailDB.")
    return 0


def _load_catalog(client: Client) -> list[CanonicalIngredient]:
    rows = client.table("ingredients").select("id,name,aliases").execute().data or []
    return [
        CanonicalIngredient(id=row["id"], name=row["name"], aliases=tuple(row.get("aliases") or []))
        for row in rows
    ]


def _load_other_category_id(client: Client) -> int:
    """Look up the `Other` ingredient category so we can slot auto-created
    long-tail ingredients into it. Fails loud if the seed migration didn't run.
    """
    result = (
        client.table("ingredient_categories")
        .select("id")
        .eq("name", "Other")
        .limit(1)
        .execute()
    )
    if not result.data:
        raise RuntimeError(
            "Missing 'Other' ingredient category — did the "
            "20260704000004_seed_common_ingredients migration apply?"
        )
    return int(result.data[0]["id"])


def _upsert_recipe(
    client: Client,
    resolver: IngredientResolver,
    other_category_id: int,
    recipe: Recipe,
) -> None:
    payload = {
        "name": recipe.name,
        "slug": recipe.slug,
        "description": recipe.description,
        "glass": recipe.glass,
        "method": recipe.method,
        "garnish": recipe.garnish,
        "instructions": recipe.instructions,
        "difficulty": recipe.difficulty,
        "abv_estimate": str(recipe.abv_estimate) if recipe.abv_estimate is not None else None,
        "flavor_tags": recipe.flavor_tags,
        "source_url": recipe.source_url,
        "source_name": recipe.source_name,
        "image_url": recipe.image_url,
    }
    upserted = (
        client.table("cocktails")
        .upsert(payload, on_conflict="slug")
        .execute()
    )
    if not upserted.data:
        return
    cocktail_id = upserted.data[0]["id"]

    # Dedupe by canonical ingredient id — some TheCocktailDB recipes list the
    # same ingredient twice, and multiple raw names ("Cointreau", "Triple Sec")
    # can resolve to the same canonical row via aliases. Either case produces
    # duplicate `(cocktail_id, ingredient_id)` rows which Postgres refuses to
    # upsert in a single statement (SQLSTATE 21000). Keep the first occurrence
    # so we preserve the earliest `position`.
    seen_ingredient_ids: set[int] = set()
    rows: list[dict] = []
    for ing in recipe.ingredients:
        canonical_id = resolver.resolve(ing.raw_name)
        if canonical_id is None:
            canonical_id = _get_or_create_ingredient(
                client, resolver, other_category_id, ing.raw_name
            )
        if canonical_id is None:
            continue
        if canonical_id in seen_ingredient_ids:
            continue
        seen_ingredient_ids.add(canonical_id)
        rows.append({
            "cocktail_id": cocktail_id,
            "ingredient_id": canonical_id,
            "quantity": str(ing.quantity) if ing.quantity is not None else None,
            "unit": ing.unit,
            "is_optional": ing.is_optional,
            "position": ing.position,
        })
    if rows:
        client.table("cocktail_ingredients").upsert(
            rows, on_conflict="cocktail_id,ingredient_id"
        ).execute()


def _get_or_create_ingredient(
    client: Client,
    resolver: IngredientResolver,
    other_category_id: int,
    raw_name: str,
) -> int | None:
    name = raw_name.strip()
    if not name:
        return None
    canonical = name.title()
    inserted = (
        client.table("ingredients")
        .upsert(
            {"name": canonical, "category_id": other_category_id, "is_common": False},
            on_conflict="name",
        )
        .execute()
    )
    if not inserted.data:
        return None
    ingredient_id = int(inserted.data[0]["id"])
    resolver.add(CanonicalIngredient(id=ingredient_id, name=canonical))
    return ingredient_id


if __name__ == "__main__":
    raise SystemExit(main())
