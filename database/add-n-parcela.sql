ALTER TABLE public.registros_parcela
	ADD COLUMN IF NOT EXISTS n_parcela varchar(50) NULL;

CREATE OR REPLACE FUNCTION public.fn_parcela_n_parcela_before_write()
RETURNS trigger AS $$
DECLARE
	dni_value text;
	next_sequence bigint;
BEGIN
	IF TG_OP = 'UPDATE'
		AND OLD.n_parcela IS NOT NULL
		AND NEW.dni_productor IS NOT DISTINCT FROM OLD.dni_productor THEN
		NEW.n_parcela := OLD.n_parcela;
		RETURN NEW;
	END IF;

	dni_value := btrim(NEW.dni_productor::text);
	PERFORM pg_advisory_xact_lock(
		hashtext('public.registros_parcela.n_parcela'),
		hashtext(dni_value)
	);

	SELECT COALESCE(MAX(substring(p.n_parcela FROM char_length(dni_value) + 2)::bigint), 0) + 1
	INTO next_sequence
	FROM public.registros_parcela p
	WHERE p.dni_productor = NEW.dni_productor
		AND p.n_parcela ~ ('^' || dni_value || '_[0-9]+$');

	NEW.n_parcela := dni_value || '_' || next_sequence;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_parcela_n_parcela_before_write ON public.registros_parcela;

CREATE TRIGGER trg_parcela_n_parcela_before_write
BEFORE INSERT OR UPDATE ON public.registros_parcela
FOR EACH ROW EXECUTE FUNCTION public.fn_parcela_n_parcela_before_write();
