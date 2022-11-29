delete from connectors
       where type is not null and
             (select count(*) from connector_types ct where ct.id = type) = 0;

alter table connectors
    add foreign key (type) references connector_types;
