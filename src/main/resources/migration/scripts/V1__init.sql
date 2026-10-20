CREATE SEQUENCE product_seq MINVALUE 1 INCREMENT 50;

CREATE TABLE public.products (
                                 price numeric(38, 2) NOT NULL,
                                 stock_quantity int4 NOT NULL,
                                 product_id int8 NOT NULL,
                                 "name" varchar(150) NOT NULL,
                                 description text NULL,
                                 CONSTRAINT products_pkey PRIMARY KEY (product_id)
);