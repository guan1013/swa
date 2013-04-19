package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;

@ApplicationException(rollback = true)
public class InvalidBestellungIdException extends AbstractBestellungServiceException {

	private static final long serialVersionUID = 7046802277087384158L;
	private final Integer bestellungId;
	private final Collection<ConstraintViolation<Bestellung>> violations;

	public InvalidBestellungIdException(Integer pBID,
			Collection<ConstraintViolation<Bestellung>> pVio) {
		super("Ungueltige Bestellung-ID: " + pBID + ", Violations: " + pVio);
		this.bestellungId = pBID;
		this.violations = pVio;
	}

	public Integer getBestellungId() {
		return bestellungId;
	}

	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
