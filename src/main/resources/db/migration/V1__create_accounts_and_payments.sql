create table accounts (
    id          uuid primary key,
    owner_name  varchar(100)   not null,
    balance     numeric(14, 2) not null default 0,
    currency    char(3)        not null,
    created_at  timestamptz    not null default now()
);

create table payments (
    id uuid primary key,
    from_account_id uuid references accounts(id),
    to_account_id uuid not null references accounts(id),
    amount numeric(14,2) not null check (amount > 0),
    status varchar not null,
    type varchar not null,
    created_at timestamptz not null default now()
);

create index daily_limit_idx on payments(from_account_id, type, created_at)