CREATE TABLE GITS
(
    id            serial PRIMARY KEY,
    name          varchar   not null,
    url           varchar   not null,
    username      varchar,
    password      varchar,
    hash          varchar,
    location      varchar,
    token         varchar,
    status        varchar   not null,
    last_modified timestamp not null default now()
);

CREATE TABLE FUNCTIONS
(
    id             serial PRIMARY KEY,
    git_id         bigint REFERENCES GITS (id),
    language       varchar   not null,
    file_location  varchar   not null,
    method_name    varchar   not null,
    original_body  varchar   not null,
    hash           varchar   not null,
    modified_body  varchar,
    commit_message varchar,
    status         varchar   not null,
    last_modified  timestamp not null default now()
);

CREATE TABLE CHAT
(
    id        serial PRIMARY KEY,
    author    varchar   not null,
    message   varchar,
    timestamp timestamp not null default now()
);

CREATE TABLE SETTINGS
(
    id  varchar,
    val varchar
);

INSERT INTO SETTINGS (id, val)
VALUES ('Model', 'deepseek-coder-v2'),
       ('Temperature', '0.0'),
       ('Language', 'english'),
       ('Roy', 'active'),
       ('Moss', 'active');
