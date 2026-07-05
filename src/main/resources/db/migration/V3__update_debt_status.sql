ALTER TABLE debts
DROP CONSTRAINT IF EXISTS debts_debt_status_check;

ALTER TABLE debts
    ADD CONSTRAINT chk_debts_debt_status
        CHECK (debt_status IN ('OPEN', 'ARCHIVED'));