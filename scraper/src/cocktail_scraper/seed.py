"""Seed the Supabase catalog with cocktails + ingredients from TheCocktailDB."""

from __future__ import annotations

import os
import sys
from pathlib import Path

from dotenv import load_dotenv
from supabase import Client, create_client

from cocktail_scraper.models import Recipe
from cocktail_scraper.normalize import CanonicalIngredient, IngredientResolver
from cocktail_scraper.sources.thecocktaildb import TheCocktailDBSource

UNRESOLVED_LOG = Path("scraper/cache/unresolved_ingredients.csv")


def main() -> int:
    load_dotenv()
    url = os.environ["SUPABASE_URL"]
    key = os.environ["SUPABASE_SERVICE_ROLE_KEY"]
    client: Client = create_client(url, key)

    catalog = _load_catalog(client)
    resolver = IngredientResolver(catalog=catalog, unresolved_log=UNRESOLVED_LOG)

    source = TheCocktailDBSource()
    ingested = 0
    for recipe in source.iter_recipes():
        _upsert_recipe(client, resolver, recipe)
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


def _upsert_recipe(client: Client, resolver: IngredientResolver, recipe: Recipe) -> None:
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

    rows: list[dict] = []
    for ing in recipe.ingredients:
        canonical_id = resolver.resolve(ing.raw_name)
        if canonical_id is None:
            continue
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


if __name__ == "__main__":
    raise SystemExit(main())
