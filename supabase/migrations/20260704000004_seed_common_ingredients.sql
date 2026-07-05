-- Seed the ingredient category taxonomy and the "common at-home" ingredient list
-- that powers the initial pantry checklist. Safe to re-run.

insert into public.ingredient_categories (name, sort_order) values
    ('Spirits',    10),
    ('Liqueurs',   20),
    ('Wine & Fortified', 30),
    ('Bitters',    40),
    ('Mixers',     50),
    ('Juices',     60),
    ('Fresh',      70),
    ('Syrups',     80),
    ('Garnishes',  90),
    ('Other',      100)
on conflict (name) do update set sort_order = excluded.sort_order;

-- Common at-home ingredients. Marked is_common=true so they appear in the
-- pantry checklist by default. The scraper will insert other canonical
-- ingredients as it ingests recipes.
with cat as (
    select name, id from public.ingredient_categories
)
insert into public.ingredients (name, category_id, is_common, aliases) values
    -- Spirits
    ('Vodka',             (select id from cat where name = 'Spirits'), true, '{}'),
    ('Gin',               (select id from cat where name = 'Spirits'), true, '{}'),
    ('White Rum',         (select id from cat where name = 'Spirits'), true, array['light rum']),
    ('Dark Rum',          (select id from cat where name = 'Spirits'), true, '{}'),
    ('Blanco Tequila',    (select id from cat where name = 'Spirits'), true, array['silver tequila','tequila']),
    ('Bourbon',           (select id from cat where name = 'Spirits'), true, '{}'),
    ('Rye Whiskey',       (select id from cat where name = 'Spirits'), true, array['rye']),
    ('Scotch',            (select id from cat where name = 'Spirits'), true, '{}'),
    ('Brandy',            (select id from cat where name = 'Spirits'), true, '{}'),
    ('Mezcal',            (select id from cat where name = 'Spirits'), true, '{}'),

    -- Liqueurs
    ('Triple Sec',        (select id from cat where name = 'Liqueurs'), true, array['cointreau','curacao']),
    ('Campari',           (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Aperol',            (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Amaretto',          (select id from cat where name = 'Liqueurs'), true, '{}'),
    ('Coffee Liqueur',    (select id from cat where name = 'Liqueurs'), true, array['kahlua']),
    ('Elderflower Liqueur', (select id from cat where name = 'Liqueurs'), false, array['st germain']),
    ('Maraschino Liqueur',(select id from cat where name = 'Liqueurs'), false, '{}'),

    -- Wine & Fortified
    ('Sweet Vermouth',    (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Dry Vermouth',      (select id from cat where name = 'Wine & Fortified'), true, '{}'),
    ('Prosecco',          (select id from cat where name = 'Wine & Fortified'), false, array['sparkling wine']),
    ('Champagne',         (select id from cat where name = 'Wine & Fortified'), false, '{}'),

    -- Bitters
    ('Angostura Bitters', (select id from cat where name = 'Bitters'), true, '{}'),
    ('Orange Bitters',    (select id from cat where name = 'Bitters'), true, '{}'),
    ('Peychauds Bitters', (select id from cat where name = 'Bitters'), false, '{}'),

    -- Mixers
    ('Soda Water',        (select id from cat where name = 'Mixers'), true, array['club soda','sparkling water']),
    ('Tonic Water',       (select id from cat where name = 'Mixers'), true, '{}'),
    ('Ginger Beer',       (select id from cat where name = 'Mixers'), true, '{}'),
    ('Cola',              (select id from cat where name = 'Mixers'), true, '{}'),
    ('Ginger Ale',        (select id from cat where name = 'Mixers'), false, '{}'),

    -- Juices
    ('Lime Juice',        (select id from cat where name = 'Juices'), true, array['fresh lime juice']),
    ('Lemon Juice',       (select id from cat where name = 'Juices'), true, array['fresh lemon juice']),
    ('Orange Juice',      (select id from cat where name = 'Juices'), true, '{}'),
    ('Cranberry Juice',   (select id from cat where name = 'Juices'), true, '{}'),
    ('Pineapple Juice',   (select id from cat where name = 'Juices'), true, '{}'),
    ('Grapefruit Juice',  (select id from cat where name = 'Juices'), false, '{}'),
    ('Tomato Juice',      (select id from cat where name = 'Juices'), false, '{}'),

    -- Fresh
    ('Lime',              (select id from cat where name = 'Fresh'), true, '{}'),
    ('Lemon',             (select id from cat where name = 'Fresh'), true, '{}'),
    ('Orange',            (select id from cat where name = 'Fresh'), true, '{}'),
    ('Mint',              (select id from cat where name = 'Fresh'), true, array['fresh mint']),
    ('Ginger',            (select id from cat where name = 'Fresh'), false, '{}'),
    ('Cucumber',          (select id from cat where name = 'Fresh'), false, '{}'),
    ('Egg White',         (select id from cat where name = 'Fresh'), false, '{}'),

    -- Syrups
    ('Simple Syrup',      (select id from cat where name = 'Syrups'), true, '{}'),
    ('Grenadine',         (select id from cat where name = 'Syrups'), true, '{}'),
    ('Orgeat',            (select id from cat where name = 'Syrups'), false, '{}'),
    ('Honey Syrup',       (select id from cat where name = 'Syrups'), false, '{}'),
    ('Agave Syrup',       (select id from cat where name = 'Syrups'), false, array['agave nectar']),

    -- Garnishes / Other
    ('Sugar',             (select id from cat where name = 'Other'), true, '{}'),
    ('Salt',              (select id from cat where name = 'Other'), true, array['kosher salt']),
    ('Ice',               (select id from cat where name = 'Other'), true, '{}')
on conflict (name) do update
    set category_id = excluded.category_id,
        aliases     = excluded.aliases,
        is_common   = excluded.is_common;
