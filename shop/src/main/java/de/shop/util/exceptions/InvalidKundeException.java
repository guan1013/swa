package de.shop.util.exceptions;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidKundeException extends KundeValidationException {
	private static final long serialVersionUID = 4255133082483647701L;

	private Integer id;
	private String nachname;
	private String vorname;

	public InvalidKundeException(Kunde kunde,
			                     Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		if (kunde != null) {
			this.id = kunde.getKundeID();
			this.nachname = kunde.getNachname();
			this.vorname = kunde.getVorname();
		}
	}
	
	
	public InvalidKundeException(Integer id, Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.id = id;
	}
	
	public InvalidKundeException(String nachname, Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.nachname = nachname;
	}
	
	public Integer getId() {
		return id;
	}
	public String getNachname() {
		return nachname;
	}
	public String getVorname() {
		return vorname;
	}
	
	@Override
	public String toString() {
		return "{nachname=" + nachname + ", vorname=" + vorname + "}";
	}
}
