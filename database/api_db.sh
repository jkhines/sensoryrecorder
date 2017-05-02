#!/bin/sh
# Summary: This file creates, configures, and populates the initial database
#          for the API server. It is expected to be run from 
#          the same directory as the database schema and data definitions.
# Owner: John K. Hines
# Date: January 26, 2017
SCRIPT_DIR=`dirname "$(readlink -f "$0")"`

echo "Creating user, API database, and granting permissions"
psql -c "CREATE USER $APIDBUSER WITH PASSWORD '$APIDBPASSWORD';"
psql -c "CREATE DATABASE $APIDATABASE;"
psql -c "GRANT $APIDBUSER TO $PGUSER;"
psql -d $APIDATABASE -c "REVOKE ALL ON DATABASE $APIDATABASE FROM public;"
psql -d $APIDATABASE -c "GRANT CONNECT ON DATABASE $APIDATABASE TO $APIDBUSER;"
psql -d $APIDATABASE -c "GRANT TEMP ON DATABASE $APIDATABASE TO $APIDBUSER;"
psql -d $APIDATABASE -c "GRANT USAGE ON SCHEMA public TO $APIDBUSER;"
psql -d $APIDATABASE -c "GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO $APIDBUSER;"
psql -d $APIDATABASE -c "GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO $APIDBUSER;"
psql -d $APIDATABASE -c "ALTER DEFAULT PRIVILEGES FOR ROLE $PGUSER IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO $APIDBUSER;"
psql -d $APIDATABASE -c "ALTER DEFAULT PRIVILEGES FOR ROLE $PGUSER IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO $APIDBUSER;"

echo "Creating database tables"
psql -d $APIDATABASE -f $SCRIPT_DIR/api_tables.sql
