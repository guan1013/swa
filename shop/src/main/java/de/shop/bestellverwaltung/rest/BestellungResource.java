package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellpostenService;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.bestellverwaltung.service.BestellungService.FetchType;
import de.shop.util.NotFoundException;

/**
 * Resource Klasse für Bestellung für die RestfullWebservices
 * 
 * @author Matthias Schnell
 */
@Path("/bestellung")
@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Consumes
@RequestScoped
public class BestellungResource {
	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Inject
	private BestellungService bs;

	@Inject
	private BestellpostenService bps;

	@Inject
	private UriHelperBestellung uriHelperBestellung;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response addBestellung(Bestellung pBE, @Context UriInfo uriInfo) {

		bs.addBestellung(pBE, LOCALE_DEFAULT);

		final URI beUri = uriHelperBestellung.getUriBestellung(pBE, uriInfo);
		return Response.created(beUri).build();
	}

	@GET
	@Wrapped(element = "bestellungen")
	public List<Bestellung> findAllBestellung(@Context UriInfo uriInfo) {
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
	public Bestellung findBestellungById(@PathParam("id") Integer pID,
			@Context UriInfo uriInfo) {

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
	@Path("{kid:[1-9][0-9]*}/kunde")
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