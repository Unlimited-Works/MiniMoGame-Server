CREATE DATABASE game with ENCODING = utf8 LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';
CREATE DATABASE game_test with ENCODING = utf8 LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';

Create table if not exists users (
	oid char(24), -- objectId
	user_name varchar(32) not null, -- 8 utf8
	pwd varchar(50),
	primary key (oid),
	UNIQUE (user_name)
);

CREATE INDEX index_user_name ON users (user_name);