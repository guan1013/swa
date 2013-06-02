package de.shop.bestellverwaltung.controller;

import static de.shop.util.Messages.MessagesType.KUNDENVERWALTUNG;
import static javax.ejb.TransactionAttributeType.REQUIRED;

import java.lang.invoke.MethodHandles;
import java.util.Locale;

import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.controller.KundeController;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktService.FetchType;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.util.Client;
import de.shop.util.Log;
import de.shop.util.Messages;
import de.shop.util.Transactional;

@Named("bc")
@Log
@RequestScoped
public class BestellungController {

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private Warenkorb warenkorb;
	
	
	@Inject
	private KundeController kc;
	
	@Inject
	private BestellungService bs;
	@Inject
	private ProduktService ps;
	@Inject
	private transient HttpServletRequest request;
	
	@Inject
	private Messages messages;
	
	@Inject
	@Client
	private Locale locale;
	
	private Integer bestellungId;
	private Bestellung bestellungSearch;
	
	private static final String ERROR = "funzt ned";
	private static final String JSF_BESTELLVERWALTUNG = "/bestellverwaltung/";
	private static final String JSF_VIEW_BESTELLUNG = JSF_BESTELLVERWALTUNG
			+ "viewKunde";
	private static final String MSG_KEY_BESTELLUNG_NOT_FOUND_BY_ID = "viewBestellung.notFound";
	private static final String CLIENT_ID_BESTELLUNGID = "form:bestellungIdInput";

	private SuchFilter suchFilter;
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public void bestellen() {

		if (warenkorb.isEmpty()) {
			return;
		}

		LOGGER.debugf("Neue Bestellung mit insgesamt %s Positionen",
				warenkorb.getSize());

		Bestellung bestellung = new Bestellung();
		for (Bestellposten p : warenkorb.getPositionen()) {
			bestellung.addBestellposten(p);
		}
		Kunde k = kc.getKunde();
		k.addBestellung(bestellung);
		bs.addBestellung(bestellung, null);
	}
	
	public void warenkorbLeeren() {
		warenkorb = null;
	}
	
	@TransactionAttribute(REQUIRED)
	public String sucheById() {
		// Bestellungen werden durch "Extended Persistence Context" nachgeladen
		//bestellung = bs.findBestellungById(bestellungId, locale);

		bestellungSearch = bs.findBestellungById(bestellungId, locale);
		
		if (bestellungSearch == null) {
			// Kein Kunde zu gegebener ID gefunden
			return ERROR;
		}

		return JSF_VIEW_BESTELLUNG;
	}
	
	@Transactional
	public void createEmptySuchfilter() {

		suchFilter = new SuchFilter();
	}

	public Integer getBestellungId() {
		return bestellungId;
	}

	public void setBestellungId(Integer bestellungId) {
		this.bestellungId = bestellungId;
	}

	public Bestellung getBestellungSearch() {
		return bestellungSearch;
	}

	public void setBestellungSearch(Bestellung bestellungSearch) {
		this.bestellungSearch = bestellungSearch;
	}

	public Warenkorb getWarenkorb() {
		return warenkorb;
	}

	public void setWarenkorb(Warenkorb warenkorb) {
		this.warenkorb = warenkorb;
	}

}
