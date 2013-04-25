package de.shop.produktverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.util.exceptions.NotFoundException;

//@formatter:off
@Path("/produkte")
@Produces({ APPLICATION_JSON })
@Consumes
@RequestScoped
//@formatter:on
public class ProduktResource {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private ProduktService produktService;

	@Inject
	private ProduktdatenService produktdatenService;

	@Inject
	private UriHelperProdukt uriHelperProdukt;

	//formatter:off
	@POST
	@Consumes({ APPLICATION_JSON })
	@Produces
	public Response addProdukt(Produkt produkt, @Context UriInfo uriInfo,
			@Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Neues Produkt: {0}", produkt);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		produkt = produktService.addProdukt(produkt, locale);
		uriHelperProdukt.updateProduktdatenURI(produkt, uriInfo);

		// Log
		LOGGER.log(FINER, "REST ENDE: Neues Produkt:{0}", produkt);

		final URI artikelUri = uriHelperProdukt.getUriProdukt(produkt, uriInfo);
		return Response.created(artikelUri).build();

	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Produkt findProduktById(@PathParam("id") Integer id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST Beginn: Finde Produkt by ID={0}", id);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Service aufrufen
		Produkt produkt = produktService.findProduktByID(id,
				ProduktService.FetchType.NUR_PRODUKTE, locale);

		// Ggf. Fehlermeldung
		if (produkt == null) {
			throw new NotFoundException("Kein Produkt mit id=" + id
					+ " gefunden!");
		}

		// URI
		uriHelperProdukt.updateProduktdatenURI(produkt, uriInfo);

		// Log
		LOGGER.log(FINER, "REST Ende: Finde Produkt by Id. Ergebnis={0}",
				produkt);

		return produkt;

	}

	@GET
	@Wrapped(element = "produkte")
	@Path("/hersteller")
	public Collection<Produkt> findProduktByHersteller(
			@QueryParam("hersteller") String hersteller,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST Beginn: Finde Produkte by Hersteller={0}",
				hersteller);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		Collection<Produkt> results;

		if (hersteller == null || hersteller.equals("")) {
			// Log
			LOGGER.log(FINER, "Hersteller is null/empty => Alle Produkte");

			results = produktService.findProdukte();
		}
		else {
			results = produktService.findProduktByHersteller(hersteller,
					ProduktService.FetchType.KOMPLETT, locale);
		}

		// Ggf. Fehlermeldung
		if (results.isEmpty()) {
			throw new NotFoundException("Kein Produkte von Hersteller="
					+ hersteller + " gefunden!");
		}

		// URI
		for (Produkt produkt : results) {
			uriHelperProdukt.updateProduktdatenURI(produkt, uriInfo);
		}

		// Log
		LOGGER.log(FINER,
				"REST Ende: Finde Produkte by Hersteller (gefunden: {0})",
				results.size());

		return results;
	}

	@GET
	@Wrapped(element = "produkte")
	public Collection<Produkt> findProduktByBeschreibung(
			@QueryParam("beschreibung") String beschreibung,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST Beginn: Finde Produkte by Beschreibung={0}",
				beschreibung);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Service aufrufen
		Collection<Produkt> results = produktService.findProduktByBeschreibung(
				beschreibung, ProduktService.FetchType.KOMPLETT, locale);

		// Ggf. Fehlermeldung
		if (results.isEmpty()) {
			throw new NotFoundException("Kein Produkte mit Beschreibung="
					+ beschreibung + " gefunden!");
		}

		// URI
		for (Produkt produkt : results) {
			uriHelperProdukt.updateProduktdatenURI(produkt, uriInfo);
		}

		// Log
		LOGGER.log(FINER,
				"REST Ende: Finde Produkte by Beschreibung (gefunden: {0})",
				results.size());

		return results;
	}

	//formatter:off
	@PUT
	@Consumes({ APPLICATION_JSON })
	@Produces
	//formatter:on
	public void updateProdukt(Produkt produkt, @Context UriInfo uriInfo,
			@Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Update Produkte= {0}", produkt);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Produktdaten laden
		List<Produktdaten> pdaten = produktdatenService
				.findProduktdatenByProduktId(produkt.getProduktID());
		produkt.setProduktdaten(pdaten);

		// Service aufrufen
		produktService.updateProdukt(produkt, locale);

		// Log
		LOGGER.log(FINER, "REST ENDE: Update Produkte={0}", produkt);

	}

	@GET
	@Path("{id:[1-9][0-9]*}/produktdaten")
	public Collection<Produktdaten> findProduktdatenByProduktId(
			@PathParam("id") Integer id) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Find Produktdaten by ProduktID={0}", id);

		// Service aufrufen
		List<Produktdaten> results = produktdatenService
				.findProduktdatenByProduktId(id);

		if (results.isEmpty()) {
			throw new NotFoundException(
					"Dieses Produkt hat noch keine Produktdaten!");
		}

		// Log
		LOGGER.log(FINER,
				"REST ENDE: Find Produktdaten by ProduktID. Gefunden: {0}",
				results.size());

		return results;
	}
}
