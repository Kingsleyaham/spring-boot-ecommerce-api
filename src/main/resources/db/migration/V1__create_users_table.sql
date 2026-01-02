CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL,
    profile_image TEXT,
    verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    token_expiration TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uk_users_email_active ON users (email)
WHERE
    deleted_at IS NULL;

CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_users_deleted_at ON users (deleted_at)
WHERE
    deleted_at IS NULL;