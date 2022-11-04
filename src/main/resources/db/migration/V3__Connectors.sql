alter table namespaces
    rename to connectors;

alter table connectors
    add type text,
    add config text;
