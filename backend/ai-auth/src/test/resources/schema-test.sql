CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(64) NOT NULL UNIQUE,
    password_hash   VARCHAR(128) NOT NULL,
    roles           VARCHAR(512) NOT NULL,
    permissions     VARCHAR(512) NOT NULL,
    status          TINYINT NOT NULL DEFAULT 1,
    last_login_at   TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
