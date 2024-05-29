--liquibase formatted sql
--changeset nelmin:2024-06-29-23-00

CREATE TABLE if not exists public.user
(
    id                bigserial    NOT NULL,
    username          varchar(255) NOT NULL,
    nick_name         varchar(255) NOT NULL,
    "password"        varchar(255) NOT NULL,
    enabled           bool         NULL,
    registration_date timestamp    NULL,
    update_time       timestamp    NULL,
    last_login_date   timestamp    NULL,
    image             bytea        NULL,

    CONSTRAINT users_pkey primary key (id),
    CONSTRAINT uk_user_username UNIQUE (username)
);