package de.shop.test.produktverwaltung.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logmanager.Level;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktService.FetchType;
import de.shop.test.util.AbstractDomainTest;
import de.shop.util.exceptions.ProduktValidationException;

/**
 * Die Klasse ProduktServiceTest testet die Funktionalität der Klasse
 * ProduktService. Die in ProduktService implementierten Use-Cases werden durch
 * folgenden Test-Cases getestet:<br>
 * 
 * # Use-Case: Suche Produkt nach ID<br>
 * # Test-Cases:<br>
 * - Suche nach gültiger ID<br>
 * - Suche nach ungültiger ID<br>
 * 
 * # Use-Case: Suche Produkt nach Hersteller<br>
 * # Test-Cases:<br>
 * - Suche nach gültigem Hersteller<br>
 * - Suche nach ungültigem Hersteller<br>
 * 
 * # Use-Case: Suche Produkt nach Beschreibung<br>
 * # Test-Cases:<br>
 * - Suche nach gültigem Produkt<br>
 * - Suche nach ungültigem Produkt<br>
 * 
 * # Use-Case: Hinzufügen Produkt<br>
 * # Test-Cases:<br>
 * - Hinzufügen gültiges Produkt<br>
 * - Hinzufügen ungültiges Produkt<br>
 * 
 * # Use-Case: Bearbeiten eines Produkts<br>
 * # Test-Cases:<br>
 * - Bearbeiten gültiges Produkt<br>
 * - Bearbeiten ungültiges Produkt<br>
 * 
 * @author Andreas Güntzel
 * @see Produkt
 * @see ProduktService
 * 
 */
@RunWith(Arquillian.class)
public class ProduktServiceTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final Locale LOCALE = new Locale("de");

	private static final ProduktService.FetchType FETCH_TYPE = ProduktService.FetchType.NUR_PRODUKTE;

	private static final Integer VORHANDENE_ID = new Integer(303);

	private static final Integer INVALID_ID = new Integer(-12);

	private static final Integer NICHT_VORHANDENE_ID = new Integer(4122);

	private static final String NICHT_VORHANDENER_HERSTELLER = "adidas";

	private static final String VORHANDENER_HERSTELLER = "MFH";

	private static final String INVALID_HERSTELLER = null;

	private static final String NEUER_HERSTELLER = "JUnitTestHersteller";

	private static final String INVALID_BESCHREIBUNG = null;

	private static final String NEUE_BESCHREIBUNG = "JUnitTestBeschreibung";

	private static final String NICHT_VORHANDENE_BESCHREIBUNG = "zzzyyyxxx";

	private static final String VORHANDENE_BESCHREIBUNG = "Hemd";

	private static final Integer VORHANDENE_PRODUKT_ID = new Integer(303);

	private static final Integer INVALID_PRODUKT_ID = null;

	@Inject
	private ProduktService produktService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@Test
	public void testeAddProdukt() {

		// Neues Produkt anlegen
		Produkt neuesProdukt = new Produkt();

		// Werte setzen
		neuesProdukt.setBeschreibung(NEUE_BESCHREIBUNG);
		neuesProdukt.setHersteller(NEUER_HERSTELLER);

		// Log
		LOGGER.log(Level.DEBUG, "TEST BEGINN: Teste AddProdukt Produkt={0}",
				neuesProdukt);

		// Service aufrufen
		produktService.addProdukt(neuesProdukt, LOCALE);

		// Log
		LOGGER.log(Level.DEBUG, "TEST ENDE: Teste AddProdukt Produkt={0}",
				neuesProdukt);

	}

	@Test
	public void testeAddNonValidProdukt() {

		// Neues Produkt anlegen
		Produkt neuesProdukt = new Produkt();

		// Werte setzen
		neuesProdukt.setBeschreibung(INVALID_BESCHREIBUNG);
		neuesProdukt.setHersteller(INVALID_HERSTELLER);

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste AddNonValidProdukt Produkt={0}",
				neuesProdukt);

		// Fehler erwarten
		thrown.expect(ProduktValidationException.class);

		// Service aufrufen
		produktService.addProdukt(neuesProdukt, LOCALE);

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste AddNonValidProdukt Produkt={0}", neuesProdukt);

	}

	@Test
	public void testeFindExistingProduktById() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktById ID={0}",
				VORHANDENE_ID);

		// Produkt auswählen
		Produkt result = produktService.findProduktByID(VORHANDENE_ID,
				FetchType.KOMPLETT, LOCALE);

		// Testen ob ID stimmt
		assertThat(result.getProduktID(), is(VORHANDENE_ID));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktById Produkt={0}", result);

	}

	@Test
	public void testeFindNonExistingProduktById() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktById ID={0}",
				NICHT_VORHANDENE_ID);

		// Produkt auswählen
		Produkt result = produktService.findProduktByID(NICHT_VORHANDENE_ID,
				FetchType.NUR_PRODUKTE, LOCALE);

		// Testen ob ID stimmt
		assertThat(result, is(nullValue()));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktById Produkt={0}",
				result);

	}

	@Test
	public void testeFindProduktByIdWithValidationError() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByIdWithValidationError ID={0}",
				INVALID_ID);

		// Fehler erwarten
		thrown.expect(ProduktValidationException.class);

		// Produkt mit ungültiger ID suchen
		Produkt result = produktService.findProduktByID(INVALID_ID,
				FetchType.KOMPLETT, LOCALE);

		// Testen ob wirklich kein Produkt gefunden wurde
		assertThat(result, is(nullValue()));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByIdWithValidationError Produkt={0}",
				result);

	}

	@Test
	public void testeFindExistingProduktByHersteller() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktByHersteller Hersteller={0}",
				VORHANDENER_HERSTELLER);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByHersteller(
				VORHANDENER_HERSTELLER, FETCH_TYPE, LOCALE);

		// Testen ob alle Produkte von diesem Hersteller sind
		for (Produkt produkt : results) {
			assertThat(produkt.getHersteller(), is(VORHANDENER_HERSTELLER));
		}

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktByHersteller Gefunden: {0}",
				results.size());

	}

	@Test
	public void testeFindNonExistingProduktByHersteller() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktByHersteller Hersteller={0}",
				NICHT_VORHANDENER_HERSTELLER);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByHersteller(
				NICHT_VORHANDENER_HERSTELLER, FetchType.KOMPLETT, LOCALE);

		// Testen ob wirklich kein Produkt gefunden wurde
		assertThat(results.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktByHersteller Gefunden: {0}",
				results.size());

	}

	@Test
	public void testeFindProduktByHerstellerWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByHerstellerWithValidationError Hersteller={0}",
				INVALID_HERSTELLER);

		// Fehler erwarten
		thrown.expect(ProduktValidationException.class);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByHersteller(
				INVALID_HERSTELLER, FetchType.KOMPLETT, LOCALE);

		// Testen ob wirklich nichts gefunden wurde
		assertThat(results.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByHerstellerWithValidationError Gefunden: {0}",
				results.size());

	}

	@Test
	public void testeFindExistingProduktByBeschreibung() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktByBeschreibung Beschreibung={0}",
				VORHANDENE_BESCHREIBUNG);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByBeschreibung(
				VORHANDENE_BESCHREIBUNG, FETCH_TYPE, LOCALE);

		// Testen ob alle Produkte der Beschreibung entsprechen
		for (Produkt produkt : results) {
			assertThat(
					produkt.getBeschreibung().contains(VORHANDENE_BESCHREIBUNG),
					is(true));
		}

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktByBeschreibung Gefunden: {0}",
				results.size());
	}

	@Test
	public void testeFindNonExistingProduktByBeschreibung() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktByBeschreibung Beschreibung={0}",
				NICHT_VORHANDENE_BESCHREIBUNG);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByBeschreibung(
				NICHT_VORHANDENE_BESCHREIBUNG,
				ProduktService.FetchType.NUR_PRODUKTE, LOCALE);

		// Testen ob wirklich kein Produkt gefunden wurde
		assertThat(results.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktByBeschreibung Gefunden: {0}",
				results.size());
	}

	@Test
	public void testeFindProduktByBeschreibungWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByBeschreibungWithValidationError Beschreibung={0}",
				INVALID_BESCHREIBUNG);

		// Fehler erwarten
		thrown.expect(ProduktValidationException.class);

		// Produkte suchen
		List<Produkt> results = produktService.findProduktByBeschreibung(
				INVALID_BESCHREIBUNG, FETCH_TYPE, LOCALE);

		// Testen ob wirklich nichts gefunden wurde
		assertThat(results.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByBeschreibungWithValidationError Gefunden: {0}",
				INVALID_BESCHREIBUNG);

	}

	@Test
	public void testeUpdateProdukt() {

		// Vorhandenes Produkt auswählen
		Produkt testProdukt = produktService.findProduktByID(
				VORHANDENE_PRODUKT_ID, FetchType.KOMPLETT, LOCALE);

		// Log
		LOGGER.log(Level.DEBUG, "TEST BEGINN: Teste UpdateProdukt Produkt={0}",
				testProdukt);

		// Produkt bearbeiten
		testProdukt.setBeschreibung(NEUE_BESCHREIBUNG);
		testProdukt.setHersteller(NEUER_HERSTELLER);

		// In DB speichern
		produktService.updateProdukt(testProdukt, LOCALE);

		// Produkt neu abfragen
		Produkt testProduktBearbeitet = produktService.findProduktByID(
				VORHANDENE_PRODUKT_ID, FetchType.KOMPLETT, LOCALE);

		// Vergleichen
		assertThat(testProdukt.getBeschreibung(),
				is(testProduktBearbeitet.getBeschreibung()));
		assertThat(testProdukt.getHersteller(),
				is(testProduktBearbeitet.getHersteller()));

		// Log
		LOGGER.log(Level.DEBUG, "TEST ENDE: Teste UpdateProdukt Produkt={0}",
				testProdukt);

	}

	@Test
	public void testeUpdateInvalidProdukt() {

		// Produkt auswählen
		Produkt testProdukt = produktService.findProduktByID(
				VORHANDENE_PRODUKT_ID, FetchType.KOMPLETT, LOCALE);

		// Log
		LOGGER.log(Level.DEBUG, "TEST BEGINN: Teste UpdateProdukt Produkt={0}",
				testProdukt);

		// Fehler erwarten
		thrown.expect(ProduktValidationException.class);

		// Fehlerhafte ID setzen
		testProdukt.setProduktID(INVALID_PRODUKT_ID);

		// Produkt speichern
		produktService.updateProdukt(testProdukt, LOCALE);

		// Log
		LOGGER.log(Level.DEBUG, "TEST ENDE: Teste UpdateProdukt Produkt={0}",
				testProdukt);

	}
}
