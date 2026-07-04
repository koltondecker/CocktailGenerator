"""CLI entrypoint: run a single named source."""

from __future__ import annotations

import typer
from dotenv import load_dotenv

from cocktail_scraper.sources import REGISTRY

app = typer.Typer(help="Run a cocktail scraper source.")


@app.command()
def main(
    source: str = typer.Option(..., help=f"One of: {', '.join(REGISTRY)}"),
    limit: int = typer.Option(0, help="Stop after N recipes; 0 = unlimited."),
) -> None:
    load_dotenv()
    if source not in REGISTRY:
        raise typer.BadParameter(f"Unknown source '{source}'. Known: {list(REGISTRY)}")
    src_cls = REGISTRY[source]
    src = src_cls()
    count = 0
    for recipe in src.iter_recipes():
        typer.echo(f"{recipe.slug}\t{recipe.name}\t{len(recipe.ingredients)} ing.")
        count += 1
        if limit and count >= limit:
            break
    typer.echo(f"# {count} recipes")


if __name__ == "__main__":
    app()
