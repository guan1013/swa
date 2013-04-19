package de.shop.util.exceptions;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;


/**
 * Exception, die ausgeloest wird, wenn die Attributwerte einer Bestellung nicht
 * korrekt sind
 * 
 * @author Matthias Schnell
 */
public class BestellungValidationException extends AbstractKundeServiceException {
	
	private static final long serialVersionUID = 1704517030139784303L;
	private final Bestellung bestellung;
	private final Collection<ConstraintViolation<Bestellung>> violations;

	public BestellungValidationException(Bestellung pBD,
			Collection<ConstraintViolation<Bestellung>> pVio) {
		super("Ungueltigee Bestellung: " + pBD + ", Violations: " + pVio);
		this.bestellung = pBD;
		this.violations = pVio;
	}

	public Bestellung getKunde() {
		return bestellung;
	}

	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}

}
