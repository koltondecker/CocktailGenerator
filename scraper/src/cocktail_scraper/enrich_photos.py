"""Replace TheCocktailDB thumbnails with Wikipedia's originalimage source.

Classic cocktails (Manhattan, Margarita, Old Fashioned, Negroni, ...) have
professional-grade photos on Wikimedia Commons, freely licensed, no API key
required. The Wikipedia REST API surfaces the article's lead image as
`originalimage.source`.

Strategy per cocktail:
  1. Fetch the summary for the exact name (`/summary/Margarita`). If the
     article's description mentions "cocktail", use that article's photo.
  2. Otherwise fall back to `<name> (cocktail)` — Wikipedia's disambiguation
     convention for drinks whose plain-name article is something else
     (`Manhattan` is a borough; `Manhattan (cocktail)` is the drink).
  3. If neither exists, leave the existing image_url alone.
"""

from __future__ import annotations

import os
import sys
from urllib.parse import quote

import httpx
from dotenv import load_dotenv
from supabase import Client, create_client
from tenacity import retry, stop_after_attempt, wait_exponential

WIKI_API = "https://en.wikipedia.org/api/rest_v1/page/summary/"
USER_AGENT = (
    "CocktailGenerator/0.2 "
    "(https://github.com/koltondecker/CocktailGenerator) "
    "python-httpx"
)


def main() -> int:
    load_dotenv()
    url = os.environ["SUPABASE_URL"]
    key = os.environ["SUPABASE_SERVICE_ROLE_KEY"]
    client: Client = create_client(url, key)

    cocktails = (
        client.table("cocktails")
        .select("id,name,image_url")
        .execute()
        .data or []
    )
    print(f"Enriching {len(cocktails)} cocktails from Wikipedia...", file=sys.stderr)

    updated = skipped = missing = 0
    with httpx.Client(headers={"User-Agent": USER_AGENT}, timeout=15.0) as http:
        for i, cocktail in enumerate(cocktails, start=1):
            wiki_image = _try_wiki(http, cocktail["name"])
            if wiki_image is None:
                missing += 1
            elif wiki_image == cocktail.get("image_url"):
                skipped += 1
            else:
                (
                    client.table("cocktails")
                    .update({"image_url": wiki_image})
                    .eq("id", cocktail["id"])
                    .execute()
                )
                updated += 1
            if i % 25 == 0:
                print(f"  {i}/{len(cocktails)} processed ({updated} updated)", file=sys.stderr)

    print(
        f"Done. Updated {updated}, kept {skipped}, no Wikipedia article for {missing}."
    )
    return 0


def _try_wiki(http: httpx.Client, name: str) -> str | None:
    # Prefer the direct article if it's about a cocktail — avoids picking up
    # unrelated "Manhattan" or "Cosmopolitan" articles.
    direct = _fetch_summary(http, name)
    if direct is not None:
        description = (direct.get("description") or "").lower()
        if "cocktail" in description or "mixed drink" in description:
            image = _extract_image(direct)
            if image is not None:
                return image

    # Fall back to Wikipedia's `(cocktail)` disambiguation title.
    disambiguated = _fetch_summary(http, f"{name} (cocktail)")
    return _extract_image(disambiguated) if disambiguated else None


@retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, max=8))
def _fetch_summary(http: httpx.Client, title: str) -> dict | None:
    slug = quote(title.replace(" ", "_"), safe="")
    resp = http.get(WIKI_API + slug, follow_redirects=True)
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json()


def _extract_image(summary: dict) -> str | None:
    for key in ("originalimage", "thumbnail"):
        img = summary.get(key)
        if img and img.get("source"):
            return img["source"]
    return None


if __name__ == "__main__":
    raise SystemExit(main())
