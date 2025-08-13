create table portfolio
(
    id            bigint         not null
        primary key,
    purchase_type VARCHAR(15)    not null,
    symbol        VARCHAR(10)    not null,
    quantity      decimal(15, 8) not null,
    constraint portfolio_balance_id_fk
        foreign key (id) references balance (id)
);