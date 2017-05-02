#!/bin/sh

SCRIPT_DIR=`dirname "$(readlink -f "$0")"`

. $SCRIPT_DIR/devenv.sh

docker run -d -p 5432:5432 postgres:latest
psql -c "\q" > /dev/null 2>&1
while [ $? -ne 0 ]
do
    echo "Waiting for PostgreSQL to start..."
    sleep 2
    psql -c "\q" > /dev/null 2>&1
done

$SCRIPT_DIR/auth_db.sh
$SCRIPT_DIR/api_db.sh

