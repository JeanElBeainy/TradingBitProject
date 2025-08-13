alter table balance
drop foreign key portfolio_users_id_fk;

rename table balance to balance;

alter table balance
    add constraint balance_users_id_fk
        foreign key (id) references users (id);