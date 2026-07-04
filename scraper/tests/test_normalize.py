from cocktail_scraper.normalize import CanonicalIngredient, IngredientResolver


def _catalog() -> list[CanonicalIngredient]:
    return [
        CanonicalIngredient(id=1, name="Blanco Tequila", aliases=("silver tequila", "tequila")),
        CanonicalIngredient(id=2, name="Lime Juice", aliases=("fresh lime juice",)),
        CanonicalIngredient(id=3, name="Simple Syrup"),
        CanonicalIngredient(id=4, name="Triple Sec", aliases=("cointreau", "curacao")),
    ]


def test_exact_name_match_is_case_insensitive() -> None:
    r = IngredientResolver(_catalog())
    assert r.resolve("blanco tequila") == 1
    assert r.resolve("Simple Syrup") == 3


def test_alias_match() -> None:
    r = IngredientResolver(_catalog())
    assert r.resolve("tequila") == 1
    assert r.resolve("fresh lime juice") == 2
    assert r.resolve("Cointreau") == 4


def test_fuzzy_match_within_threshold() -> None:
    r = IngredientResolver(_catalog(), threshold=80)
    assert r.resolve("simple sirup") == 3


def test_unknown_returns_none() -> None:
    r = IngredientResolver(_catalog(), threshold=95)
    assert r.resolve("elderflower cordial") is None
