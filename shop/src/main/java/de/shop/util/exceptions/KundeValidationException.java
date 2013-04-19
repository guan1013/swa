package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

/**
 * Exception, die ausgeloest wird, wenn die Attributwerte eines Kunden nicht
 * korrekt sind
 * 
 * @author Matthias Schnell
 */

@ApplicationException(rollback = true)
public class KundeValidationException extends AbstractKundeServiceException {

	private static final long serialVersionUID = -9184767805417976154L;

	private final Kunde kunde;
	private final Collection<ConstraintViolation<Kunde>> violations;

	public KundeValidationException(Kunde pKD,
			Collection<ConstraintViolation<Kunde>> pVio) {
		super("Ungueltiger Kunde: " + pKD + ", Violations: " + pVio);
		this.kunde = pKD;
		this.violations = pVio;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
