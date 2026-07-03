-- Insert roles
INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('USER');

-- Insert capabilities
INSERT INTO capabilities (name, description)
VALUES
    ('VIEW_USERS',       'View User/Users and details'),
    ('DELETE_USER', 'De-activate User account'),
    ('ACTIVATE_USER',   'Activate a deleted account'),
    ('VIEW_ONLY_USER',  'View only own User details'),
    ('DELETE_ONLY_USER','De-activate only own User account'),
    ('EDIT_ONLY_USER', 'Edit only own User details');

-- Apply capabilities to ADMIN
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r, capabilities c
WHERE r.name = 'ADMIN'
  AND c.name IN ('VIEW_USERS','DELETE_USER', 'ACTIVATE_USER');

-- Apply capabilities to USER
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r, capabilities c
WHERE r.name = 'USER'
  AND c.name IN ('VIEW_ONLY_USER', 'DELETE_ONLY_USER', 'EDIT_ONLY_USER');