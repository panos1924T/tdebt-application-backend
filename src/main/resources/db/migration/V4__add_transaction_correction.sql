ALTER TABLE transactions
    ADD COLUMN corrected_transaction_id BIGINT NULL;


ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_corrected
        FOREIGN KEY (corrected_transaction_id) REFERENCES transactions (id)
        ON DELETE RESTRICT;

CREATE INDEX idx_transactions_corrected ON transactions (corrected_transaction_id) WHERE corrected_transaction_id IS NOT NULL;