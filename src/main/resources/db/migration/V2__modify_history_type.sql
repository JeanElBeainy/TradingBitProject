alter table history
    modify from_price decimal(30, 8) not null;

alter table history
    modify to_price decimal(30, 8) not null;

alter table history
    modify volume decimal(30, 8) not null;

alter table history
    modify fee decimal(30, 8) not null;
