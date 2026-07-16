ALTER TABLE transactions
    ADD COLUMN resulting_amount NUMERIC(19,4) NULL;

ALTER TABLE transactions
    ADD COLUMN resulting_action VARCHAR(20) NULL;

-- Backfill existing rows: for a never-corrected transaction the resulting
-- value IS its own amount/action. For existing correction rows we can't
-- reconstruct history perfectly retroactively, so backfill them with their
-- own amount/action too (best-effort); new corrections going forward will
-- be written correctly by the updated application code.
UPDATE transactions
SET resulting_amount = amount,
    resulting_action = action
WHERE resulting_amount IS NULL;

ALTER TABLE transactions
    ALTER COLUMN resulting_amount SET NOT NULL;

ALTER TABLE transactions
    ALTER COLUMN resulting_action SET NOT NULL;