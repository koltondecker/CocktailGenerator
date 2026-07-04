from __future__ import annotations

from abc import ABC, abstractmethod
from collections.abc import Iterator

from cocktail_scraper.models import Recipe


class RecipeSource(ABC):
    """Common contract every scraper implements."""

    name: str = ""

    @abstractmethod
    def iter_recipes(self) -> Iterator[Recipe]:
        """Yield recipes one at a time. Sources should respect robots.txt and
        throttle their requests; the caller does not.
        """
