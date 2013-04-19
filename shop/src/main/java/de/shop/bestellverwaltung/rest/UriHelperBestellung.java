package de.shop.bestellverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperBestellung {

	public void updateUriBestellung(Bestellung bestellung, UriInfo uriInfo) {

		// URL fuer Bestellposten setzen

		UriBuilder ub = uriInfo
				.getBaseUriBuilder()
				.path(BestellungResource.class)
				.path(BestellungResource.class,
						"findBestellpostenByBestellungId");
		final URI bestellpostenUri = ub.build(bestellung.getBestellungID());
		bestellung.setBestellpostenUri(bestellpostenUri);
	}

	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
				.path(BestellungResource.class)
				.path(BestellungResource.class, "findBestellungById");
		final URI uri = ub.build(bestellung.getBestellungID());
		return uri;
	}

	public URI getUriBestellungpost(Bestellposten bestellposten, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
				.path(BestellpostenResource.class)
				.path(BestellungResource.class, "findBestellpostenById");
		final URI uri = ub.build(bestellposten.getBestellpostenID());
		return uri;
	}
}
