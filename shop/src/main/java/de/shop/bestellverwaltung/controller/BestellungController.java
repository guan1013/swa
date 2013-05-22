package de.shop.bestellverwaltung.controller;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.util.Log;

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
	private BestellungService bestellungService;

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

		bestellungService.addBestellung(bestellung, null);
	}

}
