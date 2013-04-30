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
import de.shop.produktverwaltung.service.ProduktService.FetchType;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.util.Log;
import de.shop.util.Transactional;
import de.shop.util.exceptions.NotFoundException;

//@formatter:off
@Path("/produktdaten")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
// @formatter:on
public class ProduktdatenResource {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private ProduktdatenService produktdatenService;

	@Inject
	private ProduktService produktService;

	@Inject
	private UriHelperProduktdaten uriHelperProduktdaten;

	// formatter:off
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	// formatter:on
	public Response addProduktdaten(Produktdaten produktdaten,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Neue Produktdaten: {0}", produktdaten);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Produkt nachladen
		final Produkt currProdukt = produktService.findProduktByID(produktdaten
				.getProdukt().getProduktID(), FetchType.NUR_PRODUKTE, locale);
		produktdaten.setProdukt(currProdukt);

		produktdaten = produktdatenService
				.addProduktdaten(produktdaten, locale);

		// Log
		LOGGER.log(FINER, "REST ENDE: Neue Produktdaten: {0}", produktdaten);

		final URI artikelUri = uriHelperProduktdaten.getUriProduktdaten(
				produktdaten, uriInfo);
		return Response.created(artikelUri).build();

	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Produktdaten findProduktdatenById(@PathParam("id") Integer id,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Finde Produktdaten by ID = {0}", id);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Service aufrufen
		final Produktdaten result = produktdatenService.findProduktdatenByID(id,
				locale);

		// Ggf. Fehlermeldung
		if (result == null) {
			throw new NotFoundException("Keine Produktdaten mit id = " + id
					+ " gefunden!");
		}

		// Log
		LOGGER.log(FINER,
				"REST ENDE: Finde Produktdaten by Id. Ergebnis = {0}", result);

		return result;
	}

	@GET
	@Wrapped(element = "produktdaten")
	public Collection<Produktdaten> findProduktdatenByDetailSuche(
			@QueryParam("farbe") String farbe,
			@QueryParam("anzahl") int anzahl,
			@QueryParam("beschreibung") String beschreibung,
			@QueryParam("groesse") String groesse,
			@QueryParam("hersteller") String hersteller,
			@QueryParam("preis_unten") double preisUnten,
			@QueryParam("preis_oben") double preisOben,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		final SuchFilter filter = new SuchFilter();
		filter.setFarbe(farbe);
		filter.setAnzahl(anzahl == 0 ? null : anzahl);
		filter.setBeschreibung(beschreibung);
		filter.setGroesse(groesse);
		filter.setHersteller(hersteller);
		filter.setPreisOben(preisOben == 0 ? null : preisOben);
		filter.setPreisUnten(preisUnten == 0 ? null : preisUnten);

		// Log
		LOGGER.log(
				FINER,
				"REST BEGINN: Finde Produktdaten by Detail Suche (Filter = {0})",
				filter);

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Service aufrufen
		final List<Produktdaten> results = produktdatenService
				.findProduktdatenByFilter(filter, locale);

		// Ggf. Fehlermeldung
		if (results.isEmpty()) {

			throw new NotFoundException(
					"Keine passenden Produktdaten gefunden!");
		}

		// Log
		LOGGER.log(FINER,
				"REST ENDE: Finde Produktdaten by Detail Suche. Gefunden: {0}",
				results.size());

		return results;
	}

	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	public void updateProduktdaten(Produktdaten produktdaten,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: Update Produktdaten = {0}",
				produktdaten);

		// Ggf. Exception
		if (produktdaten == null) {
			throw new NotFoundException("Nichts zu updaten");
		}

		// Locale auswählen
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales
				.get(0);

		// Service aufrufen
		produktdatenService.updateProduktdaten(produktdaten, locale);

		// Log
		LOGGER.log(FINER, "REST ENDE: Update Produkte = {0}", produktdaten);

	}
}
