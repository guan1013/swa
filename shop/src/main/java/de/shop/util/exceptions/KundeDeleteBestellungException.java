package de.shop.util.exceptions;

import javax.ejb.ApplicationException;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class KundeDeleteBestellungException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -2220390878237172144L;
	private final Integer kundeId;
	private final int anzahlBestellungen;
	
	public KundeDeleteBestellungException(Kunde kunde) {
		super("Kunde mit ID=" + kunde.getKundeID() + " kann nicht geloescht werden: "
			  + kunde.getBestellungen().size() + " Bestellung(en)");
		this.kundeId = kunde.getKundeID();
		this.anzahlBestellungen = kunde.getBestellungen().size();
	}

	public Integer getKundeId() {
		return kundeId;
	}

	public int getAnzahlBestellungen() {
		return anzahlBestellungen;
	}
}
