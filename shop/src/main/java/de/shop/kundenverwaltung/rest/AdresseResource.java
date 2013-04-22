package de.shop.kundenverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
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

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.service.AdresseService;
import de.shop.kundenverwaltung.service.AdresseService.FetchType;
import de.shop.util.LocaleHelper;
import de.shop.util.NotFoundException;

@Path("/adressen")
@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Consumes
@RequestScoped
public class AdresseResource {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());
	@Context
	private HttpHeaders headers;

	@Inject
	private LocaleHelper localeHelper;

	@Inject
	private AdresseService as;

	@Inject
	private UriHelperAdresse uriHelperAdresse;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response addAdresse(Adresse adresse, @Context UriInfo uriInfo) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: addAdresse", adresse);

		// Locale
		Locale LOCALE = localeHelper.getLocale(headers);

		// Service aufrufen
		adresse = as.addAdresse(adresse, LOCALE);

		final URI adresseURI = uriHelperAdresse.getUriAdresse(adresse, uriInfo);

		// Log
		LOGGER.log(FINER, "REST END: addAdresse", adresse);

		return Response.created(adresseURI).build();

	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Adresse findAdresseByID(@PathParam("id") Integer id) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: findAdresse By ID={0}", id);

		// Locale
		Locale LOCALE = localeHelper.getLocale(headers);

		// Service aufrufen und ggf. Exception
		Adresse result = as.findAdresseByAdresseID(id, LOCALE);
		if (result == null) {
			final String msg = "Keine Adresse mit der ID " + id + " gefunden";

			throw new NotFoundException(msg);
		}

		// Log
		LOGGER.log(FINER, "REST END: findAdresseByID", id);

		return result;
	}

	@GET
	@Wrapped(element = "adressen")
	@Path("/plz")
	public Collection<Adresse> findAdresseByPLZ(@QueryParam("plz") int plz) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: findAdresse By PLZ={0}", plz);

		// Locale
		Locale LOCALE = localeHelper.getLocale(headers);

		// Service aufrufen und ggf. Exception
		Collection<Adresse> results = as.findAdresseByPLZ(FetchType.MIT_KUNDE,
				plz, LOCALE);

		if (results.isEmpty()) {
			final String msg = "Keine Adresse mit der PLZ " + plz + " gefunden";
			throw new NotFoundException(msg);
		}

		// Log
		LOGGER.log(FINER, "REST END: findAdresseByPLZ", plz);

		return results;
	}

	@GET
	@Wrapped(element = "adressen")
	@Path("/strasse")
	public Collection<Adresse> findAdresseByStrasse(
			@QueryParam("strasse") String strasse) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: findAdresse By Strasse={0}", strasse);

		Collection<Adresse> results = null;

		// Service aufrufen und ggf. Exception
		if ("".equals(strasse)) {
			results = as.findAllAdressen();
			if (results.isEmpty()) {
				final String msg = "Keine Adresse vorhanden";
				throw new NotFoundException(msg);
			}
		} else {

			// Locale
			Locale LOCALE = localeHelper.getLocale(headers);
			results = as.findAdresseByStrasse(FetchType.MIT_KUNDE, strasse,
					LOCALE);
			if (results.isEmpty()) {
				final String msg = "Keine Adresse mit der Strasse " + strasse
						+ " gefunden";
				throw new NotFoundException(msg);
			}
		}

		// Log
		LOGGER.log(FINER, "REST END: findAdresseByStrasse", strasse);

		return results;
	}

	@GET
	@Wrapped(element = "adressen")
	@Path("/ort")
	public Collection<Adresse> findAdresseByOrt(@QueryParam("ort") String ort) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: findAdresse By Ort={0}", ort);

		Collection<Adresse> results = null;

		// Service aufrufen und ggf. Exception
		if ("".equals(ort)) {
			results = as.findAllAdressen();
			if (results.isEmpty()) {
				final String msg = "Keine Adresse vorhanden";
				throw new NotFoundException(msg);
			}
		} else {
			// Locale
			Locale LOCALE = localeHelper.getLocale(headers);

			results = as.findAdresseByOrt(FetchType.MIT_KUNDE, ort, LOCALE);
			if (results.isEmpty()) {
				final String msg = "Keine Adresse mit dem Ort " + ort
						+ " gefunden";
				throw new NotFoundException(msg);
			}
		}

		// Log
		LOGGER.log(FINER, "REST END: findAdresseByOrt", ort);

		return results;
	}

	@PUT
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response updateAdresse(Adresse adresse, @Context UriInfo uriInfo) {

		// Log
		LOGGER.log(FINER, "REST BEGINN: updateAdresse", adresse);

		// Locale
		Locale LOCALE = localeHelper.getLocale(headers);

		// GGf. Exception
		if (adresse == null) {
			final String msg = "Keine Adresse übergeben";
			throw new NotFoundException(msg);
		}

		// Service aufrufen
		adresse = as.updateAdresse(adresse, LOCALE);

		final URI adresseUri = uriHelperAdresse.getUriAdresse(adresse, uriInfo);

		LOGGER.log(FINER, "REST END: updateAdresse", adresse);

		return Response.created(adresseUri).build();
	}
}
