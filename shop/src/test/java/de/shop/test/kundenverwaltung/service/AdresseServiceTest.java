package de.shop.test.kundenverwaltung.service;

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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.AdresseService;
import de.shop.kundenverwaltung.service.AdresseService.FetchType;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.test.util.AbstractTest;
import de.shop.util.exceptions.AdresseValidationException;

/**
 * Testklasse zur Service Klasse AdresseServiceTest.java
 * 
 * Folgende Tests werde durchlaufen:
 * 
 * - Anlegen einer neuen Adresse<br>
 * - Anlegen einer neuen Adresse mit syntaktischem Fehler<br>
 * - Update der Adresse<br>
 * - Update der Adresse mit syntaktischem Fehler<br>
 * - Suche einer vorhandenen Adresse (mit und ohne Kunde) anhand
 * Ort/Strasse/PLZ/ID<br>
 * - Suche einer vorhandenen Adresse mit syntaktischem Fehler (mit und ohne
 * Kunde) anhand Ort/Strasse/PLZ/ID<br>
 * 
 * @author Yannick Gentner
 */
@RunWith(Arquillian.class)
public class AdresseServiceTest extends AbstractTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private AdresseService as;
	@Inject
	private KundeService ks;

	private static final String ORT_EXISTS = "Berlin";
	private static final String ORT_NOT_EXISTS = "Wunderland";
	private static final String INVALID_ORT = null;
	private static final String STRASSE_EXISTS = "Bahnhofsstraße 8";
	private static final String STRASSE_NOT_EXISTS = "Hobbitweg X";
	private static final String INVALID_STRASSE = null;
	private static final String STRASSE_UPDATE_EXISTS = "Friedhofsstraße 12";
	private static final Integer PLZ_EXISTS = Integer.valueOf(74212);
	private static final Integer PLZ_NOT_EXISTS = Integer.valueOf(99999);
	private static final Integer INVALID_PLZ = Integer.valueOf(10);
	private static final Integer ADRESSE_ID_EXISTS = Integer.valueOf(210);
	private static final Integer ADRESSE_ID_NOT_EXISTS = Integer
			.valueOf(999999);
	private static final Integer INVALID_ADRESSE_ID = null;
	private static final Integer ADRESSE_ID_UPDATE_EXISTS = Integer
			.valueOf(211);
	private static final Adresse ADRESSE_CORRECT = new Adresse(
			ADRESSE_ID_EXISTS, ORT_EXISTS, PLZ_EXISTS, STRASSE_EXISTS);
	private static final Adresse ADRESSE_INCORRECT = new Adresse(100000,
			"-berlin", 12345, "Bergstr 12");
	private static final Integer ID_KUNDE_EXISTS = new Integer(103);

	private static final Locale LOCALE_DEFAULT = Locale.getDefault();

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@Test
	public void testeAddAdresse() {

		// Log
		LOGGER.log(Level.DEBUG, "TEST BEGINN: Teste AddAdresse Kunde_ID= {0}",
				ID_KUNDE_EXISTS);

		Kunde k = ks.findKundeById(ID_KUNDE_EXISTS, LOCALE_DEFAULT);

		ADRESSE_CORRECT.setKunde(k);

		// Eben angelegte Adresse in der DB suchen
		Adresse ad = as.findAdresseByAdresseID(ADRESSE_CORRECT.getAdresseID(),
				LOCALE_DEFAULT);

		// Prüfe ob Adresse gefunden wurde
		assertThat(ad.getAdresseID(), is(ADRESSE_CORRECT.getAdresseID()));

		// Log
		LOGGER.log(Level.DEBUG, "TEST ENDE: TEste AddAdresse Adresse= {0}",
				ADRESSE_CORRECT);

	}

	@Test
	public void testeAddInvalidAdresse() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste AddInvalidAdresse Kunde_ID= {0}",
				ID_KUNDE_EXISTS);

		// Erwartete Fehlermeldung
		thrown.expect(AdresseValidationException.class);

		Kunde k = ks.findKundeById(ID_KUNDE_EXISTS, LOCALE_DEFAULT);

		ADRESSE_INCORRECT.setKunde(k);

		as.addAdresse(ADRESSE_INCORRECT, LOCALE_DEFAULT);

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste AddInvalidAdresse InvalidAdresse= {0}",
				ADRESSE_INCORRECT);
	}

	@Ignore
	@Test
	public void testeUpdateAdresse() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste UpdateAdresse Adresse_ID= {0}",
				ADRESSE_ID_UPDATE_EXISTS);

		// Zu updatende Adresse suchen
		Adresse ad = as.findAdresseByAdresseID(ADRESSE_ID_UPDATE_EXISTS,
				LOCALE_DEFAULT);

		// Neue Strasse wird dem Adresse Objekt hinzugefügt
		ad.setStrasse(STRASSE_UPDATE_EXISTS);

		// neue Werte werden geprüft und geupdated
		as.updateAdresse(ad, LOCALE_DEFAULT);

		// Prüfen ob Adresse mit neuer Strasse gefunden wird
		Adresse adUpdate = as.findAdresseByAdresseID(ADRESSE_ID_UPDATE_EXISTS,
				LOCALE_DEFAULT);

		assertThat(adUpdate.getStrasse(), is(STRASSE_UPDATE_EXISTS));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste UpdateAdresse UpdateAdresse= {0}",
				adUpdate.getStrasse());
	}

	@Test
	public void testeUpdateInvalidAdresse() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste UpdateInvalidAdresse Adresse= {0}",
				ADRESSE_ID_UPDATE_EXISTS);

		// Zu updatende Adresse suchen
		Adresse ad = as.findAdresseByAdresseID(ADRESSE_ID_UPDATE_EXISTS,
				LOCALE_DEFAULT);

		// Erwartete Fehlermeldung
		thrown.expect(AdresseValidationException.class);

		// Neuer fehlerhafter Ort wird dem Adresse Objekt hinzugefügt
		ad.setOrt("-ki r.lach");

		// neue Werte werden geprüft und geupdated
		as.updateAdresse(ad, LOCALE_DEFAULT);

		// Prüfen ob Adresse mit neuer Strasse gefunden wird
		Adresse adUpdate = as.findAdresseByAdresseID(ADRESSE_ID_UPDATE_EXISTS,
				LOCALE_DEFAULT);

		assertThat(adUpdate.getStrasse(), is(STRASSE_UPDATE_EXISTS));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste UpdateInvalidAdresse UpdateAdresse= {0}",
				adUpdate.getStrasse());
	}

	@Test
	public void testeFindExistingAdresseByOrt() {

		// Log
		LOGGER.log(Level.DEBUG,
				"Test Beginn: Teste FindExistingAdresseByOrt Ort= {0}",
				ORT_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByOrt(FetchType.NUR_ADRESSE,
				ORT_EXISTS, LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(1));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseByOrt Gefunden={0}",
				ad.size());

	}

	@Test
	public void testeFindNonExistingAdresseByOrt() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingAdresseByOrt Ort= {0}",
				ORT_NOT_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByOrt(FetchType.NUR_ADRESSE,
				ORT_NOT_EXISTS, LOCALE_DEFAULT);

		// Teste ob Adresse gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindNonExsistingAdresseByOrt Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindAdresseByOrtWithValidationError() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: FindAdresseByOrtWithValidaitonError Ort={0}",
				INVALID_ORT);

		// Fehler erwarten
		thrown.expect(AdresseValidationException.class);

		// Adressen suchen
		List<Adresse> ad = as.findAdresseByOrt(FetchType.NUR_ADRESSE,
				INVALID_ORT, LOCALE_DEFAULT);

		// Testen ob wirklich nichts gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindAdresseByOrtWithValidaitonError Gefunden: {0}",
				ad.size());
	}

	@Test
	public void testeFindExistingAdresseByStrasse() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindExistingAdresseByStrasse Strasse= {0}",
				STRASSE_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByStrasse(FetchType.NUR_ADRESSE,
				STRASSE_EXISTS, LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(1));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseByStrasse Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindNonExistingAdresseByStrasse() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingAdresseByStrasse Strasse={0}",
				STRASSE_NOT_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByStrasse(FetchType.NUR_ADRESSE,
				STRASSE_NOT_EXISTS, LOCALE_DEFAULT);

		// Teste ob wirklich nichts gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingAdresseByStrasse Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindAdresseByStrasseWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindAdresseByStrasseWithValidationError Strasse= {0}",
				INVALID_STRASSE);

		// Fehler erwarten
		thrown.expect(AdresseValidationException.class);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByStrasse(FetchType.NUR_ADRESSE,
				INVALID_STRASSE, LOCALE_DEFAULT);

		// Teste ob wirklich nichts gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindAdresseByStrasseWithValidationError Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindExistingAdresseByStrasseFetchKunde() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingAdresseByStrasseFetchKunde Strasse= {0}",
				STRASSE_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByStrasse(FetchType.MIT_KUNDE,
				STRASSE_EXISTS, LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(1));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseByStrasseFetchKunde Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindExistingAdresseByPLZ() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindExistingAdresseByPlz Ort= {0}",
				ORT_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByPLZ(FetchType.NUR_ADRESSE,
				PLZ_EXISTS, LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(1));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseByPlz Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindNonExistingAdresseByPLZ() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingAdresseByPlz Plz= {0}",
				PLZ_NOT_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByPLZ(FetchType.NUR_ADRESSE,
				PLZ_NOT_EXISTS, LOCALE_DEFAULT);

		// Teste ob wirklich nichts gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingAdresseByPlz Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindAdresseByPLZWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindAdresseByPLZWithValidationError Plz= {0}",
				INVALID_PLZ);

		// Fehler erwarten
		thrown.expect(AdresseValidationException.class);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByPLZ(FetchType.NUR_ADRESSE,
				INVALID_PLZ, LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(0));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindAdresseByPLZWithValidationError Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindExistingAdresseByPLZFetchKunde() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindExistingAdresseByPLZFetchKunde Plz= {0}",
				PLZ_EXISTS);

		// Suche in Datenbank
		List<Adresse> ad = as.findAdresseByPLZ(FetchType.MIT_KUNDE, PLZ_EXISTS,
				LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.size(), is(1));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseByPLZFetchKunde Gefunden= {0}",
				ad.size());
	}

	@Test
	public void testeFindExistingAdresseById() {

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST BEGINN: Teste FindExistingAdresseById Adresse_Id= {0}",
				ADRESSE_ID_EXISTS);

		// Suche in Datenbank
		Adresse ad = as.findAdresseByAdresseID(ADRESSE_ID_EXISTS,
				LOCALE_DEFAULT);

		// Test ob ID gefunden wurde
		assertThat(ad.getAdresseID(), is(ADRESSE_ID_EXISTS));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindExistingAdresseById Gefunden= {0}",
				ad.getAdresseID());
	}

	@Test
	public void testeFindNonExistingAdresseById() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindNonExistingAdresseById Adresse_Id= {0}",
				ADRESSE_ID_NOT_EXISTS);

		// Suche in Datenbank
		Adresse ad = as.findAdresseByAdresseID(ADRESSE_ID_NOT_EXISTS,
				LOCALE_DEFAULT);

		// Test ob wirklich nichts gefunden wurde
		assertThat(ad, is(nullValue()));

		// Log
		LOGGER.log(Level.DEBUG,
				"TEST ENDE: Teste FindNonExistingAdresseById Gefunden= {0}", ad);
	}

	@Test
	public void testeFindAdresseByIdWithValidationError() {

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST BEGINN: Teste FindAdresseByIdWithValidationError Adresse_Id= {0}",
				INVALID_ADRESSE_ID);

		// Fehler erwarten
		thrown.expect(AdresseValidationException.class);

		// Suche in Datenbank
		Adresse ad = as.findAdresseByAdresseID(INVALID_ADRESSE_ID,
				LOCALE_DEFAULT);

		// Test ob Adresse gefunden wurde
		assertThat(ad.getAdresseID(), is(1));

		// Log
		LOGGER.log(
				Level.DEBUG,
				"TEST ENDE: Teste FindAdresseByIdWithValidationError Gefunden= {0}",
				ad.getAdresseID());
	}
}
