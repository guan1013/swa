package de.shop.produktverwaltung.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.util.AbstractDomainTest;

/**
 * Die Klasse ProduktdatenTest testet die Funktionalität der Klasse
 * Produktdaten. Folgende Testcases werde durchlaufen:
 * 
 * - Suche von vorhandenen Produktdaten anhand ID<br>
 * - Suche von nicht vorhandenen Produktdaten<br>
 * - Ändern von vorhandenen Produktdaten<br>
 * - Einfügen von inkonsistenten Produktdaten<br>
 * 
 * @see Produktdaten
 * @author Andreas Güntzel
 * 
 */
@RunWith(Arquillian.class)
public class ProduktdatenTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Integer VORHANDENE_PRODUKTDATEN_ID = new Integer(404);

	private static final Integer NICHT_VORHANDENE_PRODUKTDATEN_ID = new Integer(
			33152);
	
	private static final int TEST_ANZAHL = 4;

	private static final String TEST_STRING_FARBE = "JUnitBlau";

	private static final String TEST_STRING_GROESSE = "JUnitXXL";
	
	private static final Produkt NICHT_GUELTIGES_PRODUKT = null;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// /////////////////////////////////////////////////////////////////////
	// TEST-METHODS

	@Test
	public void sucheVorhandeneProduktdatenID() {

		// Suche in Datenbank
		Produktdaten gefundeneProduktdaten = getEntityManager().find(
				Produktdaten.class, VORHANDENE_PRODUKTDATEN_ID);

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundeneProduktdaten.getProduktdatenID(), is(VORHANDENE_PRODUKTDATEN_ID));
	}

	@Test
	public void sucheNichtVorhandeneProduktdatenID() {

		// Suche in Datenbank
		Produktdaten gefundeneProduktdaten = getEntityManager().find(
				Produktdaten.class, NICHT_VORHANDENE_PRODUKTDATEN_ID);

		// Teste ob wirklich kein Produkt gefunden wurde
		assertThat(gefundeneProduktdaten, is(nullValue()));

	}

	@Test
	public void aendereVorhandeneProduktdaten() {
		
		// Suche in Datenbank
		Produktdaten gefundeneProduktdaten = getEntityManager().find(
				Produktdaten.class, VORHANDENE_PRODUKTDATEN_ID);

		// Produktdaten ändern
		gefundeneProduktdaten.setFarbe(TEST_STRING_FARBE);
		gefundeneProduktdaten.setGroesse(TEST_STRING_GROESSE);

		// Speichern in der Datenbank
		getEntityManager().merge(gefundeneProduktdaten);

		// Produktdaten neu abfragen
		Produktdaten neueProduktdaten = getEntityManager().find(
				Produktdaten.class, VORHANDENE_PRODUKTDATEN_ID);

		// Vergleiche auf Änderungen
		assertThat(neueProduktdaten.getFarbe(), is(TEST_STRING_FARBE));

	}

	@Test
	public void fuegeUngueltigeProduktdatenHinzu() {

		// Neue Produktdaten anlegen
		Produktdaten neueProduktdaten = new Produktdaten();
		neueProduktdaten.setAnzahlVerfuegbar(TEST_ANZAHL);
		neueProduktdaten.setFarbe(TEST_STRING_FARBE);
		neueProduktdaten.setGroesse(TEST_STRING_GROESSE);
		neueProduktdaten.setProdukt(NICHT_GUELTIGES_PRODUKT);

		// Erwarte Fehler
		thrown.expect(ConstraintViolationException.class);

		// Versuchen in Datenbank anzulegen
		getEntityManager().persist(neueProduktdaten);

		// Datenbank synchronisieren
		getEntityManager().flush();

	}
}