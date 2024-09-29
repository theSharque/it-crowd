CREATE TABLE gits
(
    id            serial PRIMARY KEY,
    name          varchar,
    url           varchar,
    hash          varchar,
    location      varchar,
    token         varchar,
    status        varchar,
    last_modified timestamp default now()
);

CREATE TABLE java_methods
(
    id             serial PRIMARY KEY,
    git_id         bigint REFERENCES gits (id),
    method_name    varchar,
    original_body  varchar,
    hash           varchar,
    modified_body  varchar,
    commit_message varchar,
    status         varchar,
    last_modified  timestamp default now()
);
