alter table accounts
    add column type varchar(20) not null default 'CUSTOMER';

alter table accounts
    add constraint chk_accounts_type check (type in ('CUSTOMER', 'EXTERNAL'));

insert into accounts (id, owner_name, balance, currency, type)
    values ('00000000-0000-0000-0000-000000000001', 'EXTERNAL EUR', 0, 'EUR', 'EXTERNAL')
