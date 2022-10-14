create table namespaces
(
    id uuid not null primary key,
    app_id uuid not null references apps (id),
    name text not null,
    token text not null
);

create index on namespaces (app_id);
create index on namespaces (token);
