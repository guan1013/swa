package de.shop.bestellverwaltung.rest;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.service.BestellpostenService;
import de.shop.bestellverwaltung.service.BestellpostenService.FetchType;
import de.shop.util.exceptions.NotFoundException;

@Path("/bestellposten")
@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Consumes
@RequestScoped
public class BestellpostenResource {

	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());
	
	@Inject
	private BestellpostenService bps;
	
	///////////////////////////////////////////////////////////////////////
	// METHODS
	
	/**
		Lege einen Bestellposten an
	 */
	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public void addBestellposten(Bestellposten bestellposten, @Context UriInfo uriInfo) {
	
		LOGGER.log(FINER, "BEGINN: Bestellposten Anlegen");
		
		bps.addBestellposten(bestellposten, LOCALE_DEFAULT);
		
		LOGGER.log(FINER, "ENDE: Bestellposten Anlegen");	
	}
	
	/**
	Verändere einen Bestellposten
	 */
	@PUT
	@Consumes({ APPLICATION_XML, TEXT_XML })
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

}
