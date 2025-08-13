create table balance
(
    id             bigint auto_increment
        primary key,
    usd_balance    double not null,
    crypto_balance double not null,
    stock_balance  double not null,
    total_balance  double not null,
    constraint portfolio_users_id_fk
        foreign key (id) references users (id)
);
