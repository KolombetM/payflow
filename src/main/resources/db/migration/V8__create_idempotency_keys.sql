create table idempotency_keys (
    key varchar(100) primary key,
    payment_id uuid not null references payments(id),
    created_at timestamptz not null default now()
)