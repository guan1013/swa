package de.shop.util.exceptions;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;

public class InvalidGesamtpreisException extends AbstractBestellungServiceException {

	private static final long serialVersionUID = -1240578893715951960L;
	private final Double gesamtpreis;
	private final Collection<ConstraintViolation<Bestellung>> violations;

	public InvalidGesamtpreisException(Double pPreis,
			Collection<ConstraintViolation<Bestellung>> pVio) {
		super("Ungueltiger Gesamtpreis: " + pPreis + ", Violations: " + pVio);
		this.gesamtpreis = pPreis;
		this.violations = pVio;
	}

	public Double getGesamtpreis() {
		return gesamtpreis;
	}

	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
