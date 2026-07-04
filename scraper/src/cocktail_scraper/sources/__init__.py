from cocktail_scraper.sources.base import RecipeSource
from cocktail_scraper.sources.thecocktaildb import TheCocktailDBSource

REGISTRY: dict[str, type[RecipeSource]] = {
    "thecocktaildb": TheCocktailDBSource,
    # Register additional scrapers (diffords, liquor_com, punch, imbibe) here.
}

__all__ = ["RecipeSource", "REGISTRY"]
