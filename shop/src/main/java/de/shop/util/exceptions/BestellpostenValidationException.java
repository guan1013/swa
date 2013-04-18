package de.shop.util.exceptions;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposten;

/**
 * Exception, die ausgeloest wird, wenn die Attributwerte eines Bestellpostens nicht
 * korrekt sind
 * 
 * @author Dennis Brull
 */

public class BestellpostenValidationException extends RuntimeException {


	private static final long serialVersionUID = -227473892514873330L;
	
	private final Bestellposten bestellposten;
	private final Collection<ConstraintViolation<Bestellposten>> violations;

	public BestellpostenValidationException(Bestellposten bp,
			Collection<ConstraintViolation<Bestellposten>> vio) {
		super("Ungueltiger Bestellposten: " + bp + ", Violations: " + vio);
		this.bestellposten = bp;
		this.violations = vio;
	}
	
	

	public BestellpostenValidationException(String msg) {
		super(msg);
		violations = null;
		bestellposten = null;
	}



	public Bestellposten getBestellposten() {
		return bestellposten;
	}

	public Collection<ConstraintViolation<Bestellposten>> getViolations() {
		return violations;
	}
}
