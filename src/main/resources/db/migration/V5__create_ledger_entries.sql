create table ledger_entries(
    id uuid primary key,
    payment_id uuid references payments(id),
    account_id uuid references accounts(id),
    amount numeric(14,2) not null check (amount <> 0),
    created_at timestamptz not null default now()
);

create index all_columns_idx on ledger_entries(id, payment_id, account_id, amount, created_at);