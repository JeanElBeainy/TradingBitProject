alter table history
    add from_price decimal(15, 8) not null;

alter table history
    add to_price decimal(15, 8) not null;