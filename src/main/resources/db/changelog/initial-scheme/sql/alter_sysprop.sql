--liquibase formatted sql
--changeset anclav:issue-128 alter sysprop
--preconditions onFail:MARK_RAN onError:HALT

alter table sys_prop add column description varchar(255)