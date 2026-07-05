-- Row-level security policies.
-- Catalog tables (ingredients, cocktails, etc.) are readable by anyone signed in.
-- Per-user tables allow read/write only to auth.uid() = user_id.

-- ------------------------------------------------------------------
-- Catalog: public read for authenticated users; writes reserved for service_role
-- ------------------------------------------------------------------

alter table public.ingredient_categories enable row level security;
alter table public.ingredients            enable row level security;
alter table public.cocktails              enable row level security;
alter table public.cocktail_ingredients   enable row level security;

create policy "catalog_read_categories" on public.ingredient_categories
    for select to authenticated using (true);

create policy "catalog_read_ingredients" on public.ingredients
    for select to authenticated using (true);

create policy "catalog_read_cocktails" on public.cocktails
    for select to authenticated using (true);

create policy "catalog_read_cocktail_ingredients" on public.cocktail_ingredients
    for select to authenticated using (true);

-- No insert/update/delete policies for authenticated → service_role bypasses RLS
-- and is the only writer (used by the scraper).

-- ------------------------------------------------------------------
-- Per-user tables
-- ------------------------------------------------------------------

alter table public.user_pantry    enable row level security;
alter table public.user_favorites enable row level security;
alter table public.user_notes     enable row level security;

create policy "pantry_owner_all" on public.user_pantry
    for all to authenticated
    using (user_id = auth.uid())
    with check (user_id = auth.uid());

create policy "favorites_owner_all" on public.user_favorites
    for all to authenticated
    using (user_id = auth.uid())
    with check (user_id = auth.uid());

create policy "notes_owner_all" on public.user_notes
    for all to authenticated
    using (user_id = auth.uid())
    with check (user_id = auth.uid());
