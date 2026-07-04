-- Initial schema for cocktail-generator.
-- Canonical ingredient catalog + normalized cocktail recipes + per-user state.

create extension if not exists "pgcrypto";
create extension if not exists "pg_trgm";

-- ------------------------------------------------------------------
-- Catalog
-- ------------------------------------------------------------------

create table public.ingredient_categories (
    id          smallserial primary key,
    name        text not null unique,
    sort_order  smallint not null default 0
);

create table public.ingredients (
    id          bigserial primary key,
    name        text not null unique,
    category_id smallint not null references public.ingredient_categories(id) on delete restrict,
    aliases     text[] not null default '{}',
    is_common   boolean not null default false,
    created_at  timestamptz not null default now()
);

create index ingredients_category_idx on public.ingredients(category_id);
create index ingredients_is_common_idx on public.ingredients(is_common) where is_common;
create index ingredients_name_trgm_idx on public.ingredients using gin (name gin_trgm_ops);

create table public.cocktails (
    id             bigserial primary key,
    name           text not null,
    slug           text not null unique,
    description    text,
    glass          text,
    method         text,
    garnish        text,
    instructions   text,
    difficulty     smallint check (difficulty between 1 and 5),
    abv_estimate   numeric(4,2),
    flavor_tags    text[] not null default '{}',
    source_url     text,
    source_name    text,
    image_url      text,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

create index cocktails_flavor_tags_idx on public.cocktails using gin (flavor_tags);
create index cocktails_name_trgm_idx on public.cocktails using gin (name gin_trgm_ops);

create table public.cocktail_ingredients (
    cocktail_id   bigint not null references public.cocktails(id) on delete cascade,
    ingredient_id bigint not null references public.ingredients(id) on delete restrict,
    quantity      numeric(6,2),
    unit          text,
    is_optional   boolean not null default false,
    position      smallint not null default 0,
    primary key (cocktail_id, ingredient_id)
);

create index cocktail_ingredients_ingredient_idx on public.cocktail_ingredients(ingredient_id);

-- ------------------------------------------------------------------
-- Per-user state (RLS enforced in a later migration)
-- ------------------------------------------------------------------

create table public.user_pantry (
    user_id       uuid not null references auth.users(id) on delete cascade,
    ingredient_id bigint not null references public.ingredients(id) on delete cascade,
    added_at      timestamptz not null default now(),
    primary key (user_id, ingredient_id)
);

create index user_pantry_ingredient_idx on public.user_pantry(ingredient_id);

create table public.user_favorites (
    user_id     uuid not null references auth.users(id) on delete cascade,
    cocktail_id bigint not null references public.cocktails(id) on delete cascade,
    added_at    timestamptz not null default now(),
    primary key (user_id, cocktail_id)
);

create table public.user_notes (
    user_id         uuid not null references auth.users(id) on delete cascade,
    cocktail_id     bigint not null references public.cocktails(id) on delete cascade,
    body            text,
    personal_rating smallint check (personal_rating between 1 and 5),
    updated_at      timestamptz not null default now(),
    primary key (user_id, cocktail_id)
);

-- Keep updated_at fresh
create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin
    new.updated_at = now();
    return new;
end $$;

create trigger cocktails_touch before update on public.cocktails
    for each row execute function public.touch_updated_at();

create trigger user_notes_touch before update on public.user_notes
    for each row execute function public.touch_updated_at();
