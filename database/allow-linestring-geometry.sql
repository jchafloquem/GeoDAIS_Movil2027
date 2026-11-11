BEGIN;

ALTER TABLE public.registros_parcela
	ALTER COLUMN geom TYPE geometry
	USING geom::geometry;

ALTER TABLE public.registros_parcela
	DROP CONSTRAINT IF EXISTS ck_parcela_geom_type;

ALTER TABLE public.registros_parcela
	ADD CONSTRAINT ck_parcela_geom_type
	CHECK (
		GeometryType(geom) IN ('POINT', 'LINESTRING', 'POLYGON')
		AND ST_SRID(geom) = 4326
		AND ST_NDims(geom) IN (2, 3)
	);

COMMIT;
