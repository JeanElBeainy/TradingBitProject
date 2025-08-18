alter table portfolio
    modify quantity decimal(30, 8) not null;

alter table history
    modify from_quantity decimal(30, 8) not null;

alter table history
    modify to_quantity decimal(30, 8) not null;

alter table history
    modify fee decimal(15, 8) not null;
