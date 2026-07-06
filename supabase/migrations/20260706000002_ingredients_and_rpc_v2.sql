-- Expand the common-ingredient seed (esp. spirits/liqueurs) and rebuild
-- match_cocktails to (a) return `method` and (b) accept a `methods` filter.
-- Removes the broken `max_difficulty` / `min_abv` / `max_abv` filters — the
-- scraper never populates those columns, so setting them always returned
-- zero rows.

-- ------------------------------------------------------------------
-- 1. Add ~55 more common ingredients (upserts; safe to re-run).
-- ------------------------------------------------------------------

with cat as (select name, id from public.ingredient_categories)
insert into public.ingredients (name, category_id, is_common, aliases) values
    -- Extra spirits
    ('Aged Rum',            (select id from cat where name = 'Spirits'), true, array['añejo rum']),
    ('Spiced Rum',          (select id from cat where name = 'Spirits'), true, '{}'),
    ('Reposado Tequila',    (select id from cat where name = 'Spirits'), true, '{}'),
    ('Añejo Tequila',       (select id from cat where name = 'Spirits'), true, '{}'),
    ('Irish Whiskey',       (select id from cat where name = 'Spirits'), true, '{}'),
    ('Japanese Whisky',     (select id from cat where name = 'Spirits'), true, '{}'),
    ('Absinthe',            (select id from cat where name = 'Spirits'), true, '{}'),
    ('Cachaça',             (select id from cat where name = 'Spirits'), true, array['cachaca']),
    ('Pisco',               (select id from cat where name = 'Spirits'), true, '{}'),

    -- Extra liqueurs
    ('Cointreau',           (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Grand Marnier',       (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Green Chartreuse',    (select id from cat where name = 'Liqueurs'), true, array['chartreuse verte']),
    ('Yellow Chartreuse',   (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Bénédictine',         (select id from cat where name = 'Liqueurs'), true, array['benedictine']),
    ('Drambuie',            (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Chambord',            (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Baileys Irish Cream', (select id from cat where name = 'Liqueurs'), true, array['bailey''s irish cream','baileys']),
    ('Sambuca',             (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Frangelico',          (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Galliano',            (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Fernet-Branca',       (select id from cat where name = 'Liqueurs'), true, array['fernet']),
    ('Cynar',               (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Amaro Nonino',        (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Amaro Averna',        (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Crème de Cacao (White)', (select id from cat where name = 'Liqueurs'), true, array['white crème de cacao','white creme de cacao','crema di cacao white']),
    ('Crème de Cacao (Dark)',  (select id from cat where name = 'Liqueurs'), true, array['dark crème de cacao','dark creme de cacao']),
    ('Crème de Menthe',     (select id from cat where name = 'Liqueurs'), true, array['creme de menthe','green crème de menthe']),
    ('Crème de Violette',   (select id from cat where name = 'Liqueurs'), true, array['creme de violette']),
    ('Sloe Gin',            (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Peach Schnapps',      (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Blue Curaçao',        (select id from cat where name = 'Liqueurs'), true, array['blue curacao']),
    ('Malibu Coconut Rum',  (select id from cat where name = 'Liqueurs'), true, array['malibu','coconut rum']),
    ('Midori',              (select id from cat where name = 'Liqueurs'), true, array['melon liqueur']),
    ('Suze',                (select id from cat where name = 'Liqueurs'), true, '{}'),

    -- Extra wine & fortified
    ('Lillet Blanc',        (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Dubonnet',            (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Fino Sherry',         (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Amontillado Sherry',  (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Ruby Port',           (select id from cat where name = 'Wine & Fortified'), true, '{}'),

    -- Fresh
    ('Basil',               (select id from cat where name = 'Fresh'), true, '{}'),
    ('Rosemary',            (select id from cat where name = 'Fresh'), true, '{}'),
    ('Strawberry',          (select id from cat where name = 'Fresh'), true, '{}'),
    ('Raspberry',           (select id from cat where name = 'Fresh'), true, '{}'),
    ('Blackberry',          (select id from cat where name = 'Fresh'), true, '{}'),
    ('Blueberry',           (select id from cat where name = 'Fresh'), true, '{}'),
    ('Jalapeño',            (select id from cat where name = 'Fresh'), true, array['jalapeno']),

    -- Syrups
    ('Falernum',            (select id from cat where name = 'Syrups'), true, '{}'),
    ('Demerara Syrup',      (select id from cat where name = 'Syrups'), true, '{}'),
    ('Cinnamon Syrup',      (select id from cat where name = 'Syrups'), true, '{}'),
    ('Ginger Syrup',        (select id from cat where name = 'Syrups'), true, '{}'),
    ('Vanilla Syrup',       (select id from cat where name = 'Syrups'), true, '{}'),

    -- Garnishes
    ('Maraschino Cherry',   (select id from cat where name = 'Garnishes'), true, '{}'),
    ('Olive',               (select id from cat where name = 'Garnishes'), true, '{}'),
    ('Cocktail Onion',      (select id from cat where name = 'Garnishes'), true, '{}'),
    ('Cinnamon Stick',      (select id from cat where name = 'Garnishes'), true, '{}'),
    ('Star Anise',          (select id from cat where name = 'Garnishes'), true, '{}'),

    -- Other
    ('Cream',               (select id from cat where name = 'Other'), true, '{}'),
    ('Espresso',            (select id from cat where name = 'Other'), true, '{}'),
    ('Coffee',              (select id from cat where name = 'Other'), true, '{}')
on conflict (name) do update
    set category_id = excluded.category_id,
        is_common   = true,
        aliases     = excluded.aliases;

-- ------------------------------------------------------------------
-- 2. Populate Twemoji icons for the new ingredients.
-- ------------------------------------------------------------------

with mapping(name, code) as (values
    -- Spirits (tumbler)
    ('Aged Rum','1f943'),('Spiced Rum','1f943'),('Reposado Tequila','1f943'),
    ('Añejo Tequila','1f943'),('Irish Whiskey','1f943'),('Japanese Whisky','1f943'),
    ('Absinthe','1f9ea'),          -- test tube (green)
    ('Cachaça','1f943'),('Pisco','1f943'),

    -- Liqueurs (cocktail glass by default, variants for cream/coffee/blue)
    ('Cointreau','1f378'),('Grand Marnier','1f378'),
    ('Green Chartreuse','1f378'),('Yellow Chartreuse','1f378'),
    ('Bénédictine','1f378'),('Drambuie','1f378'),('Chambord','1f378'),
    ('Baileys Irish Cream','1f95b'),  -- milk glass
    ('Sambuca','1f378'),
    ('Frangelico','2615'),            -- coffee (hazelnut)
    ('Galliano','1f378'),
    ('Fernet-Branca','1f378'),('Cynar','1f378'),
    ('Amaro Nonino','1f378'),('Amaro Averna','1f378'),
    ('Crème de Cacao (White)','1f36b'), -- chocolate bar
    ('Crème de Cacao (Dark)','1f36b'),
    ('Crème de Menthe','1f33f'),      -- herb (mint)
    ('Crème de Violette','1f337'),    -- tulip
    ('Sloe Gin','1f347'),             -- grapes (dark berry)
    ('Peach Schnapps','1f351'),       -- peach
    ('Blue Curaçao','1f30a'),         -- wave (blue)
    ('Malibu Coconut Rum','1f965'),   -- coconut
    ('Midori','1f348'),               -- melon
    ('Suze','1f378'),

    -- Wine & fortified
    ('Lillet Blanc','1f377'),('Dubonnet','1f377'),
    ('Fino Sherry','1f377'),('Amontillado Sherry','1f377'),
    ('Ruby Port','1f377'),

    -- Fresh
    ('Basil','1f33f'),('Rosemary','1f33f'),
    ('Strawberry','1f353'),
    ('Raspberry','1fad0'),   -- blueberry glyph, closest berry
    ('Blackberry','1fad0'),
    ('Blueberry','1fad0'),
    ('Jalapeño','1f336'),    -- hot pepper

    -- Syrups
    ('Falernum','1f36f'),
    ('Demerara Syrup','1f36f'),
    ('Cinnamon Syrup','1f36f'),
    ('Ginger Syrup','1f331'),
    ('Vanilla Syrup','1f36f'),

    -- Garnishes
    ('Maraschino Cherry','1f352'),
    ('Olive','1fad2'),
    ('Cocktail Onion','1f9c5'),
    ('Cinnamon Stick','1f330'),
    ('Star Anise','2b50'),

    -- Other
    ('Cream','1f95b'),
    ('Espresso','2615'),
    ('Coffee','2615')
)
update public.ingredients i
set icon_url = 'https://cdn.jsdelivr.net/gh/jdecked/twemoji@latest/assets/72x72/' || m.code || '.png'
from mapping m
where i.name = m.name;

-- ------------------------------------------------------------------
-- 3. Rebuild match_cocktails: return `method`, drop the broken filters,
--    accept a real `methods` filter, keep base_spirit_ids.
-- ------------------------------------------------------------------

drop function if exists public.match_cocktails(int, jsonb);

create or replace function public.match_cocktails(
    missing_allowed int default 0,
    filters         jsonb default '{}'::jsonb
)
returns table (
    id            bigint,
    name          text,
    slug          text,
    image_url     text,
    method        text,
    difficulty    smallint,
    abv_estimate  numeric,
    flavor_tags   text[],
    missing_count int,
    missing_ingredients jsonb
)
language sql
stable
security invoker
set search_path = public
as $$
    with pantry as (
        select ingredient_id from public.user_pantry where user_id = auth.uid()
    ),
    required as (
        select ci.cocktail_id, ci.ingredient_id, i.name as ingredient_name
        from public.cocktail_ingredients ci
        join public.ingredients i on i.id = ci.ingredient_id
        where ci.is_optional = false
    ),
    missing as (
        select
            r.cocktail_id,
            count(*) filter (where p.ingredient_id is null)::int as missing_count,
            coalesce(
                jsonb_agg(
                    jsonb_build_object('id', r.ingredient_id, 'name', r.ingredient_name)
                    order by r.ingredient_name
                ) filter (where p.ingredient_id is null),
                '[]'::jsonb
            ) as missing_ingredients
        from required r
        left join pantry p on p.ingredient_id = r.ingredient_id
        group by r.cocktail_id
    )
    select
        c.id, c.name, c.slug, c.image_url, c.method,
        c.difficulty, c.abv_estimate, c.flavor_tags,
        coalesce(m.missing_count, 0) as missing_count,
        coalesce(m.missing_ingredients, '[]'::jsonb) as missing_ingredients
    -- LEFT JOIN: cocktails without any required ingredients (or with only
    -- optional ones) still surface with missing_count=0 rather than being
    -- silently dropped.
    from public.cocktails c
    left join missing m on m.cocktail_id = c.id
    where coalesce(m.missing_count, 0) <= greatest(match_cocktails.missing_allowed, 0)
      and (
        (filters->'base_spirit_ids') is null
        or exists (
            select 1
            from public.cocktail_ingredients ci
            where ci.cocktail_id = c.id
              and ci.ingredient_id = any (
                select (jsonb_array_elements_text(filters->'base_spirit_ids'))::bigint
              )
        )
      )
      and (
        (filters->'methods') is null
        or c.method = any (
            select jsonb_array_elements_text(filters->'methods')
        )
      )
    order by coalesce(m.missing_count, 0) asc, c.name asc;
$$;

grant execute on function public.match_cocktails(int, jsonb) to authenticated;
