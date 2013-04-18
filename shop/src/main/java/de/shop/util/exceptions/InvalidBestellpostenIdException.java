package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposten;

@ApplicationException(rollback = true)
public class InvalidBestellpostenIdException  extends AbstractKundeServiceException {

	private static final long serialVersionUID = 6553191730096904972L;
	private final Integer bestellpostenId;
	private final Collection<ConstraintViolation<Bestellposten>> violations;

	public InvalidBestellpostenIdException(Integer bpid,
			Collection<ConstraintViolation<Bestellposten>> vio) {
		super("Ungueltige Bestellposten-ID: " + bpid + ", Violations: " + vio);
		this.bestellpostenId = bpid;
		this.violations = vio;
	}

	public Integer getBestellpostenId() {
		return bestellpostenId;
	}

	public Collection<ConstraintViolation<Bestellposten>> getViolations() {
		return violations;
	}

}
