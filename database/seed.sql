-- GlowLab — Datos iniciales (seed)
-- Coincide con el mock del frontend: íconos emoji por categoría

INSERT INTO categorias (nombre, descripcion, icono) VALUES
    ('Limpiadores',     'Productos para limpiar e higienizar el rostro',            '🫧'),
    ('Tónicos',         'Equilibran el pH y preparan la piel para el siguiente paso','💧'),
    ('Sérums',          'Tratamientos concentrados para necesidades específicas',    '✨'),
    ('Hidratantes',     'Cremas y geles que nutren y sellan la humedad',             '🧴'),
    ('Protección Solar','Filtros solares de amplio espectro para uso diario',        '☀️'),
    ('Tratamientos',    'Mascarillas, exfoliantes y activos especializados',         '🌿');

INSERT INTO productos (nombre, marca, descripcion, precio, tipos_piel, categoria_id) VALUES
    ('Gel Limpiador Suave',         'Cetaphil',        'Limpiador suave sin jabón, ideal para uso diario',             25000, 'seca,sensible,normal',    1),
    ('Espuma Limpiadora Purificante','La Roche-Posay',  'Elimina el exceso de sebo sin resecar',                        28000, 'grasa,mixta',             1),
    ('Agua Micelar 3 en 1',         'Garnier',         'Limpia, desmaquilla y tonifica en un solo paso',               18000, 'seca,mixta,sensible',     1),
    ('Tónico Hidratante Hialurón',  'The Ordinary',    'Aumenta la hidratación y suaviza la textura',                  22000, 'seca,normal,sensible',    2),
    ('Tónico Poros Minimizantes',   'Paula''s Choice',  'Reduce el tamaño de los poros con BHA al 0.3%',                35000, 'grasa,mixta',             2),
    ('Sérum Vitamina C 20%',        'Skinceuticals',   'Antioxidante que ilumina y unifica el tono',                   85000, 'normal,mixta,opaca',      3),
    ('Sérum Retinol 0.5%',          'The Ordinary',    'Renueva la piel y reduce líneas finas',                        32000, 'normal,mixta',            3),
    ('Sérum Niacinamida 10%',       'The Inkey List',  'Regula el sebo y minimiza poros',                              25000, 'grasa,mixta',             3),
    ('Crema Hidratante Rich',       'CeraVe',          'Hidratación intensa con ceramidas y ácido hialurónico',        38000, 'seca,muy_seca,sensible',  4),
    ('Gel Hidratante Oil-Free',     'Neutrogena',      'Hidratación ligera sin obstruir los poros',                    27000, 'grasa,mixta',             4),
    ('Protector Solar SPF 50+',     'Heliocare',       'Filtro solar de amplio espectro con textura ultraligera',      52000, 'todos',                   5),
    ('Bloqueador Mineral SPF 30',   'Avène',           'Fórmula mineral suave para pieles reactivas',                  48000, 'sensible,seca,normal',    5),
    ('Mascarilla Arcilla Purif.',   'Origins',         'Mascarilla de arcilla que absorbe impurezas',                  42000, 'grasa,mixta',             6),
    ('Exfoliante AHA 10%',          'The Ordinary',    'Exfoliante químico que suaviza y renueva la superficie',       28000, 'normal,mixta,opaca',      6);
