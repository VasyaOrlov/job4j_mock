create table if not exists tg_user (
id serial primary key,
username varchar unique,
email varchar unique,
chat_id bigint,
user_id int
);