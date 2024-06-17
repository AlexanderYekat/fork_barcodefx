--liquibase formatted sql
--changeset viktor.barsukov:create_all_tables
--preconditions onFail:MARK_RAN onError:HALT

create table printing_profile
(
    ID BIGINT auto_increment,

	name varchar(255) not null,
	type varchar(255) not null,
	profile text not null,

    constraint PRINTING_PROFILE_PK
    primary key (ID)
);

create table sys_prop
(
    ID BIGINT auto_increment,

	sys_key varchar(255) not null,
	type varchar(255) not null,
	value text not null,

    constraint SYS_PROP_PK
    primary key (ID)
);



create unique index printing_profile_name_uindex
	on printing_profile (name);

create unique index sys_prop_sys_key_uindex
	on sys_prop (sys_key);




