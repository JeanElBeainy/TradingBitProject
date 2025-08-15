alter table portfolio
    add user_id bigint not null;

alter table portfolio
drop foreign key portfolio_balance_id_fk;

alter table portfolio
    add constraint portfolio_users_id_fk
        foreign key (user_id) references users (id);
