"""Normalized data model that every source converges on before upsert."""

from __future__ import annotations

from decimal import Decimal
from typing import Literal

from pydantic import BaseModel, Field


class RawIngredient(BaseModel):
    """A source-reported ingredient — the raw string plus any parsed quantity."""

    raw_name: str
    quantity: Decimal | None = None
    unit: str | None = None
    is_optional: bool = False
    position: int = 0


class Recipe(BaseModel):
    """One cocktail as reported by a source, prior to catalog normalization."""

    name: str
    slug: str
    description: str | None = None
    glass: str | None = None
    method: Literal["shaken", "stirred", "built", "blended", "muddled"] | None = None
    garnish: str | None = None
    instructions: str | None = None
    difficulty: int | None = Field(default=None, ge=1, le=5)
    abv_estimate: Decimal | None = None
    flavor_tags: list[str] = Field(default_factory=list)
    source_url: str | None = None
    source_name: str
    image_url: str | None = None
    ingredients: list[RawIngredient]
