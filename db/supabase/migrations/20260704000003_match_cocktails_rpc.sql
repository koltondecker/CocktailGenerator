-- match_cocktails: returns cocktails the caller can make, allowing up to
-- `missing_allowed` non-optional ingredients to be absent from their pantry.
--
-- filters (all optional, jsonb):
--   { "base_spirit_ids": [1,2],       -- must include at least one of these ingredient ids
--     "flavor_tags":     ["sour"],    -- any-of match against cocktails.flavor_tags
--     "max_difficulty":  3,
--     "min_abv":         0,
--     "max_abv":         40 }
--
-- Result rows include the missing-ingredient list so the client can render
-- the "you need X" chip without a second query.

create or replace function public.match_cocktails(
    missing_allowed int default 0,
    filters         jsonb default '{}'::jsonb
)
returns table (
    id            bigint,
    name          text,
    slug          text,
    image_url     text,
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
        c.id, c.name, c.slug, c.image_url, c.difficulty, c.abv_estimate, c.flavor_tags,
        m.missing_count, m.missing_ingredients
    from public.cocktails c
    join missing m on m.cocktail_id = c.id
    where m.missing_count <= greatest(match_cocktails.missing_allowed, 0)
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
        (filters->'flavor_tags') is null
        or c.flavor_tags && (
            select array_agg(value) from jsonb_array_elements_text(filters->'flavor_tags')
        )
      )
      and (
        (filters->>'max_difficulty') is null
        or c.difficulty <= (filters->>'max_difficulty')::int
      )
      and (
        (filters->>'min_abv') is null
        or c.abv_estimate >= (filters->>'min_abv')::numeric
      )
      and (
        (filters->>'max_abv') is null
        or c.abv_estimate <= (filters->>'max_abv')::numeric
      )
    order by m.missing_count asc, c.name asc;
$$;

grant execute on function public.match_cocktails(int, jsonb) to authenticated;
