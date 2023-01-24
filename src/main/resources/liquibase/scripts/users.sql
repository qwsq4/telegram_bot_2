-- liquibase formatted sql

-- changeset bbazarov:1
CREATE TABLE task
(
    "id"                   SERIAL,
    "notification_chat_id" bigint   NOT NULL,
    "notification_message" character varying(255)  NOT NULL,
    "notification_date"    timestamp NOT NULL,
    PRIMARY KEY (id)
 );

