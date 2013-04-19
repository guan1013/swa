package de.shop.test.bestellverwaltung.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.test.util.AbstractDomainTest;

/**
 * Die Klasse BestellpostenTest testet die Funktionalität der Klasse
 * Bestellposten. Folgende Testcases werde durchlaufen:
 * 
 * - Suche eines vorhandenen Bestellpostens - DONE<br>
 * - Suche eines nicht vorhandenen Bestellpostens - DONE<br>
 * - Anlegen eines neuen Bestellpostens - DONE<br>
 * - Anlegen eines fehlerhaften Bestellpostens - DONE<br>
 * - Ändern eines vorhandenen Bestellpostens - DONE<br>
 * 
 * @see Bestellposten
 * @author Dennis Brull
 * 
 */

@RunWith(Arquillian.class)
public class BestellpostenTest extends AbstractDomainTest {

	private static final Integer VORHANDENE_ID = Integer.valueOf(602);
	private static final Integer UNGUELTIGE_ID = Integer.valueOf(123);
	private static final int ANZAHL = 9;
	private static final int UNGUELTIGE_ANZAHL = -1;
	public static final Integer BSP_BP_ID_1 = Integer.valueOf(608);
	public static final Integer BSP_BP_ID_2 = Integer.valueOf(616);
	private static final Integer ID_BESTELLUNG_EXIST = Integer.valueOf(507);
	private static final Integer ID_PRODUKTDATEN_EXIST = Integer.valueOf(413);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Die Methode sucht einen Bestellposten anhand des Primarschlüssels (601).
	 * Der Bestellposten muss in der Datenbank gefunden werden, da ein Datensatz
	 * mit diesem Primärschlüssel über die Testdaten in der Datenbank angelegt
	 * wurde.
	 */
	@Test
	public void sucheVorhandeneBestellpostenID() {

		// Suche in Datenbank
		Bestellposten gefundenerBestellposten = getEntityManager().find(
				Bestellposten.class, VORHANDENE_ID);

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundenerBestellposten.getBestellpostenID(),
				is(VORHANDENE_ID));

	}

	/**
	 * Es wird ein Bestellposten mit der Nummer 601 gesucht. Dieser Posten ist
	 * in der Datenbank vorhanden und muss gefunden werden.
	 */
	@Test
	public void searchBestellpostenTrue() {

		// Suche in Datenbank
		Bestellposten bestellpostenExist = getEntityManager().find(
				Bestellposten.class, VORHANDENE_ID);

		// Teste ob Kunde existiert
		assertThat(bestellpostenExist.getBestellpostenID(), is(VORHANDENE_ID));

	}

	/**
	 * Es wird ein Bestellposten mit der ID 123 gesucht. Einen Bestellposten 
	 * mit solcher ID gibt es nicht
	 */
	@Test
	public void searchBestellpostenFalse() {

		// Suche in Datenbank
		Bestellposten bestellpostenNotExist = getEntityManager().find(
				Bestellposten.class, UNGUELTIGE_ID);

		// Teste ob Kunde nicht existiert
		assertThat(bestellpostenNotExist, is(nullValue()));

	}

	/**
	 * Die Methode legt einen neuen Bestellposten an und versucht ihm in der
	 * Datenbank abzuspeichern.
	 */
	@Test
	public void createBestellpostenTrue() {

		Bestellung nb = getEntityManager().find(Bestellposten.class,
				BSP_BP_ID_1).getBestellung();

		Produktdaten npd = getEntityManager().find(Bestellposten.class,
				BSP_BP_ID_2).getProduktdaten();

		Bestellposten nbps = new Bestellposten(nb, npd, ANZAHL);

		// In Datenbank speichern
		getEntityManager().persist(nbps);

		// Kunde in der DB suchen
		Bestellposten bestellposten = getEntityManager().find(
				Bestellposten.class, nbps.getBestellpostenID());

		// Testen ob Kunde gefunden wurde
		assertThat(bestellposten.getAnzahl(), is(ANZAHL));
	}

	/*
	 * Legt einen neuen Bestellposten in der Datenbank an. Die Anzahl
	 * ist nicht korrekt, es muss ein Validierungsfehler geworfen werden
	 */
	@Test
	public void createBestellpostenFalse() {

		// Erwarte Fehermeldung
		thrown.expect(ConstraintViolationException.class);

		// Daten für Anlegen vorbereiten
		Produktdaten npd = getEntityManager().find(Produktdaten.class,
				ID_PRODUKTDATEN_EXIST);
		Bestellung nb = getEntityManager().find(Bestellung.class,
				ID_BESTELLUNG_EXIST);

		// Bestellposten mit fehlerhafter Anzahl (-1) versuchen anzulegen
		Bestellposten nbps = new Bestellposten(nb, npd, UNGUELTIGE_ANZAHL);

		getEntityManager().persist(nbps);
		
		getEntityManager().flush();
	}

	/**
	 * Ein vorhandener Bestellposten wird zuerst gesucht, dann wird die Anzahl
	 * verändert. anschließend wieder gesucht.
	 */
	@Test
	public void updateBestellposten() {

		// Suche in Datenbank
		Bestellposten bestellpostenUpdate = getEntityManager().find(
				Bestellposten.class, VORHANDENE_ID);

		// Anzahl ändern
		bestellpostenUpdate.setAnzahl(ANZAHL);

		// Update speichern
		getEntityManager().merge(bestellpostenUpdate);

		// Bestellposten anhand erneut suchen
		Bestellposten bestellposten = getEntityManager().find(
				Bestellposten.class, VORHANDENE_ID);

		/**
		 * Test ob die Anzahl auf den neuen wert gesetzt wurde
		 */
		assertThat(bestellposten.getAnzahl(), is(ANZAHL));

	}

}
