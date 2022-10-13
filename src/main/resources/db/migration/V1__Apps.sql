create table apps
(
    id uuid not null primary key,
    token text not null
);

create index on apps (token);
