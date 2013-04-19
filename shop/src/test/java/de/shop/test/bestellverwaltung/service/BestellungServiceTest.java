package de.shop.test.bestellverwaltung.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.bestellverwaltung.service.BestellungService.FetchType;

import de.shop.kundenverwaltung.service.KundeService;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.test.util.AbstractTest;
import de.shop.util.exceptions.BestellungValidationException;
import de.shop.util.exceptions.InvalidBestellungIdException;
import de.shop.util.exceptions.InvalidGesamtpreisException;

/**
 * Die Klasse BestellungServiceTest testet die Funktionalität der Klasse
 * BestellungService.
 * 
 * Folgende Tests werde durchlaufen:
 * 
 * - Suche eine vorhandenen Bestellung(mit und ohne Kunde, Bestellposten) anhand
 * ID/Preisspanne<br>
 * - Anlegen einer neuen Bestellung<br>
 * - Anlegen einer neuen Bestellung mit syntaktischem Fehler<br>
 * - Update von Bestelldaten<br>
 * - Update von Bestelldaten mit syntaktischem Fehler
 * 
 * @see BestellService
 * @author Matthias Schnell
 */
@RunWith(Arquillian.class)
public class BestellungServiceTest extends AbstractTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private BestellungService bs;

	@Inject
	private KundeService ks;

	@Inject
	private ProduktdatenService pds;

	private static final Integer KUNDE_ID_EXIST = Integer.valueOf(101);
	private static final Integer PRODUK_ID_EXIST = Integer.valueOf(412);
	private static final Integer BESTELLUNG_ID_EXIST = Integer.valueOf(502);

	private static final Integer BESTELLUNG_ID_NOT_EXIST = Integer
			.valueOf(9999);
	private static final Integer BESTELLUNG_ID_INVALID = Integer.valueOf(-1);
	private static final Integer BESTELLUNG_ID_UPDATE = Integer.valueOf(501);

	private static final double MAX_EXIST = Double.valueOf(999);
	private static final double MIN_EXIST = Double.valueOf(1.0);
	private static final double MAX_NOT_EXIST = Double.valueOf(100000.0);
	private static final double MIN_NOT_EXIST = Double.valueOf(99999.0);
	private static final double MIN_INVALID = Double.valueOf(-1.0);

	private static final int KONSTANTE_1000 = 1000;
	private static final double GESAMTPREIS_UPDATE_FALSE = Double.valueOf(0);

	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Es wird eine neue Bestellung mit korrekten Daten angelegt und
	 * anschließend gesucht. Wurde sie gefunden, ist der Test korrekt.
	 */
	@Test
	public void testeAddBestellung() {
		// Bestellungsobjekt mit vernünftigen Daten füllen

		Bestellung newBestellungTrue = new Bestellung();

		Produktdaten produktExist = pds.findProduktdatenByID(PRODUK_ID_EXIST,
				LOCALE_DEFAULT);
		@SuppressWarnings("unused")
		Bestellposten newBestellposten = new Bestellposten(newBestellungTrue,
				produktExist, 1);
		newBestellungTrue.setKunde(ks.findKundeById(KUNDE_ID_EXIST, LOCALE_DEFAULT));
		newBestellungTrue.setGesamtpreis(KONSTANTE_1000);

		// Neue Bestellung anlegen
		bs.addBestellung(newBestellungTrue, LOCALE_DEFAULT);

		// Eben angelegte Bestellung in der Datenbanksuchen
		Bestellung be = bs.findBestellungById(
				newBestellungTrue.getBestellungID(), LOCALE_DEFAULT);

		// Prüfe ob Bestellung gefunden wurde
		assertThat(be.equals(newBestellungTrue), is(true));
	}

	/**
	 * Es wird eine neue Bestellung mit fehlerhaften Daten angelegt. Wenn der
	 * korrekte Fehler angezeigt abgefangen wird, ist der Test korrekt.
	 */
	@Test
	public void testeAddInvalidBestellung() {

		// Neue Bestellung ohne sinnvolle Daten erstellen
		Bestellung newBestellungIncorrect = new Bestellung();
		// Erwarteter Fehler
		thrown.expect(BestellungValidationException.class);

		// Neue Bestellung ohne sinnvolle Daten anlegen
		bs.addBestellung(newBestellungIncorrect, LOCALE_DEFAULT);

	}

	/**
	 * Es wird eine Bestellung mit der ID 502 gesucht. Diese ist als Testdaten
	 * in der Datenbank enthalten und muss gefunden werden
	 */
	@Test
	public void testeFindExistingBestellungById() {
		// Suche in Datenbank
		Bestellung be = bs.findBestellungById(BESTELLUNG_ID_EXIST, LOCALE_DEFAULT);

		// Test ob Bestellung nicht gefund wurde
		assertThat(be.getBestellungID(), is(BESTELLUNG_ID_EXIST));

	}

	/**
	 * Es wird eine Bestellung mit der ID 9999 gesucht. Diese ist nicht in der
	 * Datenbank vorhanden und darf nich gefunden werden
	 */
	@Test
	public void testeFindNonExistingBestellungById() {
		// Suche in Datenbank
		Bestellung be = bs.findBestellungById(BESTELLUNG_ID_NOT_EXIST,
				LOCALE_DEFAULT);

		// Test ob Bestellung nicht gefund wurde
		assertThat(be, is(nullValue()));
	}

	/**
	 * Es wird eine Bestellung mit der ID -1 gesucht. Diese verstößt gegen die
	 * Validierung und muss einen Fehler werfen.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testeFindBestellungByIdWithValidationError() {
		// Erwarteter Fehler
		thrown.expect(InvalidBestellungIdException.class);

		// Suche in Datenbank
		Bestellung be = bs.findBestellungById(BESTELLUNG_ID_INVALID, LOCALE_DEFAULT);
	}

	/**
	 * Es werden Bestellungen mit einer Preisspanne von 1 bis 999 gesucht. Davon
	 * sind Bestellungen in der Datenbank enthalten und es müssen welche
	 * Gefunden werden.
	 */
	@Test
	public void testeFindExistingBestellungByPreisspanne() {
		// Suche Bestellungen in der Datenbank
		List<Bestellung> bes = bs.findBestellungByPreisspanne(
				FetchType.JUST_BESTELLUNG, MIN_EXIST, MAX_EXIST, LOCALE_DEFAULT);

		// Es wird geprüft ob alle Bestellungen in der Preisspanne liegen
		Boolean test = false;
		for (Bestellung b : bes) {
			if (b.getGesamtpreis() >= MIN_EXIST
					&& b.getGesamtpreis() <= MAX_EXIST) {
				test = true;
			}

			else {
				test = false;
				break;
			}

		}
		assertThat(test, is(true));
	}

	/**
	 * Es werden Bestellungen (inlk Kunden) mit einer Preisspanne von 1 bis 999
	 * gesucht. Davon sind Bestellungen in der Datenbank enthalten und es müssen
	 * welche Gefunden werden.
	 */
	@Test
	public void testeFindExistingBestellungByPreisspanneFetchKunde() {

		// Suche Bestellungen in der Datenbank
		List<Bestellung> bes = bs.findBestellungByPreisspanne(
				FetchType.WITH_KUNDE, MIN_EXIST, MAX_EXIST, LOCALE_DEFAULT);

		// Es wird geprüft ob alle Bestellungen in der Preisspanne liegen
		Boolean test = false;
		for (Bestellung b : bes) {
			if (b.getGesamtpreis() >= MIN_EXIST
					&& b.getGesamtpreis() <= MAX_EXIST) {
				test = true;
			}

			else {
				test = false;
				break;
			}

		}
		assertThat(test, is(true));
	}

	/**
	 * Es werden Bestellungen (mit Bestellposten) mit einer Preisspanne von 1
	 * bis 999 gesucht. Davon sind Bestellungen in der Datenbank enthalten und
	 * es müssen welche Gefunden werden.
	 */
	@Test
	public void testeFindExistingBestellungByPreisspanneFetchBestellposten() {

		// Suche Bestellungen in der Datenbank
		List<Bestellung> bes = bs.findBestellungByPreisspanne(
				FetchType.WITH_BESTELLPOSTEN, MIN_EXIST, MAX_EXIST, LOCALE_DEFAULT);

		// Es wird geprüft ob alle Bestellungen in der Preisspanne liegen
		Boolean test = false;
		for (Bestellung b : bes) {
			if (b.getGesamtpreis() >= MIN_EXIST
					&& b.getGesamtpreis() <= MAX_EXIST) {
				test = true;
			}

			else {
				test = false;
				break;
			}

			assertThat(test, is(true));
		}

	}

	/**
	 * Es werden Bestellungen mit sehr hoher Preisspanne gesucht. Diese ist
	 * nicht in der Datenbank vorhanden und es dürfen keine Bestellungen
	 * gefunden werden.
	 */
	@Test
	public void testeFindNonExistingBestellungByPreisspanne() {
		// Suche in Datenbank
		List<Bestellung> be = bs.findBestellungByPreisspanne(
				FetchType.JUST_BESTELLUNG, MIN_NOT_EXIST, MAX_NOT_EXIST,
				LOCALE_DEFAULT);

		// Test ob keine Bestellung gefunden wurde.
		assertThat(be.size(), is(0));
	}

	/**
	 * Es werden Bestellungen mit negativer Preisspanne gesucht. Dieses verstößt
	 * gegen die Validierung und muss einen Fehler werfen.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testeFindBestellungByPreisspanneWithValidationError() {
		// Erwarteter Fehler
		thrown.expect(InvalidGesamtpreisException.class);

		// Suche in Datenbank
		List<Bestellung> be = bs.findBestellungByPreisspanne(
				FetchType.JUST_BESTELLUNG, MIN_INVALID, MAX_EXIST, LOCALE_DEFAULT);
	}

	/**
	 * Ein vorhandene Bestellung wird gesucht und der Gesamtpreis neu berechnet.
	 * Anschließend wird die geupdatete Bestellung wieder in die Datenbank
	 * eingepflegt.
	 */
	@Test
	public void testeUpdateBestellung() {
		// vorhandene Bestellung aus der Datenbank suchen
		Bestellung bestellungExist = bs.findBestellungById(BESTELLUNG_ID_EXIST,
				LOCALE_DEFAULT);

		// Gesamtpreis der Bestellung updaten
		bestellungExist.errechneGesamtpreis();

		// geupdatete Bestellung an die Datenbank übergeben
		bs.updateBestellung(bestellungExist, LOCALE_DEFAULT);

		// Bestellung anhand des neuen Gesamtpreises wiederfinden
		List<Bestellung> be = bs.findBestellungByPreisspanne(
				FetchType.JUST_BESTELLUNG, bestellungExist.getGesamtpreis(),
				bestellungExist.getGesamtpreis(), LOCALE_DEFAULT);

		// rausfinden ob eine der Bestellungen die gesuchte ist
		Boolean test = false;
		for (Bestellung b : be) {
			if (b.getBestellungID() == BESTELLUNG_ID_EXIST) {
				test = true;
				break;
			}
		}
		assertThat(test, is(true));

	}

	/**
	 * Eine vorhandene Bestellung wird gesucht und anschließend ein fehlerhafter
	 * Gesamtpreis bestimmt. Wird der korrekte Fehler beim updaten auf die
	 * Datenbank geworfen, ist der Test korrekt.
	 */
	@Test
	public void testeUpdateInvalidBestellung() {
		// vorhandene Bestellung aus der Datenbank suchen
		Bestellung bestellungExist = bs.findBestellungById(
				BESTELLUNG_ID_UPDATE, LOCALE_DEFAULT);

		// Fehlerhafte Daten eingeben
		bestellungExist.setGesamtpreis(GESAMTPREIS_UPDATE_FALSE);

		// Erwarteter Fehler
		thrown.expect(BestellungValidationException.class);

		// Geänderte Bestellung an Datenbank übergeben
		bs.updateBestellung(bestellungExist, LOCALE_DEFAULT);

	}
}