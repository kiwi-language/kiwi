create table public.instance
(
    id           bigint                     not null
        primary key,
    app_id       bigint                     not null,
    data         bytea                      not null,
    version      bigint default '0'::bigint not null,
    sync_version bigint default '0'::bigint not null,
    deleted_at   bigint default '0'::bigint not null,
    next_node_id bigint default 0           not null
);

alter table public.instance
    owner to postgres;

create table public.id_region
(
    type_category integer not null
        primary key,
    start_id      bigint  not null,
    end_id        bigint  not null,
    next_id       bigint  not null
);

alter table public.id_region
    owner to postgres;

create table public.id_block
(
    id        bigint   not null
        primary key,
    app_id    bigint   not null,
    type_id   bigint   not null,
    start_id  bigint   not null,
    end_id    bigint   not null,
    next_id   bigint   not null,
    is_active boolean  not null,
    type_tag  smallint not null
);

alter table public.id_block
    owner to postgres;

create index idx_end
    on public.id_block (end_id);

create table public.files
(
    name    varchar(255) not null
        primary key,
    content bytea        not null
);

alter table public.files
    owner to postgres;

create table public.index_entry
(
    app_id      bigint not null,
    index_id    bytea  not null,
    column0     bytea  not null,
    column1     bytea  not null,
    column2     bytea  not null,
    column3     bytea  not null,
    column4     bytea  not null,
    column5     bytea  not null,
    column6     bytea  not null,
    column7     bytea  not null,
    column8     bytea  not null,
    column9     bytea  not null,
    column10    bytea  not null,
    column11    bytea  not null,
    column12    bytea  not null,
    column13    bytea  not null,
    column14    bytea  not null,
    instance_id bytea  not null,
    primary key (app_id, index_id, column0, column1, column2, column3, column4, column5, column6, column7, column8,
                 column9, column10, column11, column12, column13, column14, instance_id)
);

alter table public.index_entry
    owner to postgres;

create table public.reference
(
    id             bigserial
        primary key,
    app_id         bigint  not null,
    kind           integer not null,
    source_tree_id bigint  not null,
    target_id      bytea   not null,
    field_id       bytea
);

alter table public.reference
    owner to postgres;

create index target_idx
    on public.reference (app_id, kind, target_id);

