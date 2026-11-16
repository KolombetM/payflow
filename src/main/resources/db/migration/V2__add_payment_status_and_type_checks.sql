create table payment_status(
    status varchar primary key
);

insert into payment_status (status) values ('COMPLETED'), ('DECLINED');

create table payment_type (
    type varchar primary key
);

insert into payment_type (type) values ('TRANSFER'), ('DEPOSIT');

alter table payments
    add constraint fk_payments_status
    foreign key (status) references payment_status (status);

alter table payments
    add constraint fk_payments_type
    foreign key (type) references payment_type (type);

