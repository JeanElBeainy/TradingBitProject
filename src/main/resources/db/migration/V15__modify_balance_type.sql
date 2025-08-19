alter table balance
    modify usd_balance decimal(30, 8) not null;

alter table balance
    modify crypto_balance decimal(30, 8) not null;

alter table balance
    modify stock_balance decimal(30, 8) not null;

alter table balance
    modify total_balance decimal(30, 8) not null;

alter table balance
    modify total_volume decimal(30, 8) not null;