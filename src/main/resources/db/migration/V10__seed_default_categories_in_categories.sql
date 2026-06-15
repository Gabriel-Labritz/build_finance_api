INSERT INTO categories (id, name, default_category, type, user_id) VALUES
    (UUID_TO_BIN(UUID()), 'Alimentação',    1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Saúde',          1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Transporte',     1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Moradia',        1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Educação',       1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Entretenimento', 1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Serviços', 1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Vestuário',      1, 'EXPENSE', NULL),
    (UUID_TO_BIN(UUID()), 'Salário',        1, 'INCOME',   NULL),
    (UUID_TO_BIN(UUID()), 'Freelance',      1, 'INCOME',   NULL),
    (UUID_TO_BIN(UUID()), 'Rendimentos',  1, 'INCOME',   NULL);