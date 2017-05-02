#============================================
# DSSR values
# Web severs - append to /etc/default/jetty9
# IoT device - append to /etc/profile
# XDK daemon - append to /lib/systemd/system/xdk-daemon.service
#============================================
# Infrastructure servers
export APISERVER=""
export AUTHSERVER=""
export DBSERVER=""
# Server-specific values
export AUTHPROTOCOL=""
export APIPROTOCOL=""
# Database owner
export PGUSER=""
export PGPASSWORD=""
export PGHOST=""
export PGDATABASE=""
# Database user
export APIDBUSER=""
export APIDBPASSWORD=""
export APIDBHOST=""
export APIDATABASE=""
# Database user
export AUTHDBUSER=""
export AUTHDBPASSWORD=""
export AUTHDBHOST=""
export AUTHDATABASE=""
# Auth web app admin
export AUTHADMINUSER=""
export AUTHADMINPASSWORD=""
# Official clients
export CLIENTID=""
export CLIENTSECRET=""
export APICLIENTID=""
export APICLIENTSECRET=""
