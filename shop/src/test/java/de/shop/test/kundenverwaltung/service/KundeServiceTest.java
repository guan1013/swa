package de.shop.test.kundenverwaltung.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.test.util.AbstractTest;
import de.shop.util.exceptions.InvalidEmailException;
import de.shop.util.exceptions.InvalidKundeIdException;
import de.shop.util.exceptions.InvalidNachnameException;
import de.shop.util.exceptions.KundeValidationException;

/**
 * Die Klasse KundeServiceTest testet die Funktionalität der Klasse
 * KundeService.
 * 
 * Folgende Tests werde durchlaufen:
 * 
 * - Suche eines vorhandenen Kunden(mit und ohne Bestellungen) anhand
 * Nachname/E-Mail/ID<br>
 * - Anlegen eines neuen Kunden<br>
 * - Anlegen eines neuen Kunden mit syntaktischem Fehler<br>
 * - Update von Kundendaten<br>
 * - Update von Kundendaten mit syntaktischem Fehler
 * 
 * @see KundeService
 * @author Matthias Schnell
 */

@RunWith(Arquillian.class)
public class KundeServiceTest extends AbstractTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private KundeService ks;

	private static final Integer ID_EXIST = Integer.valueOf(101);
	private static final Integer ID_NOT_EXIST = Integer.valueOf(9999);
	private static final Integer ID_INVALID = Integer.valueOf(-3);

	private static final String MAIL_EXIST = "ben.meier@yahoo.com";
	private static final String MAIL_NOT_EXIST = "mail@adresse.com";
	private static final String MAIL_INVALID = null;

	private static final String NAME_EXIST = "Meier";
	private static final String NAME_NOT_EXIST = "Nichtvorhanden";
	private static final String NAME_INVALID = "noName";

	private static final String MAIL_UPDATE = "mia.mueller@gmail.com";

	private static final Integer ID_UPDATE_EXIST = Integer.valueOf(100);

	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Es wird ein neuer Kunde mit korrekten Daten angelegt und anschließend
	 * gesucht. Wurde er gefunden, ist der Test korrekt.
	 */
	@Test
	public void testeAddKunde() {
		// Neues Kundenobjekt erstellen
		Kunde newKdCorrect = new Kunde("Doe", "John", "jd@mail.com");
		// Neuen Kunde anlegen
		ks.addKunde(newKdCorrect, LOCALE_DEFAULT);

		// Eben angelegten Kunden in der Datenbank suchen
		List<Kunde> kd = ks.findKundeByMail(FetchType.JUST_KUNDE,
				newKdCorrect.getEmail(), LOCALE_DEFAULT);

		// Prüfe ob Kunde gefunden wurde
		assertThat(kd.size(), is(1));
	}

	/**
	 * Ein neuer Kunde mit fehlerhaftem Nachnamen (anhand der Validierung) wird
	 * angelegt. Wird die korrekte Fehlermeldung geworfen, ist der Test
	 * erfolgreich.
	 */
	@Test
	public void testeAddInvalidKunde() {
		// Neuen Fehlerhaften Kunden anlegen
		Kunde newKdIncorrect = new Kunde("doe-", "john-", "jd@mail.com");

		// Erwartete Fehermeldung
		thrown.expect(KundeValidationException.class);

		// Neuen fehlerhaften Kunden anlegen
		ks.addKunde(newKdIncorrect, LOCALE_DEFAULT);

		// Eben angelegten Kunden in der Datenbank suchen
		List<Kunde> kd = ks.findKundeByMail(FetchType.JUST_KUNDE,
				newKdIncorrect.getEmail(), LOCALE_DEFAULT);

		// Prüfe ob Kunde gefunden wurde
		assertThat(kd.size(), is(0));

	}

	/**
	 * Es werden alle Kunden gesucht. Da Testdaten in der Datenbank vorhanden
	 * sind, müssen Kunden gefunden werden
	 */
	@Test
	public void testeFindAllKunden() {
		// Kunden suchen
		List<Kunde> kd = ks.findAllKunden();

		// Testen ob etwas gefunden wurde
		assertThat(kd.isEmpty(), is(false));
	}

	/**
	 * Suche nach einem Kunden mit der ID: 101. Diese ist in der Datenbank
	 * vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeById() {
		// Suche in Datenbank
		Kunde kd = ks.findKundeById(ID_EXIST, LOCALE_DEFAULT);

		// Test ob Kunde gefunden wurde
		assertThat(kd.getKundeID(), is(ID_EXIST));

	}

	/**
	 * Suche nach einem Kunden mit der ID: 9999. Diese ist nicht in der
	 * Datenbank vorhanden und darf nicht gefunden werden.
	 */
	@Test
	public void testeFindNonExistingKundeById() {
		// Suche in Datenbank
		Kunde kd = ks.findKundeById(ID_NOT_EXIST, LOCALE_DEFAULT);

		// Test ob Kunde nicht gefunden wurde
		assertThat(kd, is(nullValue()));

	}

	/**
	 * Suche nach einem Kunden mit der ID: -3. Diese ID entspricht nicht der
	 * Validierung und muss einen Fehler werfen
	 */
	@SuppressWarnings("unused")
	@Test
	public void testeFindKundeByIdWithValidationError() {
		// Erwarteter Fehler
		thrown.expect(InvalidKundeIdException.class);

		// Suche in Datenbank
		Kunde kd = ks.findKundeById(ID_INVALID, LOCALE_DEFAULT);

	}

	/**
	 * Suche nach einem Kunden mit der E-Mail Adresse: ben.meier@yahoo.com.
	 * Diese ist in der Datenbank vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeByMail() {

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByMail(FetchType.JUST_KUNDE, MAIL_EXIST,
				LOCALE_DEFAULT);

		// Test ob Kunde gefunden wurde
		assertThat(kd.size(), is(1));
	}

	/**
	 * Suche nach einem Kunden, inkl Bestellungen, mit der E-Mail Adresse:
	 * ben.meier@yahoo.com. Diese ist in der Datenbank vorhanden, hat
	 * Bestellungen und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeByMailFetchBestellungen() {

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByMail(FetchType.WITH_BESTELLUNGEN,
				MAIL_EXIST, LOCALE_DEFAULT);
		Boolean test = false;
		for (Kunde k : kd) {
			if (k.getEmail().equals(MAIL_EXIST)) {
				test = true;
			}
			else {
				test = false;
				break;
			}
		}

		// Test ob Kunde gefunden wurde
		assertThat(test, is(true));
	}

	/**
	 * Suche nach einem Kunden mit der E-Mail Adresse: email@adresse.com. Diese
	 * ist nicht in der Datenbank vorhanden und darf nicht gefunden werden.
	 */
	@Test
	public void testeFindNonExistingKundeByMail() {

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByMail(FetchType.JUST_KUNDE,
				MAIL_NOT_EXIST, LOCALE_DEFAULT);

		// Test ob kein Kunde gefunden wurde
		assertThat(kd.size(), is(0));
	}

	/**
	 * Suche nach einem Kunden mit der einem Null Wert als E-Mail Adresse. Dies
	 * verstößt gegen die Validierung und muss einen Fehler werfen.
	 */
	@SuppressWarnings("unused")
	@Test
	@Ignore
	public void testeFindKundeByMailWithValidationError() {

		// Erwarteter Fehler
		thrown.expect(InvalidEmailException.class);

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByMail(FetchType.JUST_KUNDE, MAIL_INVALID,
				LOCALE_DEFAULT);
	}

	/**
	 * Es wird ein Kunde mit dem Nachnamen Meier gesucht. Dieser ist als
	 * Testdaten in der Datenbank vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeByNachname() {
		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByNachname(FetchType.JUST_KUNDE,
				NAME_EXIST, LOCALE_DEFAULT);

		// Test ob jeder Name in kd Meier ist
		Boolean test = false;
		for (Kunde k : kd) {
			if (k.getNachname().equals(NAME_EXIST)) {
				test = true;
			}
			else {
				test = false;
				break;
			}
		}

		// Test ob Kunde existiert
		assertThat(test, is(true));

	}

	/**
	 * Es wird ein Kunde (inkl Bestellungen) mit dem Nachnamen Meier gesucht.
	 * Dieser ist als Testdaten in der Datenbank vorhanden, hat Bestellungen und
	 * muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeByNachnameFetchBestellungen() {

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByNachname(FetchType.WITH_BESTELLUNGEN,
				NAME_EXIST, LOCALE_DEFAULT);

		// Test ob jeder Name in kd Meier ist
		Boolean test = false;
		for (Kunde k : kd) {
			if (k.getNachname().equals(NAME_EXIST)) {
				test = true;
			}

			else {
				test = false;
				break;
			}
		}

		// Test ob Kunde existiert
		assertThat(test, is(true));
	}

	/**
	 * Es wird ein Kunde mit dem Nachnamen "Nichtvorhanden" gesucht. Dieser ist
	 * nicht in der Datenbank vorhanden und darf nichtg gefunden werden.
	 */
	@Test
	public void testeFindNonExistingKundeByNachname() {

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByNachname(FetchType.JUST_KUNDE,
				NAME_NOT_EXIST, LOCALE_DEFAULT);

		// Test ob kein Kunde gefunden wurde
		assertThat(kd.size(), is(0));
	}

	/**
	 * Es wird ein Kunde mit dem Nachnamen "noName" gesucht. Dieser verstößt
	 * gegen die Validierung und muss einen Fehler werfen.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testeFindKundeByNachnameWithValidationError() {
		// Erwarteter Fehler
		thrown.expect(InvalidNachnameException.class);

		// Suche in Datenbank
		List<Kunde> kd = ks.findKundeByNachname(FetchType.JUST_KUNDE,
				NAME_INVALID, LOCALE_DEFAULT);

	}

	/**
	 * Der Kunde mit der ID 100 wird gesucht. Diese ID ist in der Datenbank
	 * vorhanden und muss gefunden werden. Die Email Adresse wird zunächst im
	 * Kundenobjekt geupdated und dann an die Datenbank übergeben. Wenn dieser
	 * Kunde anhand der neuen Adresse wieder gefunden wird, ist der Test
	 * geglückt.
	 */
	@Test
	public void testeUpdateKunde() {

		// Zu updatenden Kunden suchen
		Kunde kd = ks.findKundeById(ID_UPDATE_EXIST, LOCALE_DEFAULT);

		// Neue E-Mail wird dem Kunden Objekt hinzugefügt
		kd.setEmail(MAIL_UPDATE);

		// neue Werte werden geprüft und geupdated
		ks.updateKunde(kd, LOCALE_DEFAULT);

		// Prüfen ob Kunde mit neuer E-Mail gefunden wird
		List<Kunde> kdUpdated = ks.findKundeByMail(FetchType.JUST_KUNDE,
				kd.getEmail(), LOCALE_DEFAULT);

		assertThat(kdUpdated.get(0).getKundeID(), is(kd.getKundeID()));

	}

	/**
	 * Ein Kunde wird gesucht und dessen Nachname fehlerhaft (nach Validierung)
	 * abgeändert. Wird die korrekte Fehlermeldung geworfen, ist der Test
	 * erfolgreich.
	 */
	@Test
	public void testeUpdateInvlidKunde() {
		// Zu updatenden Kunden suchen
		Kunde kd = ks.findKundeById(ID_UPDATE_EXIST, LOCALE_DEFAULT);

		// Erwartete Fehermeldung
		thrown.expect(KundeValidationException.class);

		// Neuer Nachname wird dem Kunden Objekt hinzugefügt
		kd.setNachname("mÜll3r");

		// neue Werte werden geprüft und geupdated
		ks.updateKunde(kd, LOCALE_DEFAULT);

		// Prüfen ob Kunde mit neuer E-Mail gefunden wird
		List<Kunde> kdUpdated = ks.findKundeByMail(FetchType.JUST_KUNDE,
				kd.getEmail(), LOCALE_DEFAULT);

		assertThat(kdUpdated.get(0).getKundeID(), is(kd.getKundeID()));

	}
}