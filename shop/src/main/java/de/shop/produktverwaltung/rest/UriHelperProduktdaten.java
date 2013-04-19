package de.shop.produktverwaltung.rest;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.util.Log;


@ApplicationScoped
@Log
public class UriHelperProduktdaten {
	public URI getUriProduktdaten(Produktdaten produktdaten, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(ProduktdatenResource.class)
		                             .path(ProduktdatenResource.class, "findProduktdatenById");
		final URI uri = ub.build(produktdaten.getProduktdatenID());
		return uri;
	}
}
