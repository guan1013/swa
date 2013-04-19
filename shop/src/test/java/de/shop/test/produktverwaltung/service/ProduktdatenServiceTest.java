package de.shop.test.produktverwaltung.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktService.FetchType;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.test.util.AbstractDomainTest;
import de.shop.util.exceptions.ProduktdatenValidationException;

/**
 * Die Klasse ProduktdatenServiceTest testet die Funktionalität der Klasse
 * ProduktdatenService. Die in Produktdatenservice implementierten Use-Cases
 * werden durch folgende Test-Cases getestet:
 * 
 * # Use-Case: Suche nach Produktdaten mit Filter<br>
 * # Test-Cases: Suche nach Produkdaten mit unterschiedlichen Einstellungen<br>
 * 
 * @see ProdukdatenService
 * @see Produktdaten
 * @author Andreas Güntzel
 * 
 */
@RunWith(Arquillian.class)
public class ProduktdatenServiceTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final int ANZAHL_ALLER_PRODUKTDATEN = 21;

	private static final int TEST_ANZAHL = 5;

	private static final double[] TEST_PREISSPANNE = new double[] {20d, 100d};

	private static final String TEST_FARBE = "blau";

	private static final String TEST_GROESSE = "42";

	private static final String TEST_HERSTELLER = "brAn";

	private static final String TEST_BESCHREIBUNG = "hose";

	private static final double TEST_PREIS = 13.00d;

	private static final Integer VORHANDENE_PRODUKT_ID = new Integer(313);

	private static final Locale LOCALE = new Locale("de");

	@Inject
	private ProduktdatenService produktdatenService;

	@Inject
	private ProduktService produktService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// /////////////////////////////////////////////////////////////////////
	// TEST-METHODS

	@Test
	public void testeAddValidProduktdaten() {

		// Neue Produktdaten anlegen
		Produktdaten neueProduktdaten = new Produktdaten();
		neueProduktdaten.setAnzahlVerfuegbar(TEST_ANZAHL);
		neueProduktdaten.setFarbe(TEST_FARBE);
		neueProduktdaten.setPreis(TEST_PREIS);
		neueProduktdaten.setProdukt(produktService.findProduktByID(
				VORHANDENE_PRODUKT_ID, FetchType.NUR_PRODUKTE, LOCALE));
		neueProduktdaten.setGroesse(TEST_GROESSE);

		// Service aufrufen
		produktdatenService.addProduktdaten(neueProduktdaten, LOCALE);

	}

	@Test
	public void testeAddNonValidProduktdaten() {

		// Neue Produktdaten anlegen
		Produktdaten neueProduktdaten = new Produktdaten();
		neueProduktdaten.setAnzahlVerfuegbar(TEST_ANZAHL);
		neueProduktdaten.setFarbe(TEST_FARBE);
		neueProduktdaten.setPreis(TEST_PREIS);
		neueProduktdaten.setProdukt(null);

		// Fehler erwarten
		thrown.expect(ProduktdatenValidationException.class);

		// Service aufrufen
		produktdatenService.addProduktdaten(neueProduktdaten, LOCALE);
	}

	@Test
	public void testeDetailProduktdatenSuche() {

		SuchFilter filter = new SuchFilter();

		// Keine Einschränkungen => alle Produktdaten müssen gefunden werden
		List<Produktdaten> liste = produktdatenService
				.findProduktdatenByFilter(filter, LOCALE);
		assertThat(liste.size(), is(ANZAHL_ALLER_PRODUKTDATEN));

		// Verfügbare Anzahl mindestens 5
		filter.setAnzahl(TEST_ANZAHL);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {
			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
		}

		// Verfügbare Anzahl mindestens 5, Preis zwischen 20 und 100
		filter.setPreisOben(TEST_PREISSPANNE[1]);
		filter.setPreisUnten(TEST_PREISSPANNE[0]);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {

			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
			assertThat(
					(pdaten.getPreis() >= TEST_PREISSPANNE[0] && pdaten
							.getPreis() <= TEST_PREISSPANNE[1]),
					is(true));
		}

		// Verfügbare Anzahl mindestens 5, Preis zwischen 20 und 100, Farbe blau
		filter.setFarbe(TEST_FARBE);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {

			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
			assertThat(
					(pdaten.getPreis() >= TEST_PREISSPANNE[0] && pdaten
							.getPreis() <= TEST_PREISSPANNE[1]),
					is(true));
			assertThat(
					pdaten.getFarbe().toLowerCase(LOCALE)
							.contains(TEST_FARBE.toLowerCase(LOCALE)), is(true));
		}

		// Verfügbare Anzahl mindestens 5, Preis zwischen 20 und 100, Farbe
		// blau, Größe 42
		filter.setGroesse(TEST_GROESSE);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {

			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
			assertThat(
					(pdaten.getPreis() >= TEST_PREISSPANNE[0] && pdaten
							.getPreis() <= TEST_PREISSPANNE[1]),
					is(true));
			assertThat(
					pdaten.getFarbe().toLowerCase(LOCALE)
							.contains(TEST_FARBE.toLowerCase(LOCALE)), is(true));
			assertThat(pdaten.getGroesse(), is(TEST_GROESSE));
		}

		// Verfügbare Anzahl mindestens 5, Preis zwischen 20 und 100, Farbe
		// blau, Größe 42, Hersteller (like) Bran
		filter.setHersteller(TEST_HERSTELLER);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {

			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
			assertThat(
					(pdaten.getPreis() >= TEST_PREISSPANNE[0] && pdaten
							.getPreis() <= TEST_PREISSPANNE[1]),
					is(true));
			assertThat(
					pdaten.getFarbe().toLowerCase(LOCALE)
							.contains(TEST_FARBE.toLowerCase(LOCALE)), is(true));
			assertThat(pdaten.getGroesse(), is(TEST_GROESSE));
			assertThat(pdaten.getProdukt().getHersteller().toLowerCase(LOCALE)
					.contains(TEST_HERSTELLER.toLowerCase(LOCALE)), is(true));
		}

		// Verfügbare Anzahl mindestens 5, Preis zwischen 20 und 100, Farbe
		// blau, Größe 42, Hersteller (like) Bran, Beschreibung (like) Hose
		filter.setBeschreibung(TEST_BESCHREIBUNG);
		liste = produktdatenService.findProduktdatenByFilter(filter, LOCALE);
		for (Produktdaten pdaten : liste) {

			assertThat(pdaten.getAnzahlVerfuegbar() >= TEST_ANZAHL, is(true));
			assertThat(
					(pdaten.getPreis() >= TEST_PREISSPANNE[0] && pdaten
							.getPreis() <= TEST_PREISSPANNE[1]),
					is(true));
			assertThat(
					pdaten.getFarbe().toLowerCase(LOCALE)
							.contains(TEST_FARBE.toLowerCase(LOCALE)), is(true));
			assertThat(pdaten.getGroesse(), is(TEST_GROESSE));
			assertThat(pdaten.getProdukt().getHersteller().toLowerCase(LOCALE)
					.contains(TEST_HERSTELLER.toLowerCase(LOCALE)), is(true));
			assertThat(pdaten.getProdukt().getBeschreibung().toLowerCase(LOCALE)
					.contains(TEST_BESCHREIBUNG.toLowerCase(LOCALE)), is(true));
		}

	}
}
