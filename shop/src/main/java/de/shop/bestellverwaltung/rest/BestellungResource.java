package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import de.shop.util.Transactional;
import de.shop.util.exceptions.NotFoundException;

/**
 * Resource Klasse für Bestellung für die RestfullWebservices
 * 
 * @author Matthias Schnell
 */
@Path("/bestellung")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class BestellungResource {
	private static final int EXISTING_KUNDEN_ID = 101;

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

	
	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	public void updateBestellung(Bestellung bestellung) {

		final Locale locale = localeHelper.getLocale(headers);
//		final String kundeUriStr = bestellung.getKundeUri().toString();
//		int startPos = kundeUriStr.lastIndexOf('/') + 1;
//		final String kundeIdStr = kundeUriStr.substring(startPos);
//		Integer kundeId = null;
//		try {
//			kundeId = Integer.valueOf(kundeIdStr);
//		}
//		catch (NumberFormatException e) {
//			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr, e);
//		}
//		Locale localeDefault = localeHelper.getLocale(headers);
//		Kunde k = ks.findKundeById(kundeId, localeDefault);
//		
//		if(k == null)
//		{
//			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr);
//		}
//		bestellung.setKunde(k);
		

		// Vorhandenen Kunden suchen
		final Bestellung kd = bs.findBestellungById(bestellung.getBestellungID(), locale);
		bestellung.setKunde(ks.findKundeById(EXISTING_KUNDEN_ID, locale));
		
		
		
		
		//LOGGER.tracef("Kunde vorher = %s", kd);
		// Daten des vorhandenen Objekts überschreiben
		kd.setValues(bestellung);
		//LOGGER.tracef("Kunde nachher = %s", kd);

		// Objekt an die Datenbank übergeben
		bestellung = bs.updateBestellung(kd, locale);
		if (bestellung == null) {
			final String msg = "Keine Bestellung mit der ID " + kd.getBestellungID()
					+ " gefunden.";
			throw new NotFoundException(msg);
		}
	}
	
//	@POST
//	@Consumes({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
//	@Produces
//	public Response addBestellung(Bestellung pBE) {
//		Locale localeDefault = localeHelper.getLocale(headers);
//		bs.addBestellung(pBE, localeDefault);
//		final URI beUri = uriHelperBestellung.getUriBestellung(pBE, uriInfo);
//		return Response.created(beUri).build();
//	}
	@POST
	@Consumes(APPLICATION_JSON)
	public Response createBestellung(Bestellung bestellung) throws Exception {
		// Schluessel des Kunden extrahieren
		final String kundeUriStr = bestellung.getKundeUri().toString();
		final int startPos = kundeUriStr.lastIndexOf('/') + 1;
		final String kundeIdStr = kundeUriStr.substring(startPos);
		Integer kundeId = null;
		try {
			kundeId = Integer.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr, e);
		}
		final Locale localeDefault = localeHelper.getLocale(headers);
		final Kunde k = ks.findKundeById(kundeId, localeDefault);
		
		if (k == null) {
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr);
		}
		bestellung.setKunde(k);
		
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

		final Locale locale = localeHelper.getLocale(headers);
		bs.deleteBestellungById(pKID, locale);
	}
	
	@GET
	@Wrapped(element = "bestellungen")
	public List<Bestellung> findAllBestellung() {
		LOGGER.log(FINER, "REST BEGINN: findAllBestellung");

		final List<Bestellung> be = bs.findAllBestellungen();
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
		final Locale localeDefault = localeHelper.getLocale(headers);
		final Bestellung be = bs.findBestellungById(pID, localeDefault);
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
		final Locale localeDefault = localeHelper.getLocale(headers);
		be = bs.findBestellungByPreisspanne(FetchType.JUST_BESTELLUNG, pMin,
				pMax, localeDefault);
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
		final Locale localeDefault = localeHelper.getLocale(headers);
		List<Bestellposten> bestellposten = null;

		bestellposten = bps
				.findBestellpostenByBestellungId(
						de.shop.bestellverwaltung.service.BestellpostenService.FetchType.JUST_BESTELLPOSTEN,
						bestellungFk, localeDefault);
		if (bestellposten == null) {

			throw new NotFoundException("Keine Bestellung gefunden mit ID "
					+ bestellungFk);
		}
		Bestellung best;
		for (Bestellposten b : bestellposten) {
			best = bs.findBestellungById(b.getBestellung().getBestellungID(),
					localeDefault);
			uriHelperBestellung.updateUriBestellung(best, uriInfo);
		}

		return bestellposten;
	}

}
