CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT price_positive CHECK (price > 0),
    CONSTRAINT stock_non_negative CHECK (stock >= 0)
    );

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);