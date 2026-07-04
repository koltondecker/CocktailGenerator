"""Resolve free-text ingredient names to canonical `ingredients.id`."""

from __future__ import annotations

import csv
import os
from dataclasses import dataclass
from pathlib import Path

from rapidfuzz import fuzz, process


@dataclass(frozen=True)
class CanonicalIngredient:
    id: int
    name: str
    aliases: tuple[str, ...] = ()

    def all_terms(self) -> tuple[str, ...]:
        return (self.name, *self.aliases)


class IngredientResolver:
    """Map raw ingredient strings to canonical ingredient IDs.

    Resolution order per raw name:
      1. Exact case-insensitive match on canonical name
      2. Exact case-insensitive match on any alias
      3. Fuzzy match via RapidFuzz (token_set_ratio) against all terms
    """

    def __init__(
        self,
        catalog: list[CanonicalIngredient],
        threshold: int | None = None,
        unresolved_log: Path | None = None,
    ) -> None:
        self._catalog = catalog
        self._threshold = threshold or int(os.getenv("INGREDIENT_MATCH_THRESHOLD", "88"))
        self._unresolved_log = unresolved_log

        self._by_term: dict[str, int] = {}
        self._all_terms: list[str] = []
        self._term_to_id: list[int] = []
        for ing in catalog:
            for term in ing.all_terms():
                key = term.lower().strip()
                self._by_term.setdefault(key, ing.id)
                self._all_terms.append(term)
                self._term_to_id.append(ing.id)

    def resolve(self, raw_name: str) -> int | None:
        key = raw_name.lower().strip()
        if not key:
            return None
        if key in self._by_term:
            return self._by_term[key]

        result = process.extractOne(
            raw_name,
            self._all_terms,
            scorer=fuzz.token_set_ratio,
            score_cutoff=self._threshold,
        )
        if result is None:
            self._log_unresolved(raw_name)
            return None
        _, _, idx = result
        return self._term_to_id[idx]

    def _log_unresolved(self, raw_name: str) -> None:
        if self._unresolved_log is None:
            return
        self._unresolved_log.parent.mkdir(parents=True, exist_ok=True)
        write_header = not self._unresolved_log.exists()
        with self._unresolved_log.open("a", newline="") as f:
            writer = csv.writer(f)
            if write_header:
                writer.writerow(["raw_name"])
            writer.writerow([raw_name])
