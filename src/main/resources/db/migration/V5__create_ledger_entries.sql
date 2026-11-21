create table ledger_entries(
    id uuid primary key,
    payment_id uuid not null references payments(id),
    account_id uuid not null references accounts(id),
    amount numeric(14,2) not null check (amount <> 0),
    created_at timestamptz not null default now()
);

create index all_account_idx on ledger_entries(account_id, created_at);
create index payment_idx on ledger_entries(payment_id);

create function forbid_ledger_change() returns trigger
    language plpgsql as $$
begin
    raise exception 'ledger_entries is append-only';
end;
$$;

create trigger ledger_entries_no_update_delete
    before update or delete on ledger_entries
    for each row execute function forbid_ledger_change();

create trigger ledger_entries_no_truncate
    before truncate on ledger_entries
    for each statement execute function forbid_ledger_change();