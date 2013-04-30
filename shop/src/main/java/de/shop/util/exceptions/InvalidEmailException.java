package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidEmailException extends AbstractKundeServiceException {

	private static final long serialVersionUID = 3638520573628599264L;
	private final String email;
	private final Collection<ConstraintViolation<Kunde>> violations;

	public InvalidEmailException(String pMail,
			Collection<ConstraintViolation<Kunde>> pVio) {
		super("Ungueltige Email: " + pMail + ", Violations: " + pVio);
		this.email = pMail;
		this.violations = pVio;
	}

	public String getEmail() {
		return email;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
