package de.shop.kundenverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.bestellverwaltung.rest.BestellungResource;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperKunde {
	public void updateUriKunde(Kunde kunde, UriInfo uriInfo) {
		// URL fuer Bestellungen setzen
		UriBuilder ub = uriInfo.getBaseUriBuilder().path(BestellungResource.class)
				.path(BestellungResource.class, "findBestellungenByKundeId");
		final URI bestellungenUri = ub.build(kunde.getKundeID());
		kunde.setBestellungenUri(bestellungenUri);

		// URL fuer Adresse setzen
		ub = uriInfo.getBaseUriBuilder().path(KundeResource.class)
				.path(KundeResource.class, "findAdressenByKundeId");
		final URI adressenUri = ub.build(kunde.getKundeID());
		kunde.setAdressenUri(adressenUri);

	}

	public URI getUriKunde(Kunde kunde, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
				.path(KundeResource.class)
				.path(KundeResource.class, "findKundeById");
		final URI kundeUri = ub.build(kunde.getKundeID());
		return kundeUri;
	}

	public URI getUriAdresse(Adresse adresse, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
				.path(AdresseResource.class)
				.path(AdresseResource.class, "findAdresseID");
		final URI uri = ub.build(adresse.getAdresseID());
		return uri;
	}
}
