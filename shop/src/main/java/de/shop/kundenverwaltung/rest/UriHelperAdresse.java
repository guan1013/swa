package de.shop.kundenverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperAdresse {
	public URI getUriAdresse(Adresse adresse, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
				.path(AdresseResource.class)
				.path(AdresseResource.class, "findAdresseByID");
		final URI uri = ub.build(adresse.getAdresseID());
		return uri;
	}
}
