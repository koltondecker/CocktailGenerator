"""TheCocktailDB seed source.

Uses the public free-tier API (test key `1`). For production seeding, register
for a Patreon-tier key and set THECOCKTAILDB_KEY.
"""

from __future__ import annotations

import os
import string
from collections.abc import Iterator
from decimal import Decimal, InvalidOperation

import httpx
from slugify import slugify
from tenacity import retry, stop_after_attempt, wait_exponential

from cocktail_scraper.models import RawIngredient, Recipe
from cocktail_scraper.sources.base import RecipeSource

BASE = "https://www.thecocktaildb.com/api/json/v1"


class TheCocktailDBSource(RecipeSource):
    name = "thecocktaildb"

    def __init__(self, timeout: float = 10.0) -> None:
        key = os.getenv("THECOCKTAILDB_KEY", "1")
        self._base = f"{BASE}/{key}"
        self._client = httpx.Client(timeout=timeout, headers={"User-Agent": "cocktail-generator/0.1"})

    def iter_recipes(self) -> Iterator[Recipe]:
        try:
            seen: set[str] = set()
            for letter in string.ascii_lowercase:
                drinks = self._search_by_letter(letter)
                for drink in drinks:
                    idx = drink.get("idDrink")
                    if not idx or idx in seen:
                        continue
                    seen.add(idx)
                    recipe = self._to_recipe(drink)
                    if recipe is not None:
                        yield recipe
        finally:
            self._client.close()

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, max=8))
    def _search_by_letter(self, letter: str) -> list[dict]:
        r = self._client.get(f"{self._base}/search.php", params={"f": letter})
        r.raise_for_status()
        return r.json().get("drinks") or []

    @staticmethod
    def _to_recipe(drink: dict) -> Recipe | None:
        name = drink.get("strDrink")
        if not name:
            return None

        ingredients: list[RawIngredient] = []
        for i in range(1, 16):
            raw = drink.get(f"strIngredient{i}")
            if not raw or not raw.strip():
                continue
            measure = (drink.get(f"strMeasure{i}") or "").strip()
            qty, unit = _parse_measure(measure)
            ingredients.append(
                RawIngredient(
                    raw_name=raw.strip(),
                    quantity=qty,
                    unit=unit,
                    position=i,
                )
            )

        if not ingredients:
            return None

        method = _infer_method(drink.get("strInstructions") or "")

        return Recipe(
            name=name,
            slug=slugify(name),
            description=None,
            glass=(drink.get("strGlass") or None),
            method=method,
            garnish=None,
            instructions=drink.get("strInstructions") or None,
            source_url=None,
            source_name="TheCocktailDB",
            image_url=drink.get("strDrinkThumb") or None,
            ingredients=ingredients,
        )


def _parse_measure(measure: str) -> tuple[Decimal | None, str | None]:
    """Best-effort split of TheCocktailDB's mixed-unit measurement strings."""
    if not measure:
        return None, None
    tokens = measure.split()
    qty: Decimal | None = None
    unit_parts: list[str] = []
    for tok in tokens:
        parsed = _to_decimal(tok)
        if parsed is not None and qty is None:
            qty = parsed
        else:
            unit_parts.append(tok)
    unit = " ".join(unit_parts).strip() or None
    return qty, unit


def _to_decimal(tok: str) -> Decimal | None:
    tok = tok.strip()
    if "/" in tok:
        try:
            num, den = tok.split("/", 1)
            return Decimal(num) / Decimal(den)
        except (InvalidOperation, ZeroDivisionError):
            return None
    try:
        return Decimal(tok)
    except InvalidOperation:
        return None


def _infer_method(instructions: str) -> str | None:
    text = instructions.lower()
    if "shake" in text:
        return "shaken"
    if "stir" in text:
        return "stirred"
    if "blend" in text:
        return "blended"
    if "muddle" in text:
        return "muddled"
    if instructions:
        return "built"
    return None
