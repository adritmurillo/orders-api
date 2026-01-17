create extension if not exists "pgcrypto";

create table zonas(
    id varchar(50) primary key,
    soporte_refrigeracion boolean not null default false
);

create table clientes(
    id varchar(50) primary key,
    activo boolean not null default true
);

create table pedidos(
    id uuid primary key default gen_random_uuid(),
    numero_pedido varchar(100) not null unique,
    cliente_id varchar(50) not null,
    zona_id varchar(50) not null,
    fecha_entrega date not null,
    estado varchar(20) not null check ( estado in ('PENDIENTE', 'CONFIRMADO', 'ENTREGADO')),
    requiere_refrigeracion boolean not null default false,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);

create index idx_pedidos_estado_fecha on pedidos(estado, fecha_entrega);
create table cargas_idempotencia(
    id uuid primary key default gen_random_uuid(),
    idempotency_key varchar(255) not null,
    archivo_hash varchar(255) not null,
    created_at timestamp default current_timestamp,
    constraint uk_idempotencia_archivo unique(idempotency_key, archivo_hash)
)