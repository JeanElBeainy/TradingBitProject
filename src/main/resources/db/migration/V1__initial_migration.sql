create table users
(
    id            BIGINT auto_increment
        primary key,
    name          VARCHAR(255) not null,
    email         VARCHAR(255) not null,
    password      VARCHAR(255) not null,
    creation_date datetime     null
);