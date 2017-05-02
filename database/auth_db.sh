#!/bin/sh
# Summary: This file creates, configures, and populates the initial database
#          for the authentication server. It is expected to be run from 
#          the same directory as the database schema and data definitions.
# Owner: John K. Hines
# Date: January 26, 2017
SCRIPT_DIR=`dirname "$(readlink -f "$0")"`

echo "Creating user, auth database, and granting permissions"
psql -c "CREATE USER $AUTHDBUSER WITH PASSWORD '$AUTHDBPASSWORD';"
psql -c "CREATE DATABASE $AUTHDATABASE;"
psql -c "GRANT $AUTHDBUSER TO $PGUSER;"
psql -d $AUTHDATABASE -c "REVOKE ALL ON DATABASE $AUTHDATABASE FROM public;"
psql -d $AUTHDATABASE -c "GRANT CONNECT ON DATABASE $AUTHDATABASE TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "GRANT TEMP ON DATABASE $AUTHDATABASE TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "GRANT USAGE ON SCHEMA public TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "ALTER DEFAULT PRIVILEGES FOR ROLE $PGUSER IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO $AUTHDBUSER;"
psql -d $AUTHDATABASE -c "ALTER DEFAULT PRIVILEGES FOR ROLE $PGUSER IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO $AUTHDBUSER;"

echo "Creating database tables"
psql -d $AUTHDATABASE -f $SCRIPT_DIR/auth_tables.sql

echo "Populating tables with initial data"
psql -d $AUTHDATABASE -v admin_user=$AUTHADMINUSER -v admin_password=$AUTHADMINPASSWORD -v client_id=$CLIENTID -v client_secret=$CLIENTSECRET -v api_client_id=$APICLIENTID -v api_client_secret=$APICLIENTSECRET -f $SCRIPT_DIR/auth_data.sql
