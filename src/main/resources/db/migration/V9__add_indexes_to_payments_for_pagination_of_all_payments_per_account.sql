create index from_account_payments_idx on payments(from_account_id, created_at);
create index to_account_payment_idx on payments(to_account_id, created_at);