CREATE DATABASE game with ENCODING = utf8 LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';

Create table if not exists users (
	oid bytea, -- objectId
	username varchar(32), -- 8 utf8
	pwd varchar(50),
	primary key (oid)
);