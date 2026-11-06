-- 1) Columna aún nullable: las listas heredadas del TP2 no tienen dueño todavía.
ALTER TABLE listas ADD COLUMN usuario_id BIGINT;

-- 2) Usuario semilla para absorber las listas sin dueño.
INSERT INTO usuarios (email, password_hash, rol)
SELECT 'semilla@local', '$2a$10$7EqJtq98/Pw8tGCaf2j6UeiWniN6yu6xLj8j3yWk4Q0zZ8Y0yXQ9Q', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'semilla@local');

-- 3) Reasignar las listas heredadas al usuario semilla.
UPDATE listas SET usuario_id = (SELECT id FROM usuarios WHERE email = 'semilla@local') WHERE usuario_id IS NULL;

-- 4) Recién ahora, endurecer: NOT NULL + FK.
ALTER TABLE listas ALTER COLUMN usuario_id SET NOT NULL;
ALTER TABLE listas ADD CONSTRAINT fk_listas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);
