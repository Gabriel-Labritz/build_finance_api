ALTER TABLE categories
    ADD CONSTRAINT uq_categories_name_user UNIQUE (name, user_id);