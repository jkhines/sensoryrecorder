START TRANSACTION;
    SET myvars.client_id TO :'client_id';
    SET myvars.client_secret TO :'client_secret';
    SET myvars.api_client_id TO :'api_client_id';
    SET myvars.api_client_secret TO :'api_client_secret';

    ---------------------------------------------------------------------
    -- users
    ---------------------------------------------------------------------
    INSERT INTO users (username, password, enabled) VALUES
        (:'admin_user',:'admin_password',true)
    ON CONFLICT(username) DO NOTHING;

    ---------------------------------------------------------------------
    -- authorities
    ---------------------------------------------------------------------
    INSERT INTO authorities (username, authority) VALUES
        (:'admin_user','ROLE_ADMIN')
    ON CONFLICT(username, authority) DO NOTHING;

    ---------------------------------------------------------------------
    -- system_scope
    ---------------------------------------------------------------------
    INSERT INTO system_scope (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
        ('openid', 'log in using your identity', 'user', false, false, false, null),
        ('profile', 'basic profile information', 'list-alt', false, false, false, null),
        ('email', 'email address', 'envelope', false, true, false, null),
        ('offline_access', 'offline access', 'time', false, false, false, null),
        ('dssr:device:create', 'register devices', 'plus', false, true, false, null),
        ('dssr:data:create', 'create data', 'plus', false, true, false, null),
        ('dssr:device:read', 'read devices', 'share', false, false, false, null),
        ('dssr:device:update', 'approve devices', 'edit', false, false, false, null),
        ('dssr:data:read', 'read device data', 'share', false, false, false, null),
        ('dssr:user:create', 'create users', 'user', false, false, false, null)
    ON CONFLICT(scope) DO NOTHING;

    ---------------------------------------------------------------------
    -- client_details
    ---------------------------------------------------------------------
    INSERT INTO client_details (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
        (current_setting('myvars.client_id'), current_setting('myvars.client_secret'), 'Official Client', false, null, 3600, 600, false),
        (current_setting('myvars.api_client_id'), current_setting('myvars.api_client_secret'), 'Official API', false, null, 3600, 600, true)
    ON CONFLICT(client_id) DO UPDATE SET client_secret = current_setting('myvars.client_secret');

    DO $$
    DECLARE
        _client_id INTEGER;
        _api_client_id INTEGER;
    BEGIN
        SELECT id INTO _client_id FROM client_details WHERE client_id = current_setting('myvars.client_id');
        SELECT id INTO _api_client_id FROM client_details WHERE client_id = current_setting('myvars.api_client_id');

        ---------------------------------------------------------------------
        -- client_scope
        ---------------------------------------------------------------------
        INSERT INTO client_scope (owner_id, scope) VALUES
            (_client_id, 'openid'),
            (_client_id, 'email'),
            (_client_id, 'offline_access'),
            (_client_id, 'dssr:device:read'),
            (_client_id, 'dssr:device:update'),
            (_client_id, 'dssr:data:create'),
            (_client_id, 'dssr:data:read'),
            (_client_id, 'dssr:user:create'),
            (_api_client_id, 'dssr:device:create'),
            (_api_client_id, 'dssr:device:read'),
            (_api_client_id, 'dssr:device:update'),
            (_api_client_id, 'dssr:data:create'),
            (_api_client_id, 'dssr:data:read'),
            (_api_client_id, 'dssr:user:create')
        ON CONFLICT(owner_id, scope) DO NOTHING;

        ---------------------------------------------------------------------
        -- client_redirect_uri
        ---------------------------------------------------------------------
        INSERT INTO client_redirect_uri (owner_id, redirect_uri) VALUES
            (_client_id, ''),
            (_api_client_id, '')
        ON CONFLICT(owner_id, redirect_uri) DO NOTHING;

        ---------------------------------------------------------------------
        -- client_grant_type
        -- _api_client_id has no grants, is for introspection only
        ---------------------------------------------------------------------
        INSERT INTO client_grant_type (owner_id, grant_type) VALUES
            (_client_id, 'urn:ietf:params:oauth:grant_type:redelegate'),
            (_client_id, 'authorization_code'),
            (_client_id, 'client_credentials'),
            (_client_id, 'password'),
            (_client_id, 'refresh_token')
        ON CONFLICT(owner_id, grant_type) DO NOTHING;
    END $$;
COMMIT;
