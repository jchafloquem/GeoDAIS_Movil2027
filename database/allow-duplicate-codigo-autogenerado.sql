BEGIN;

ALTER TABLE public.registros_parcela
	DROP CONSTRAINT IF EXISTS uq_parcela_codigo;

COMMIT;
