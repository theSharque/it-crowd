CREATE TABLE GITS
(
    id            serial PRIMARY KEY,
    name          varchar   not null,
    url           varchar   not null,
    hash          varchar,
    location      varchar,
    token         varchar,
    status        varchar   not null,
    last_modified timestamp not null default now()
);

CREATE TABLE JAVA_METHODS
(
    id             serial PRIMARY KEY,
    git_id         bigint REFERENCES GITS (id),
    method_name    varchar   not null,
    original_body  varchar   not null,
    hash           varchar   not null,
    modified_body  varchar,
    commit_message varchar,
    status         varchar   not null,
    last_modified  timestamp not null default now()
);

CREATE TABLE PYTHON_METHODS
(
    id             serial PRIMARY KEY,
    git_id         bigint REFERENCES GITS (id),
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
)
