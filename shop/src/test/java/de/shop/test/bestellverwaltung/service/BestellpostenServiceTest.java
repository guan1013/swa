package de.shop.test.bestellverwaltung.service;

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
import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellpostenService;
import de.shop.bestellverwaltung.service.BestellpostenService.FetchType;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.test.util.AbstractDomainTest;
import de.shop.util.exceptions.BestellpostenValidationException;
import de.shop.util.exceptions.InvalidBestellpostenIdException;
import de.shop.util.exceptions.InvalidBestellungIdException;

/**
 * Die Klasse BestellpostenServiceTest testet die Funktionalität der Klasse
 * BestellpostenService. Die in BestellpostenService implementierten Use-Cases werden durch
 * folgenden Test-Cases getestet:<br>
 * 
 * # Use-Case: Hinzufügen Bestellposten<br>
 * # Test-Cases:<br>
 * - Hinzufügen gültiger Bestellposten<br>
 * - Hinzufügen ungültiger Bestellposten<br>
 * 
 * # Use-Case: Suche Bestellposten<br>
 * # Test-Cases:<br>
 * - Suche nach gültiger ID<br>
 * - Suche nach nicht vorhandener ID<br>
 * - Suche nach ungültiger ID<br>
 * - Suche alle Bestellposten
 * 
 * # Use-Case: Suche Bestellposten nach BestellungFK<br>
 * # Test-Cases:<br>
 * - Suche nach gültigem BestellungID<br>
 * - Suche nach nicht vorhandener BestellungID<br>
 * - Suche nach ungültiger BestellungID<br>
 * 
 # Use-Case: Update Bestellposten<br>
 * # Test-Cases:<br>
 * - Uptade mit gültigen Daten<br>
 * - Uptade mit ungültigen Daten<br>
 *  
 * @author Dennis Brull
 * @see Bestellposten
 * @see BestellpostenService
 * 
 */

@RunWith(Arquillian.class)
public class BestellpostenServiceTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	// /////////////////////////////////////////////////////////////////////
	@Inject
	private BestellpostenService bps;;
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	public static final Integer BESTELLPOSTEN_ID_VORHANDEN = Integer.valueOf(604);
	public static final Integer BESTELLPOSTEN_ID_NICHT_VORHANDEN = Integer.valueOf(777);
	public static final Integer UNGUELTIGE_ID = Integer.valueOf(-12345);
	public static final Integer BESTELLUNG_FK = Integer.valueOf(503);
	public static final Integer BESTELLUNG_FK_NICHT_VORHANDEN = Integer.valueOf(999);
	public static final Integer BESTELLUNG_FK_UNGUELTIG = Integer.valueOf(-100);
	public static final int GUELTIGE_ANZAHL = 9;
	public static final int UNGUELTIGE_ANZAHL = -1;
	public static final int ZWANZIG = 20;
	public static final Integer BSP_BP_ID_1 = Integer.valueOf(608);
	public static final Integer BSP_BP_ID_2 = Integer.valueOf(616);	
	private static final Locale DE = new Locale("de");

	// /////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////
	
	/**
	 * Es wird ein neuer Bestellposten mit korrekten Daten angelegt und anschließend
	 * gesucht. Wurde er gefunden, ist der Test korrekt.
	 */
	@Test
	public void testeAddBestellposten() {
		// Objekte, die für Anlegen nötig sind holen
		Bestellung nb = bps.findBestellpostenByIdObjekt(BSP_BP_ID_1, DE).getBestellung();
		Produktdaten npd = bps.findBestellpostenByIdObjekt(BSP_BP_ID_2, DE).getProduktdaten();
		
		//Bestellposten anlegen
		Bestellposten nbps = new Bestellposten(nb, npd, GUELTIGE_ANZAHL);
		
		
		bps.addBestellposten(nbps, DE);
		
		// Eben angelegten Bestellposten in der Datenbank suchen
		List<Bestellposten> nbp = bps.findBestellpostenById(
				FetchType.JUST_BESTELLPOSTEN, nbps.getBestellpostenID(), DE);

		// Prüfe ob Bestellposten gefunden wurde
		assertThat(nbp.size(), is(1));

	}
	
	/**
	 * Es wird ein neuer Bestellposten mit falschen Daten angelegt - Anzahl 
	 * ist negativund anschließend gesucht. Wird er nicht gefunden, ist der Test korrekt.
	 */
	@Test
	public void testeAddInvalidBestellposten() {
		// Objekte, die für Anlegen nötig sind holen
		Bestellung nb = bps.findBestellpostenByIdObjekt(BSP_BP_ID_1, DE).getBestellung();
		Produktdaten npd = bps.findBestellpostenByIdObjekt(BSP_BP_ID_2, DE).getProduktdaten();
		
		// Erwarteter Validierungsfehler
		thrown.expect(BestellpostenValidationException.class);
		
		//Bestellposten anlegen
		Bestellposten nbps = new Bestellposten(nb, npd, UNGUELTIGE_ANZAHL);
		bps.addBestellposten(nbps, DE);
		
		// Eben angelegten Bestellposten in der Datenbank suchen
		List<Bestellposten> nbp = bps.findBestellpostenById(
				FetchType.JUST_BESTELLPOSTEN, nbps.getBestellpostenID(), DE);

		// Prüfe ob Bestellposten gefunden wurde
		assertThat(nbp.size(), is(0));

	}
	
	/**
	 * Suche nach einem Bestellposten mit der ID: 601. Dieser ist in der Datenbank
	 * vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingBestellpostenById() {
		// Suche in Datenbank
		Bestellposten bp = bps.findBestellpostenByIdObjekt(BESTELLPOSTEN_ID_VORHANDEN, DE);

		// Test ob Bestellposten existiert
		assertThat(bp.getBestellpostenID(), is(BESTELLPOSTEN_ID_VORHANDEN));

	}
	
	/**
	 * Suche nach einem Bestellposten mit der ID: 777. Dieser ist in der Datenbank
	 * nicht vorhanden.
	 */
	@Test
	public void testeFindNonExistingBestellpostenById() {
		// Suche in Datenbank
		List<Bestellposten> bp = bps.findBestellpostenById(
				FetchType.JUST_BESTELLPOSTEN, BESTELLPOSTEN_ID_NICHT_VORHANDEN, DE);

		// Test ob Bestellposten existiert
		assertThat(bp.size(), is(0));
	}
	
	
	/**
	 * Suche nach einem Bestellposten mit der ID: 123456. Eine solche ID
	 * ist nicht zulässig
	 */
	@Test
	public void testeFindBestellpostenByIdWithValidationError() {
		
		// Erwarteter Validierungsfehler
		thrown.expect(InvalidBestellpostenIdException.class);
		
		// Suche in Datenbank
		List<Bestellposten> bp = bps.findBestellpostenById(
				FetchType.JUST_BESTELLPOSTEN, UNGUELTIGE_ID, DE);
				
		// Test ob Bestellposten existiert
		assertThat(bp.size(), is(0));

	}
	
	/**
	 * Suche alle Bestellposten
	 */
	@Test
	public void testefindeallebestellposten() {
		List <Bestellposten> bp = bps.findAllBestellposten();
	
		assertThat(bp.size(), is(ZWANZIG));
	}
		
	/**
	 * Es werden Bestellposten nach BestellungID 503 gesucht.
	 * Es muss ein Bestellposten mit ID 606 gefunden werden
	 */
	@Test
	public void testeFindExistingBestellpostenByBestellungId() {

		List<Bestellposten> bp = bps.findBestellpostenByBestellungId(FetchType.JUST_BESTELLPOSTEN,
				BESTELLUNG_FK, DE);

		assertThat(bp.size(), is(1));
	}
	
	/**
	 * Es werden Bestellposten nach BestellungID 999 gesucht.
	 * Eine Bestellung mit solcher ID gibt es nicht
	 */
	@Test
	public void testeFindNonExistingBestellpostenByBestellungId() {

		List<Bestellposten> bp = bps.findBestellpostenByBestellungId(FetchType.JUST_BESTELLPOSTEN,
				BESTELLUNG_FK_NICHT_VORHANDEN, DE);

		assertThat(bp.size(), is(0));
	}
	
	/**
	 * Es werden Bestellposten nach BestellungID -100 gesucht.
	 * Eine Bestellung mit solcher ID darf es nicht geben
	 */
	@Test
	public void testeFindBestellpostenByBestellungIdWithValidationError() {

		thrown.expect(InvalidBestellungIdException.class);
		List<Bestellposten> bp = bps.findBestellpostenByBestellungId(FetchType.JUST_BESTELLPOSTEN,
				BESTELLUNG_FK_UNGUELTIG, DE);

		assertThat(bp.size(), is(0));
	}
	
	/**
	 * Der Bestellposten mit der ID 601 wird gesucht. Diese ID ist in der Datenbank
	 * vorhanden und muss gefunden werden. Die Anzahl wird zunächst im
	 * Objekt geändert und dann an die Datenbank übergeben.Anschließend wird
	 * die Anzahl mit der Konstante abgegliechen
	 * */
	@Test
	public void testeUpdateBestellposten() {

		// Opfer suchen
		Bestellposten ubp = bps.findBestellpostenByIdObjekt(BESTELLPOSTEN_ID_VORHANDEN, DE);

		// ID wird verändert
		ubp.setAnzahl(GUELTIGE_ANZAHL);

		// neue Werte werden geprüft und verändert
		bps.updateBestellposten(ubp,  DE);

		// nochmal suchen
		int anz = bps.findBestellpostenByIdObjekt(BESTELLPOSTEN_ID_VORHANDEN, DE).getAnzahl();

		// Abfrage ob Änderung vorhanden
		assertThat(anz, is(GUELTIGE_ANZAHL));

	}
	
	/**
	 * Ein Bestellposten wird gesucht und dessen Anzahl fehlerhaft (nach Validierung)
	 * geändert. Wird die korrekte Fehlermeldung geworfen, ist der Test
	 * erfolgreich.
	 */
	@Test
	public void testeUpdateInvalidBestellposten() {
		// Opfer suchen
		Bestellposten bp = bps.findBestellpostenByIdObjekt(BESTELLPOSTEN_ID_VORHANDEN, DE);
		
		// Erwarteter Validierungsfehler
		thrown.expect(BestellpostenValidationException.class);
		
		// Neue Anzahl wird eingefügt
		bp.setAnzahl(UNGUELTIGE_ANZAHL);

		// update
		bps.updateBestellposten(bp, new Locale("de"));

		// Vergleichsobjekt anlegen
		Bestellposten bpUpdated = bps.findBestellpostenByIdObjekt(BESTELLPOSTEN_ID_VORHANDEN, DE);

		// Prüfung ob Veränderung stadtfand
		assertThat(bpUpdated.getAnzahl(), is(bp.getAnzahl()));

	}
	
}