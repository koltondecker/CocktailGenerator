-- Add per-ingredient icons: small flat transparent PNGs served from
-- jsdelivr's Twemoji CDN. Consistent style across the pantry.

alter table public.ingredients add column if not exists icon_url text;

-- Twemoji codepoint → CDN URL. jsdelivr caches jdecked's actively-maintained
-- fork of Twitter's open-source emoji set (CC-BY 4.0, no attribution required
-- at runtime for typical use).
with mapping(name, code) as (values
    -- Spirits: tumbler glass
    ('Vodka',              '1f943'),
    ('Gin',                '1f943'),
    ('White Rum',          '1f943'),
    ('Dark Rum',           '1f943'),
    ('Blanco Tequila',     '1f943'),
    ('Bourbon',            '1f943'),
    ('Rye Whiskey',        '1f943'),
    ('Scotch',             '1f943'),
    ('Brandy',             '1f943'),
    ('Mezcal',             '1f943'),

    -- Liqueurs: cocktail glass
    ('Triple Sec',         '1f378'),
    ('Campari',            '1f378'),
    ('Aperol',             '1f378'),
    ('Amaretto',           '1f378'),
    ('Coffee Liqueur',     '2615'),   -- coffee cup
    ('Elderflower Liqueur','1f378'),
    ('Maraschino Liqueur', '1f378'),

    -- Wine & fortified
    ('Sweet Vermouth',     '1f377'),  -- wine glass
    ('Dry Vermouth',       '1f377'),
    ('Prosecco',           '1f942'),  -- clinking glasses
    ('Champagne',          '1f942'),

    -- Bitters: droplet
    ('Angostura Bitters',  '1f4a7'),
    ('Orange Bitters',     '1f4a7'),
    ('Peychauds Bitters',  '1f4a7'),

    -- Mixers
    ('Soda Water',         '1f4a7'),
    ('Tonic Water',        '1f4a7'),
    ('Ginger Beer',        '1f37a'),  -- beer mug
    ('Cola',               '1f964'),  -- cup with straw
    ('Ginger Ale',         '1f964'),

    -- Juices
    ('Lime Juice',         '1f34b'),  -- lemon (no dedicated lime PNG)
    ('Lemon Juice',        '1f34b'),
    ('Orange Juice',       '1f34a'),
    ('Cranberry Juice',    '1f352'),  -- cherry, closest red-berry
    ('Pineapple Juice',    '1f34d'),
    ('Grapefruit Juice',   '1f34a'),
    ('Tomato Juice',       '1f345'),

    -- Fresh
    ('Lime',               '1f34b'),
    ('Lemon',              '1f34b'),
    ('Orange',             '1f34a'),
    ('Mint',               '1f33f'),
    ('Ginger',             '1f331'),
    ('Cucumber',           '1f952'),
    ('Egg White',          '1f95a'),

    -- Syrups
    ('Simple Syrup',       '1f36f'),  -- honey pot
    ('Grenadine',          '1f352'),
    ('Orgeat',             '1f95c'),  -- peanut (orgeat is almond)
    ('Honey Syrup',        '1f36f'),
    ('Agave Syrup',        '1f335'),  -- cactus

    -- Other
    ('Sugar',              '1f9c2'),  -- salt shaker
    ('Salt',               '1f9c2'),
    ('Ice',                '1f9ca')
)
update public.ingredients i
set icon_url = 'https://cdn.jsdelivr.net/gh/jdecked/twemoji@latest/assets/72x72/' || m.code || '.png'
from mapping m
where i.name = m.name;
