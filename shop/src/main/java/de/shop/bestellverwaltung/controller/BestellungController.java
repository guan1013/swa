package de.shop.bestellverwaltung.controller;

import static javax.ejb.TransactionAttributeType.REQUIRED;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Locale;

import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

import de.shop.auth.controller.AuthController;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.util.Client;
import de.shop.util.Log;
import de.shop.util.Messages;
import de.shop.util.Transactional;

@Named("bc")
@Log
@RequestScoped
public class BestellungController implements Serializable {
	
	// //////////////////////////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final long serialVersionUID = -4172696238266273075L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private Warenkorb warenkorb;
	@Inject
	private AuthController kunde;
	@Inject
	private BestellungService bs;
	@Inject
	private ProduktService ps;
	@Inject
	private KundeService ks;
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
	
	@Transactional
	public String bestellen() throws Exception {

		if (warenkorb.isEmpty()) {
			return null;
		}

		LOGGER.debugf("Neue Bestellung mit insgesamt %s Positionen",
				warenkorb.getSize());
		if (kunde.getUser() == null) throw new Exception("kein Kunde");
		final Bestellung bestellung = new Bestellung(warenkorb.getPositionen(), kunde.getUser());
		kunde.getUser().addBestellung(bestellung);
		ks.updateKunde(kunde.getUser(), locale, false);
		
//		bs.addBestellung(bestellung, locale);
//		bs.addBestellposten(bestellung);
		bestellung.setGesamtpreis(bestellung.errechneGesamtpreis());
		warenkorb.reset();
		return "/index";
	}
	
	public void warenkorbLeeren() {
		warenkorb.reset();
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
