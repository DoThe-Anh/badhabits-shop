-- =============================================================================
-- V1__init_schema.sql — Schema khởi tạo cho shop (MySQL 8 / MariaDB)
-- Khớp với entity hiện tại: User, Category, Product (+ ElementCollections),
-- Order, OrderDetail.
-- =============================================================================

-- ----- categories -----
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    slug        VARCHAR(100) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ----- users -----
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(190) NOT NULL,
    phone         VARCHAR(32),
    address       VARCHAR(500),
    role          VARCHAR(16)  NOT NULL,
    enabled       BIT(1)       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ----- products -----
CREATE TABLE products (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    slug           VARCHAR(200) NOT NULL,
    name           VARCHAR(500) NOT NULL,
    category_id    BIGINT       NOT NULL,
    collection     VARCHAR(100),
    price          BIGINT       NOT NULL,
    original_price BIGINT,
    primary_image  VARCHAR(500) NOT NULL,
    hover_image    VARCHAR(500),
    description    VARCHAR(2000),
    material       VARCHAR(1000),
    stock          INT          NOT NULL,
    is_new         BIT(1)       NOT NULL,
    is_featured    BIT(1)       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_slug (slug),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_is_new      ON products (is_new);
CREATE INDEX idx_products_is_featured ON products (is_featured);

-- ----- product_gallery (ElementCollection) -----
CREATE TABLE product_gallery (
    product_id BIGINT NOT NULL,
    image_url  VARCHAR(500),
    CONSTRAINT fk_gallery_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_gallery_product_id ON product_gallery (product_id);

-- ----- product_sizes (ElementCollection) -----
CREATE TABLE product_sizes (
    product_id BIGINT NOT NULL,
    size_value VARCHAR(32),
    CONSTRAINT fk_sizes_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_sizes_product_id ON product_sizes (product_id);

-- ----- product_colors (ElementCollection of @Embeddable Color) -----
CREATE TABLE product_colors (
    product_id BIGINT NOT NULL,
    color_name VARCHAR(64),
    color_hex  VARCHAR(16),
    CONSTRAINT fk_colors_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_colors_product_id ON product_colors (product_id);

-- ----- orders -----
CREATE TABLE orders (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    order_code     VARCHAR(32)  NOT NULL,
    placed_at      DATETIME(6)  NOT NULL,
    user_id        BIGINT,
    full_name      VARCHAR(190) NOT NULL,
    email          VARCHAR(190) NOT NULL,
    phone          VARCHAR(32)  NOT NULL,
    address        VARCHAR(500) NOT NULL,
    city           VARCHAR(120) NOT NULL,
    note           VARCHAR(1000),
    payment_method VARCHAR(32)  NOT NULL,
    subtotal       BIGINT       NOT NULL,
    shipping       BIGINT       NOT NULL,
    total          BIGINT       NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_code (order_code),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_orders_user_id    ON orders (user_id);
CREATE INDEX idx_orders_placed_at  ON orders (placed_at);
CREATE INDEX idx_orders_status     ON orders (status);

-- ----- order_items -----
CREATE TABLE order_items (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    product_id   VARCHAR(64)  NOT NULL,
    product_slug VARCHAR(190) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    price        BIGINT       NOT NULL,
    image        VARCHAR(500),
    size         VARCHAR(32),
    color        VARCHAR(64),
    quantity     INT          NOT NULL,
    position     INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
