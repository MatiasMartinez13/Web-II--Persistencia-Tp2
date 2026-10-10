-- 1) Crear una lista por defecto si no existe
INSERT INTO listas (nombre)
SELECT 'Sin clasificar'
WHERE NOT EXISTS (SELECT 1 FROM listas WHERE nombre = 'Sin clasificar');

-- 2) Reasignar los favoritos sin lista a la lista por defecto
UPDATE favoritos
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar')
WHERE lista_id IS NULL;

-- 3) Recién acá, declarar la columna NOT NULL
ALTER TABLE favoritos
    ALTER COLUMN lista_id SET NOT NULL;