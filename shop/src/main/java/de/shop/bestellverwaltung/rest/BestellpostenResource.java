package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.service.BestellpostenService;
import de.shop.bestellverwaltung.service.BestellpostenService.FetchType;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.exceptions.NotFoundException;

@Path("/bestellposten")
@Produces({ APPLICATION_JSON })
@Consumes
@RequestScoped
@Log
public class BestellpostenResource {

	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());
	
	@Context
	private HttpHeaders headers;
	
	@Inject
	private BestellpostenService bps;
	
	@Inject
	private LocaleHelper localeHelper;
	
	///////////////////////////////////////////////////////////////////////
	// METHODS
	
	/**
		Lege einen Bestellposten an
	 */
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	public void addBestellposten(Bestellposten bestellposten, @Context UriInfo uriInfo) {
	
		LOGGER.log(FINER, "BEGINN: Bestellposten Anlegen");
		
		bps.addBestellposten(bestellposten, LOCALE_DEFAULT);
		
		LOGGER.log(FINER, "ENDE: Bestellposten Anlegen");	
	}
	
	/**
	*Lösche einen Bestellposten
	*/

	@Path("{id:[1-9][0-9]*}")
	@DELETE
	@Produces
	public void deleteBestellposten(@PathParam("id") Integer id) {

		Locale LOCALE = localeHelper.getLocale(headers);
		bps.deleteBestellpostenById(id, LOCALE);
	}
	
	/**
	*Verändere einen Bestellposten
	*/
	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	public void updateBestellposten(Bestellposten bestellposten, @Context UriInfo uriInfo) {
		
		LOGGER.log(FINER, "BEGIN: Update Bestellposten mit ID {0}", bestellposten.getBestellpostenID());
		// Bestellposten nach id suchen
		Bestellposten updBestellposten = bps.findBestellpostenByIdObjekt(bestellposten.
				getBestellpostenID(), LOCALE_DEFAULT);
		
		// Wenn nicht gefunden - Fehlermeldung
		if (updBestellposten == null) {
			final String msg = "Kein Bestellposten mit ID " + bestellposten.
					getBestellpostenID() + " gefunden";
			throw new NotFoundException(msg);
		}
		
		//update durchführen
		updBestellposten.setValues(bestellposten);
		
		// Übergabe der Veränderungen an die Datenbank
		bestellposten = bps.updateBestellposten(updBestellposten, LOCALE_DEFAULT);
		
		LOGGER.log(FINER, "ENDE: Update Bestellposten {0}", bestellposten.getBestellpostenID());
		
	}
	
	/**
		Zeigt alle Bestellposten an
		@return Alle Bestellposten
	 */
	@GET
	@Wrapped(element = "bestellposten")
	public Collection<Bestellposten> findAlleBestellposten() {

		LOGGER.log(FINER, "Alle Bestellposten Anzeigen");
		
		Collection<Bestellposten> results = bps.findAllBestellposten();
		return results;
	}
	
	/**
		Suche nach Bestellposten nach ID des Bestellpostens
		@return genau 1 Bestellposten
	*/
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Bestellposten findBestellpostenById(@PathParam("id") Integer id) {
		
		LOGGER.log(FINER, "BEGINN: Suche nach Bestellposten mit ID {0}", id);
		
		Bestellposten bestellposten = bps.findBestellpostenByIdObjekt(id,
				LOCALE_DEFAULT);
		if (bestellposten == null) {
			
			throw new NotFoundException("Kein Bestellposten gefunden mit ID " + id);
		}
		LOGGER.log(FINER, "ENDE: Suche nach Bestellposten mit {0}", id);
		
		return bestellposten;
	}
	
	/**
	Suche nach Bestellposten nach ID der Bestellung
	@return Bestellposten, die zur Bestellung mit angegebener
	ID gehören
	*/
	@GET
	@Path("{bestellungFk:[1-9][0-9]*}/bestellung")
	public Collection <Bestellposten> findBestellpostenByBestellungId(
			@PathParam("bestellungFk") Integer bestellungFk) {

		Collection <Bestellposten> bestellposten = null;
		
		LOGGER.log(FINER, "BEGINN: Suche nach Bestellposten der Bestellung mit ID {0}", bestellungFk);
		
		bestellposten = bps.findBestellpostenByBestellungId(FetchType.JUST_BESTELLPOSTEN,
				bestellungFk, LOCALE_DEFAULT);
		if (bestellposten == null) {
			
			LOGGER.log(FINER, "Bestellung mit der ID {0} existiert nicht", bestellungFk);
			
			throw new NotFoundException("Keine Bestellung gefunden mit ID " + bestellungFk);
		}
		
		LOGGER.log(FINER, "ENDE: Suche nach Bestellposten der Bestellung mit ID {0}", bestellungFk);
		
		return bestellposten;
	}
	
	@PUT
	@Consumes({ APPLICATION_JSON })
	@Produces
	public void updateBestellposten(Bestellposten bP) {

		Locale LOCALE = localeHelper.getLocale(headers);

		// Vorhandenen Bestellposten suchen
		Bestellposten bp = bps.findBestellpostenByIdObjekt(bP.getBestellpostenID(), LOCALE);
		if (bp == null) {
			final String msg = "Kein Bestellpsoten mit der ID " + bP.getBestellpostenID()
					+ " gefunden";
			throw new NotFoundException(msg);
		}
		LOGGER.log(FINER,"Bestellposten vorher = {0}", bp);
		// Daten des vorhandenen Objekts überschreiben
		bp.setValues(bP);
		LOGGER.log(FINER,"Bestellposten nachher = {0}", bp);

		// Objekt an die Datenbank übergeben
		bP = bps.updateBestellposten(bp, LOCALE);
		if (bP == null) {
			final String msg = "Kein Bestellposten mit der ID " + bp.getBestellpostenID()
					+ " gefunden.";
			throw new NotFoundException(msg);
		}
	}

}
