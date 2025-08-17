create table history
(
    id            bigint auto_increment
        primary key,
    from_symbol   VARCHAR(10)    not null,
    from_name     VARCHAR(50)    not null,
    from_quantity decimal(15, 8) not null,

    to_symbol     VARCHAR(10)    not null,
    to_name       VARCHAR(50)    not null,
    to_quantity   decimal(15, 8) not null,

    user_id       bigint         not null,
    volume        decimal(15, 8) not null
);
