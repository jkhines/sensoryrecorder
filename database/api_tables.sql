--
-- Tables for REST API/Application functionality, PostgreSQL
--
CREATE TABLE IF NOT EXISTS input_data (
	id SERIAL PRIMARY KEY,
	username VARCHAR(50) NOT NULL,
	type VARCHAR(64) NOT NULL,
        quantity BIGINT,
	recorded_at TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_data_timestamp()
RETURNS TRIGGER AS
$$
BEGIN
    UPDATE input_data
    SET recorded_at = CURRENT_TIMESTAMP
    WHERE ID = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER update_data
  AFTER INSERT
  ON input_data
  FOR EACH ROW
  EXECUTE PROCEDURE update_data_timestamp();


CREATE TABLE IF NOT EXISTS devices (
	id SERIAL PRIMARY KEY,
	client_id VARCHAR(64) NOT NULL,
	client_name VARCHAR(256) NOT NULL,
	username VARCHAR(50) NOT NULL,
        approved BOOLEAN NOT NULL,
	recorded_at TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_devices_timestamp()
RETURNS TRIGGER AS
$$
BEGIN
    UPDATE devices
    SET recorded_at = CURRENT_TIMESTAMP
    WHERE ID = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER update_devices
  AFTER INSERT
  ON devices
  FOR EACH ROW
  EXECUTE PROCEDURE update_devices_timestamp();


CREATE INDEX IF NOT EXISTS id_ty_idx ON input_data(type);
CREATE INDEX IF NOT EXISTS id_un_idx ON devices(username);
