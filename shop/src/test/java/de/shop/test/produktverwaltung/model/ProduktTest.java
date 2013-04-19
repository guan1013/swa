package de.shop.test.produktverwaltung.model;

import static java.util.logging.Level.SEVERE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logmanager.Level;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produkt_;
import de.shop.test.util.AbstractDomainTest;
import de.shop.util.IdGroup;
import de.shop.util.ValidationService;
import de.shop.util.exceptions.ProduktValidationException;

/**
 * Die Klasse ProduktTest testet die Funktionalität der Klasse Produkt. Folgende
 * Testcases werde durchlaufen:
 * 
 * - Suche eines vorhandenen Produktes<br>
 * - Suche eines nicht vorhandenen Produktes<br>
 * - Anlegen eines neuen Produktes<br>
 * - Anlegen eines fehlerhaften Produktes<br>
 * - Ändern eines vorhandenen Produktes<br>
 * - Ändern eines vorhandenen Produktes fehlerhaft<br>
 * //TODO <- letzten Testcase implementieren
 * 
 * @see Produkt
 * @author Andreas Güntzel
 * 
 */
@RunWith(Arquillian.class)
public class ProduktTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final Locale LOCALE = new Locale("de");

	private static final Integer VORHANDENE_PRODUKT_ID = new Integer(313);

	private static final Integer NICHT_VORHANDENE_PRODUKT_ID = new Integer(4444);

	private static final Integer UNGUELTIGE_PRODUKT_ID = new Integer(-12);

	private static final String GUELTIGER_HERSTELLER = "MFH";

	private static final String UNGUELTIGER_HERSTELLER = null;

	private static final String GUELTIGE_BESCHREIBUNG = "Hose";

	private static final String UNGUELTIGE_BESCHREIBUNG = "";

	private static final String NICHT_VORHANDENE_BESCHREIBUNG = "yyyyyxxxxxxzzzzz";

	private static final String TEST_BESCHREIBUNG = "JUnitTestBeschreibung";

	private static final String TEST_HERSTELLER = "JUnitTestHersteller";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private ValidationService validationService;

	// /////////////////////////////////////////////////////////////////////
	// TEST-METHODS

	/**
	 * Die Methode legt ein neues Produkt ab und versucht es in der Datenbank
	 * abzuspeichern. Das Persistieren muss funktionieren, da das Produkt
	 * konsistent ist.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testeAddProdukt() {

		// Neues Produkt anlegen
		Produkt neuesProdukt = new Produkt("testprodukt", "junit");

		// In Datenbank speichern
		getEntityManager().persist(neuesProdukt);

		// Dieses Produkt über NamedQuery in DB suchen
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_HERSTELLER)
				.setParameter("hersteller", "junit").getResultList();

		// Testen ob das Produkt gefunden wurde
		assertThat(gefundeneProdukte.size(), is(1));

	}

	/**
	 * Die Methode legt ein neues, fehlerhaftes Produkt an und versucht es in
	 * der Datenbank zu speichern. Da das Objekt inkonsistent ist, erwartet die
	 * Methode, dass eine ConstrainViolationException geworfen wird.
	 */
	@Test
	public void testeAddInvalidProdukt() {

		// Neues, fehlerhaftes Produkt anlegen
		Produkt neuesFehlerhaftesProdukt = new Produkt();

		// Hersteller/Beschreibung löschen => inkonsistentes Objekt
		neuesFehlerhaftesProdukt.setHersteller(UNGUELTIGER_HERSTELLER);
		neuesFehlerhaftesProdukt.setBeschreibung(UNGUELTIGE_BESCHREIBUNG);

		// Erwarte Fehler beim Speichern
		thrown.expect(ConstraintViolationException.class);

		// Objekt als Managed Object hinzufügen
		getEntityManager().persist(neuesFehlerhaftesProdukt);

		// Mit Datenbank synchronisieren
		getEntityManager().flush();

	}

	/**
	 * Die Methode sucht ein Produkt anhand des Primarschlüssels (313). Das
	 * Produkt muss in der Datenbank gefunden werden, da ein Datensatz mit
	 * diesem Primärschlüssel über die Testdaten in der Datenbank angelegt
	 * wurde.
	 */
	@Test
	public void testeFindExistingProduktById() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktById ID = {0}",
				VORHANDENE_PRODUKT_ID);

		// Suche in Datenbank
		Produkt gefundenesProdukt = getEntityManager().find(Produkt.class,
				VORHANDENE_PRODUKT_ID);

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundenesProdukt.getProduktID(), is(VORHANDENE_PRODUKT_ID));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktById Gefunden = {0}",
				gefundenesProdukt.getProduktID());

	}

	/**
	 * Die Methode sucht ein Produkt anhand eines Primarschlüssels (19123). Es
	 * darf kein entsprechender Datensatz gefunden werden, da dieser
	 * Primärschlüssel in der Datenbank nicht vorhanden ist.
	 */
	@Test
	public void testeFindNonExistingProduktById() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktById ID = {0}",
				NICHT_VORHANDENE_PRODUKT_ID);

		// Suche in Datenbank
		Produkt gefundenesProdukt = getEntityManager().find(Produkt.class,
				NICHT_VORHANDENE_PRODUKT_ID);

		// Teste ob wirklich kein Produkt gefunden wurde
		assertThat(gefundenesProdukt, is(nullValue()));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktById Gefunden = {0}",
				gefundenesProdukt);

	}

	@Test
	public void testeFindProduktByIdWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByIdWithValidationError ID = {0}",
				UNGUELTIGE_PRODUKT_ID);

		// Erwartete Fehlermeldung
		// thrown.expect(ProduktValidationException.class);

		// Suche in Datenbank
		Produkt gefundenesProdukt = getEntityManager().find(Produkt.class,
				UNGUELTIGE_PRODUKT_ID);

		// Teste ob das Produkt wirklich nicht gefunden wurde
		assertThat(gefundenesProdukt, is(nullValue()));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByIdWithValidationError Gefunden = {0}",
				gefundenesProdukt);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindExistingProduktByHersteller() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktByHersteller Hersteller = {0}",
				GUELTIGER_HERSTELLER);

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_HERSTELLER)
				.setParameter("hersteller", GUELTIGER_HERSTELLER)
				.getResultList();

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundeneProdukte.size(), is(1));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktByHersteller Gefunden = {0}",
				gefundeneProdukte.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindNonExistingProduktByHersteller() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktByHersteller Hersteller = {0}",
				UNGUELTIGER_HERSTELLER);

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_HERSTELLER)
				.setParameter("hersteller", UNGUELTIGER_HERSTELLER)
				.getResultList();

		// Teste ob das Produkt wirklich nicht gefunden wurde
		assertThat(gefundeneProdukte.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktByHersteller Gefunden = {0}",
				gefundeneProdukte.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindProduktByHerstellerWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByHerstellerWithValidationError Hersteller = {0}",
				UNGUELTIGER_HERSTELLER);

		// Erwartete Fehlermeldung
		thrown.expect(ProduktValidationException.class);

		// Validierung Hersteller
		checkViolations(getValidator(LOCALE).validateValue(Produkt.class,
				Produkt_.hersteller.getName(), UNGUELTIGER_HERSTELLER));

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_HERSTELLER)
				.setParameter("hersteller", UNGUELTIGER_HERSTELLER)
				.getResultList();

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundeneProdukte.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByHerstellerWithValidationError Gefunden = {0}",
				gefundeneProdukte.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindExistingProduktByBeschreibung() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingProduktByBeschreibung Beschreibung = {0}",
				GUELTIGE_BESCHREIBUNG);

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_LIKE_BESCHREIBUNG)
				.setParameter("beschreibung", GUELTIGE_BESCHREIBUNG)
				.getResultList();

		for (Produkt p : gefundeneProdukte) {
			String beschreibung = p.getBeschreibung().toLowerCase();
			String beschreibung2 = GUELTIGE_BESCHREIBUNG.toLowerCase();
			assertThat(beschreibung.contains(beschreibung2), is(true));
		}

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingProduktByBeschreibung Gefunden = {0}",
				gefundeneProdukte.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindNonExistingProduktByBeschreibung() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingProduktByBeschreibung Beschreibung = {0}",
				NICHT_VORHANDENE_BESCHREIBUNG);

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_LIKE_BESCHREIBUNG)
				.setParameter("beschreibung", NICHT_VORHANDENE_BESCHREIBUNG)
				.getResultList();

		// Teste ob das Produkt wirklich gefunden wurde
		assertThat(gefundeneProdukte.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingProduktByBeschreibung Gefunden = {0}",
				gefundeneProdukte.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeFindProduktByBeschreibungWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindProduktByBeschreibungWithValidationError Beschreibung = {0}",
				UNGUELTIGE_BESCHREIBUNG);

		// Erwartete Fehlermeldung
		thrown.expect(ProduktValidationException.class);

		// Validierung Hersteller
		checkViolations(getValidator(LOCALE).validateValue(Produkt.class,
				Produkt_.beschreibung.getName(), UNGUELTIGE_BESCHREIBUNG));

		// Suche in Datenbank
		List<Produkt> gefundeneProdukte = getEntityManager()
				.createNamedQuery(Produkt.PRODUKT_BY_LIKE_BESCHREIBUNG)
				.setParameter("beschreibung", UNGUELTIGE_BESCHREIBUNG)
				.getResultList();

		// Teste ob das Produkt wirklich gefunden wurde
		assertThat(gefundeneProdukte.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindProduktByBeschreibungWithValidationError Gefunden = {0}",
				UNGUELTIGE_BESCHREIBUNG);

	}

	/**
	 * Die Methode ändert ein vorhandenes Produkt in der Datenbank. Es wird ein
	 * Produkt mit bestimmter ID (303) aus der Datenbank abgefragt (diese ID
	 * muss aufgrund der Testdaten vorhanden sein). Das ausgewählte Produkt wird
	 * bearbeitet (neue Beschreibung) und anschließend in der Datenbank
	 * gespeichert. Anschließend wird versucht per Named Query das geänderte
	 * Produkt in der DB wieder zu finden.
	 */
	@Test
	public void testeUpdateProdukt() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste UpdateProdukt Produkt_ID = {0}",
				VORHANDENE_PRODUKT_ID);

		// ID des zu ändernden Produktes
		Produkt gefundenesProdukt = getEntityManager().find(Produkt.class,
				VORHANDENE_PRODUKT_ID);

		// Produkt ändern
		gefundenesProdukt.setBeschreibung(TEST_BESCHREIBUNG);
		gefundenesProdukt.setHersteller(TEST_HERSTELLER);

		// Produkt speichern
		getEntityManager().merge(gefundenesProdukt);

		// Produkt mit neuer Beschreibung suchen
		Produkt geaendertesProdukt = getEntityManager().find(Produkt.class,
				VORHANDENE_PRODUKT_ID);

		// Teste ob Produkt mit neuer Beschreibung und Hersteller gefunden wurde

		assertThat(geaendertesProdukt.getBeschreibung(), is(TEST_BESCHREIBUNG));
		assertThat(geaendertesProdukt.getHersteller(), is(TEST_HERSTELLER));

		// Log
		LOGGER.log(Level.DEBUG, "TEST ENDE: Teste UpdateProdukt Update = {0}",
				geaendertesProdukt);

	}

	@Test
	public void testeUpdateInvalidProdukt() {

		// ID des zu ändernden Produktes
		Produkt testProdukt = getEntityManager().find(Produkt.class,
				VORHANDENE_PRODUKT_ID);

		// Erwartete Fehlermeldung
		thrown.expect(ProduktValidationException.class);

		// Produkt ändern
		testProdukt.setHersteller(null);

		// Validierung Hersteller
		checkViolations(getValidator(LOCALE).validate(testProdukt,
				Default.class, IdGroup.class));

		// Produkt speichern
		getEntityManager().merge(testProdukt);

	}

	private void checkViolations(Set<ConstraintViolation<Produkt>> violations) {

		if (!violations.isEmpty()) {
			LOGGER.log(SEVERE, "{0} Fehler bei der Validierung",
					violations.size());
			StringBuffer buffer = new StringBuffer();
			Iterator<ConstraintViolation<Produkt>> it = violations.iterator();
			while (it.hasNext()) {
				buffer.append(it.next().getMessage());
				buffer.append('\n');
			}
			throw new ProduktValidationException(buffer.toString());
		}
	}

	private Validator getValidator(Locale l) {
		return validationService.getValidator(l);
	}

}