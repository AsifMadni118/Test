CREATE TABLE credentials (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    active BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_credentials PRIMARY KEY (id),
    CONSTRAINT uk_credentials_user_id UNIQUE (user_id),
    CONSTRAINT uk_credentials_email UNIQUE (email)
);
