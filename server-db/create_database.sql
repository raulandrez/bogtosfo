CREATE ROLE bog2sfo_user LOGIN ENCRYPTED PASSWORD 'PASSWORD'   VALID UNTIL 'infinity';


CREATE DATABASE bog2sfo_db
  WITH ENCODING='UTF8'
       OWNER=bog2sfo_user
       TEMPLATE=postgis_template
       CONNECTION LIMIT=-1;


