CREATE TABLE public.point
(
   point_id serial, 
   x double precision, 
   y double precision, 
   z double precision, 
   t timestamp with time zone, 
   track_id bigint, 
   date_system timestamp without time zone NOT NULL DEFAULT now(), 
   CONSTRAINT pk_point_id PRIMARY KEY (point_id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.point
  OWNER TO bog2sfo_user;
