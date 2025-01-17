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


--changeset nelmin:2024-07-92-18-00
ALTER TABLE public.user
    ADD COLUMN IF NOT EXISTS description varchar(512);

--changeset nelmin:2024-07-03-18-00
ALTER TABLE public.user DROP COLUMN IF EXISTS image;

--changeset nelmin:2024-08-14-18-00
CREATE TABLE public.premium
(
    id           BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    enabled      BOOLEAN,
    CONSTRAINT premium_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-08-17-01-00
ALTER TABLE public.premium ADD COLUMN IF NOT EXISTS expired_date timestamp without time zone;

--changeset nelmin:2024-08-06-01-00
ALTER TABLE public.user ADD COLUMN IF NOT EXISTS blocked boolean default false;

--changeset nelmin:2024-08-06-10-00
CREATE TABLE report
(
    id           BIGINT NOT NULL,
    user_id      BIGINT NOT NULL REFERENCES "user",
    article_id   BIGINT NOT NULL REFERENCES article,
    type         VARCHAR(255),
    description  VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT report_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-08-27-10-00
ALTER TABLE public.user ADD COLUMN IF NOT EXISTS image_path varchar(255);

--changeset nelmin:2024-09-13-17-00
ALTER TABLE public.report DROP CONSTRAINT IF EXISTS report_article_id_fkey;