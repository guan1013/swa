package de.shop.kundenverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.AdresseService;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.NotFoundException;

/**
 * Resource Klasse für Kunde für die RestfullWebservices
 * 
 * @author Matthias Schnell
 */
@Path("/kunden")
@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Consumes
@RequestScoped
public class KundeResource {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	@Inject
	private KundeService ks;

	@Inject
	private AdresseService as;

	@Inject
	private UriHelperKunde uriHelperKunde;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response addKunde(Kunde pKD, @Context UriInfo uriInfo) {

		ks.addKunde(pKD, LOCALE_DEFAULT);

		final URI kdUri = uriHelperKunde.getUriKunde(pKD, uriInfo);
		return Response.created(kdUri).build();
	}

	@GET
	@Path("{kid:[1-9][0-9]*}")
	public Kunde findKundeById(@PathParam("kid") Integer pID,
			@Context UriInfo uriInfo) {
		Kunde kd = ks.findKundeById(pID, LOCALE_DEFAULT);
		if (kd == null) {
			final String msg = "Kein Kunde gefunden mit der ID" + pID;

			throw new NotFoundException(msg);
		}

		// URLs des gefundenen Kunden anpassen
		uriHelperKunde.updateUriKunde(kd, uriInfo);

		return kd;
	}

	@GET
	@Wrapped(element = "kunden")
	public List<Kunde> findKundenByNachname(@QueryParam("name") String pName,
			@Context UriInfo uriInfo) {
		List<Kunde> kd = null;
		if ("".equals(pName)) {
			kd = ks.findAllKunden();
			if (kd.isEmpty()) {
				final String msg = "Kein Kunde vorhanden";
				throw new NotFoundException(msg);
			}
		}
		else {
			kd = ks.findKundeByNachname(FetchType.JUST_KUNDE, pName,
					LOCALE_DEFAULT);
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
	public List<Adresse> findAdressenByKundeId(@PathParam("kid") Integer pKID,
			@Context UriInfo uriInfo) {
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
		for (Adresse a : ad) {
			kd = ks.findKundeById(a.getKunde().getKundeID(), LOCALE_DEFAULT);
			uriHelperKunde.updateUriKunde(kd, uriInfo);
		}

		return ad;
	}

	@PUT
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public void updateKunde(Kunde pKD, @Context UriInfo uriInfo) {

		// Vorhandenen Kunden suchen
		Kunde kd = ks.findKundeById(pKD.getKundeID(), LOCALE_DEFAULT);
		if (kd == null) {
			final String msg = "Kein Kunde mit der ID " + pKD.getKundeID()
					+ " gefunden";
			throw new NotFoundException(msg);
		}
		// Daten des vorhandenen Objekts überschreiben
		kd.setValues(pKD);

		// Objekt an die Datenbank übergeben
		pKD = ks.updateKunde(kd, LOCALE_DEFAULT);
		if (pKD == null) {
			final String msg = "Kein Kunde mit der ID " + kd.getKundeID()
					+ " gefunden.";
			throw new NotFoundException(msg);
		}
	}

}
