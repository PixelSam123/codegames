create table main.user
(
    name     TEXT not null
        constraint user_pk
            primary key,
    password TEXT not null,
    role     TEXT not null
);

create table main.submission
(
    user_pk TEXT not null
        constraint submission_user_name_fk
            references user,
    content TEXT not null,
    status  TEXT not null
);
