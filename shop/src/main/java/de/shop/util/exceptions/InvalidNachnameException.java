package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidNachnameException extends AbstractKundeServiceException {

	private static final long serialVersionUID = -4741807657588270177L;
	private final String nachname;
	private final Collection<ConstraintViolation<Kunde>> violations;

	public InvalidNachnameException(String pName,
			Collection<ConstraintViolation<Kunde>> pVio) {
		super("Ungueltiger Nachname: " + pName + ", Violations: " + pVio);
		
		this.nachname = pName;
		this.violations = pVio;
	}

	public String getNachname() {
		return nachname;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}

	public InvalidNachnameException(String msg) {
		super(msg);
		this.nachname = "";
		this.violations = null;
	}
	
	
}
