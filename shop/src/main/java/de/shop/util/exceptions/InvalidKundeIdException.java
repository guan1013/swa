package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidKundeIdException extends AbstractKundeServiceException {

	private static final long serialVersionUID = -6311420210935434921L;
	private final Integer kundeId;
	private final Collection<ConstraintViolation<Kunde>> violations;

	public InvalidKundeIdException(Integer pKID,
			Collection<ConstraintViolation<Kunde>> pVio) {
		super("Ungueltige Kunde-ID: " + pKID + ", Violations: " + pVio);
		this.kundeId = pKID;
		this.violations = pVio;
	}

	public Integer getKundeId() {
		return kundeId;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
