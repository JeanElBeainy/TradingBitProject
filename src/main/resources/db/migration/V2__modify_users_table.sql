alter table users
    modify name varchar(30) not null;

alter table users
    add role VARCHAR(25) not null;