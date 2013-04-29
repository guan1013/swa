package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellpostenService;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.bestellverwaltung.service.BestellungService.FetchType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.exceptions.NotFoundException;
import de.shop.util.Transactional;

/**
 * Resource Klasse für Bestellung für die RestfullWebservices
 * 
 * @author Matthias Schnell
 */
@Path("/bestellung")
@Produces({ APPLICATION_JSON })
@Consumes
@RequestScoped
@Transactional
@Log
public class BestellungResource {
	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	@Context
	private HttpHeaders headers;

	@Context
	private UriInfo uriInfo;

	// INJECTS
	@Inject
	private LocaleHelper localeHelper;
	
	
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private BestellungService bs;

	@Inject
	private BestellpostenService bps;
	
	@Inject
	private KundeService ks;

	@Inject
	private UriHelperBestellung uriHelperBestellung;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

//	@POST
//	@Consumes({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
//	@Produces
//	public Response addBestellung(Bestellung pBE) {
//		Locale LOCALE_DEFAULT = localeHelper.getLocale(headers);
//		bs.addBestellung(pBE, LOCALE_DEFAULT);
//		final URI beUri = uriHelperBestellung.getUriBestellung(pBE, uriInfo);
//		return Response.created(beUri).build();
//	}
	@POST
	@Consumes(APPLICATION_JSON)
	public Response createBestellung(Bestellung bestellung) {
		// Schluessel des Kunden extrahieren
		final String kundeUriStr = bestellung.getKundeUri().toString();
		int startPos = kundeUriStr.lastIndexOf('/') + 1;
		final String kundeIdStr = kundeUriStr.substring(startPos);
		Integer kundeId = null;
		try {
			kundeId = Integer.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr, e);
		}
		Locale LOCALE_DEFAULT = localeHelper.getLocale(headers);
		Kunde k = ks.findKundeById(kundeId, LOCALE_DEFAULT);
		
		if(k == null)
		{
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr);
		}
		bestellung.setKunde(k);
		
//		// persistente Artikel ermitteln
//		final Collection<Bestellposten> bestellpositionen = bestellung.getBestellposten();
//		final List<Long> artikelIds = new ArrayList<>(bestellpositionen.size());
//		for (Bestellposten bp : bestellpositionen) {
//			final String artikelUriStr = bp.getBestellpostenUri().toString();
//			startPos = artikelUriStr.lastIndexOf('/') + 1;
//			final String artikelIdStr = artikelUriStr.substring(startPos);
//			Long artikelId = null;
//			try {
//				artikelId = Long.valueOf(artikelIdStr);
//			}
//			catch (NumberFormatException e) {
//				// Ungueltige Artikel-ID: wird nicht beruecksichtigt
//				continue;
//			}
//			
//			artikelIds.add(artikelId);
//		}
//		
//		if (artikelIds.isEmpty()) {
//			// keine einzige gueltige Artikel-ID
//			final StringBuilder sb = new StringBuilder("Keine Artikel vorhanden mit den IDs: ");
//			for (Bestellposition bp : bestellpositionen) {
//				final String artikelUriStr = bp.getArtikelUri().toString();
//				startPos = artikelUriStr.lastIndexOf('/') + 1;
//				sb.append(artikelUriStr.substring(startPos));
//				sb.append(' ');
//			}
//			throw new NotFoundException(sb.toString());
//		}
//		
//		final List<Artikel> gefundeneArtikel = as.findArtikelByIds(artikelIds);
//		if (gefundeneArtikel.isEmpty()) {
//			// TODO msg passend zu locale
//			throw new NotFoundException("Keine Artikel gefunden mit den IDs " + artikelIds);
//		}
//		
//		// Bestellpositionen haben URIs fuer persistente Artikel.
//		// Diese persistenten Artikel wurden in einem DB-Zugriff ermittelt (s.o.)
//		// Fuer jede Bestellposition wird der Artikel passend zur Artikel-URL bzw. Artikel-ID gesetzt.
//		// Bestellpositionen mit nicht-gefundene Artikel werden eliminiert.
//		int i = 0;
//		final List<Bestellposition> neueBestellpositionen =
//			                        new ArrayList<>(bestellpositionen.size());
//		for (Bestellposition bp : bestellpositionen) {
//			// Artikel-ID der aktuellen Bestellposition (s.o.):
//			// artikelIds haben gleiche Reihenfolge wie bestellpositionen
//			final long artikelId = artikelIds.get(i++);
//			
//			// Wurde der Artikel beim DB-Zugriff gefunden?
//			for (Artikel artikel : gefundeneArtikel) {
//				if (artikel.getId().longValue() == artikelId) {
//					// Der Artikel wurde gefunden
//					bp.setArtikel(artikel);
//					neueBestellpositionen.add(bp);
//					break;					
//				}
//			}
//		}
//		bestellung.setBestellpositionen(neueBestellpositionen);
//		
//		// Kunde mit den vorhandenen ("alten") Bestellungen ermitteln
		final Locale locale = localeHelper.getLocale(headers);
		bestellung = bs.addBestellung(bestellung, locale);
		final URI bestellungUri = uriHelperBestellung.getUriBestellung(bestellung, uriInfo);
		//LOGGER.trace(bestellungUri);
		
		final Response response = Response.created(bestellungUri).build();
		return response;
	}

	@Path("{id:[1-9][0-9]*}")
	@DELETE
	@Produces
	public void deleteBestellung(@PathParam("id") Integer pKID) {

		Locale LOCALE = localeHelper.getLocale(headers);
		bs.deleteBestellungById(pKID, LOCALE);
	}
	
	@GET
	@Wrapped(element = "bestellungen")
	public List<Bestellung> findAllBestellung() {
		LOGGER.log(FINER, "REST BEGINN: findAllBestellung");

		List<Bestellung> be = bs.findAllBestellungen();
		if (be.isEmpty()) {
			final String msg = "Kein Bestellung vorhanden";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der gefundenen Bestellung anpassen

		for (Bestellung b : be) {
			uriHelperBestellung.updateUriBestellung(b, uriInfo);
		}

		LOGGER.log(FINER, "REST END: findAllBestellung with be={0}", be);

		return be;

	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Bestellung findBestellungById(@PathParam("id") Integer pID) {
		Locale LOCALE_DEFAULT = localeHelper.getLocale(headers);
		Bestellung be = bs.findBestellungById(pID, LOCALE_DEFAULT);
		if (be == null) {
			final String msg = "Kein Bestellung gefunden mit der ID" + pID;

			throw new NotFoundException(msg);
		}

		// URLs innerhalb der gefundenen Bestellung anpassen
		uriHelperBestellung.updateUriBestellung(be, uriInfo);

		return be;
	}

	@GET
	public List<Bestellung> findBestellungenByPreisspanne(
			@QueryParam("min") Double pMin, @QueryParam("max") Double pMax,
			@Context UriInfo uriInfo) {
		List<Bestellung> be = null;
		Locale LOCALE_DEFAULT = localeHelper.getLocale(headers);
		be = bs.findBestellungByPreisspanne(FetchType.JUST_BESTELLUNG, pMin,
				pMax, LOCALE_DEFAULT);
		if (be.isEmpty()) {
			final String msg = "Keine Bestellung mit einer Preisspanne von "
					+ pMin + " bis " + pMax + " gefunden.";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der gefundenen Bestellungen anpassen

		for (Bestellung b : be) {
			uriHelperBestellung.updateUriBestellung(b, uriInfo);
		}

		return be;
	}

	@GET
	@Path("{id:[1-9][0-9]*}/kunde")
	public List<Bestellung> findBestellungenByKundeId(
			@PathParam("kid") Integer pKID, @Context UriInfo uriInfo) {

		LOGGER.log(FINER, "REST BEGINN: FindBestellungen By KundeId={0}", pKID);

		final List<Bestellung> be = bs.findBestellungenByKundeId(pKID);

		if (be == null || be.isEmpty()) {
			final String msg = "Keine Bestellung zum Kunde mit der ID " + pKID
					+ " gefunden";
			throw new NotFoundException(msg);
		}

		// URLs innerhalb der gefundenen Bestellungen anpassen

		for (Bestellung b : be) {
			uriHelperBestellung.updateUriBestellung(b, uriInfo);
		}

		LOGGER.log(FINER,
				"REST BEGINN: FindBestellungen By KundeId. Gefunden: {0}",
				be.size());

		return be;
	}

	@GET
	@Path("{bestellungFk:[1-9][0-9]*}/bestellposten")
	public List<Bestellposten> findBestellpostenByBestellungId(
			@PathParam("bestellungFk") Integer bestellungFk,
			@Context UriInfo uriInfo) {
		Locale LOCALE_DEFAULT = localeHelper.getLocale(headers);
		List<Bestellposten> bestellposten = null;

		bestellposten = bps
				.findBestellpostenByBestellungId(
						de.shop.bestellverwaltung.service.BestellpostenService.FetchType.JUST_BESTELLPOSTEN,
						bestellungFk, LOCALE_DEFAULT);
		if (bestellposten == null) {

			throw new NotFoundException("Keine Bestellung gefunden mit ID "
					+ bestellungFk);
		}
		Bestellung best;
		for (Bestellposten b : bestellposten) {
			best = bs.findBestellungById(b.getBestellung().getBestellungID(),
					LOCALE_DEFAULT);
			uriHelperBestellung.updateUriBestellung(best, uriInfo);
		}

		return bestellposten;
	}

}