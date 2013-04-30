package de.shop.util.exceptions;

import java.util.Collection;
import javax.validation.ConstraintViolation;
import de.shop.bestellverwaltung.domain.Bestellposten;

public class InvalidAnzahlException extends AbstractBestellpostenServiceException {

	private static final long serialVersionUID = -4909117835363125609L;
	private final int anzahl;
	private final Collection<ConstraintViolation<Bestellposten>> violations;
	
	public InvalidAnzahlException(int anz,
			Collection<ConstraintViolation<Bestellposten>> vio) {
		super("Ungueltige Bestellpostenanzahl: " + anz + ", Violations: " + vio);
		
	this.anzahl = anz;
	this.violations = vio;
	}
	
	public int getAnzahl() {
		return anzahl;
	}

	public Collection<ConstraintViolation<Bestellposten>> getViolations() {
		return violations;
	}
}
