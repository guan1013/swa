package de.shop.kundenverwaltung.rest;

import static de.shop.util.Constants.KEINE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

import org.jboss.logging.Logger;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.AdresseService;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.JsonFile;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.exceptions.NotFoundException;

/**
 * Resource Klasse für Kunde für die RestfullWebservices
 * 
 * @author Matthias Schnell
 */
@Path("/kunden")
@Produces({ APPLICATION_JSON })
@Consumes
@RequestScoped
@Log
public class KundeResource {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	@Context
	private HttpHeaders headers;

	@Context
	private UriInfo uriInfo;

	// INJECTS
	@Inject
	private LocaleHelper localeHelper;

	@Inject
	private KundeService ks;

	@Inject
	private AdresseService as;

	@Inject
	private UriHelperKunde uriHelperKunde;

	// LOGGER
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@POST
	@Consumes({ APPLICATION_JSON })
	@Produces
	public Response addKunde(Kunde pKD) {

		final Locale locale = localeHelper.getLocale(headers);

		pKD.setKundeID(KEINE_ID);
		final List<Adresse> ads = pKD.getAdressen();

		if (ads != null) {
			for (Adresse ad : ads) {
				ad.setKunde(pKD);
			}
		}

		pKD.setBestellungenUri(null);
		pKD.setPasswordWdh(pKD.getPassword());

		ks.addKunde(pKD, locale);
		LOGGER.trace(pKD);

		final URI kdUri = uriHelperKunde.getUriKunde(pKD, uriInfo);

		return Response.created(kdUri).build();
	}

	@Path("{id:[1-9][0-9]*}/pic")
	@POST
	@Consumes(APPLICATION_JSON)
	public Response uploadPicKunde(@PathParam("kid") Integer pKID, JsonFile pPic) {
		final Locale locale = localeHelper.getLocale(headers);

		ks.setKundePic(pKID, pPic.getBytes(), locale);

		final URI location = uriHelperKunde.getUriDownload(pKID, uriInfo);

		return Response.created(location).build();
	}

	@Path("{id:[1-9][0-9]*}/file")
	@GET
	public JsonFile downloadPicKunde(@PathParam("kid") Integer pKID)
			throws IOException {
		final Locale locale = localeHelper.getLocale(headers);
		final Kunde kd = ks.findKundeById(pKID, locale);
		if (kd.getPic() == null) {
			return new JsonFile(new byte[] {});
		}

		return new JsonFile(kd.getPic().getBytes());
	}

	@GET
	@Path("{kid:[1-9][0-9]*}")
	public Kunde findKundeById(@PathParam("kid") Integer pID) {

		Locale LOCALE = localeHelper.getLocale(headers);
		Kunde kd = ks.findKundeById(pID, LOCALE);
		if (kd == null) {
			final String msg = "Kein Kunde gefunden mit der ID" + pID;

			throw new NotFoundException(msg);
		}

		// URLs des gefundenen Kunden anpassen
		uriHelperKunde.updateUriKunde(kd, uriInfo);

		return kd;
	}

	/**
	 * Mittels /kunden alle Kunden finden bzw /kunden?nachname="pName" ein Kunde
	 * mit einem bestimmen Nachnamen
	 * 
	 * @param pName
	 * @param uriInfo
	 * @return
	 */
	@GET
	public List<Kunde> findAllKundenOrByNachname(
			@QueryParam("name") String pName) {

		List<Kunde> kd = null;
		if ("".equals(pName)) {
			kd = ks.findAllKunden();
			if (kd.isEmpty()) {
				final String msg = "Kein Kunde vorhanden";
				throw new NotFoundException(msg);
			}
		} else {
			Locale LOCALE = localeHelper.getLocale(headers);

			kd = ks.findKundeByNachname(FetchType.JUST_KUNDE, pName, LOCALE);
			if (kd.isEmpty()) {
				final String msg = "Kein Kunde mit dem Nachnamen " + pName
						+ " gefunden.";
				throw new NotFoundException(msg);
			}
		}

		// URLs innerhalb der gefundenen Kunden anpassen
		for (Kunde k : kd) {
			uriHelperKunde.updateUriKunde(k, uriInfo);
		}

		return kd;
	}

	@GET
	@Path("{kid:[1-9][0-9]*}/adressen")
	public List<Adresse> findAdressenByKundeId(@PathParam("kid") Integer pKID) {
		final List<Adresse> ad = as.findAdressenByKundeId(pKID);
		if (ad.isEmpty()) {
			final String msg = "Keine Adresse zum Kunde mit der ID " + pKID
					+ " gefunden";
			throw new NotFoundException(msg);
		}
		final int anz = ad.size();
		final List<Integer> adIds = new ArrayList<>(anz);
		for (Adresse a : ad) {
			adIds.add(a.getAdresseID());
		}

		// URLs der gefundenen Bestellungen anpassen
		Kunde kd;
		Locale LOCALE = localeHelper.getLocale(headers);
		for (Adresse a : ad) {

			kd = ks.findKundeById(a.getKunde().getKundeID(), LOCALE);
			uriHelperKunde.updateUriKunde(kd, uriInfo);
		}

		return ad;
	}

	@PUT
	@Consumes({ APPLICATION_JSON })
	@Produces
	public void updateKunde(Kunde pKD) {

		Locale LOCALE = localeHelper.getLocale(headers);

		// Vorhandenen Kunden suchen
		Kunde kd = ks.findKundeById(pKD.getKundeID(), LOCALE);
		if (kd == null) {
			final String msg = "Kein Kunde mit der ID " + pKD.getKundeID()
					+ " gefunden";
			throw new NotFoundException(msg);
		}
		LOGGER.tracef("Kunde vorher = %s", kd);
		// Daten des vorhandenen Objekts überschreiben
		kd.setValues(pKD);
		LOGGER.tracef("Kunde nachher = %s", kd);

		// Objekt an die Datenbank übergeben
		pKD = ks.updateKunde(kd, LOCALE, false);
		if (pKD == null) {
			final String msg = "Kein Kunde mit der ID " + kd.getKundeID()
					+ " gefunden.";
			throw new NotFoundException(msg);
		}
	}

	@Path("{id:[1-9][0-9]*}")
	@DELETE
	@Produces
	public void deleteKunde(@PathParam("kid") Integer pKID) {

		Locale LOCALE = localeHelper.getLocale(headers);
		ks.deleteKundeById(pKID, LOCALE);
	}

}
