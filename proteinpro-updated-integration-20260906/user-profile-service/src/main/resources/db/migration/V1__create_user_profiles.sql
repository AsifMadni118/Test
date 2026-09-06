CREATE TABLE user_profiles (
    id VARCHAR(36) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_profiles PRIMARY KEY (id),
    CONSTRAINT uk_user_profiles_email UNIQUE (email)
);
