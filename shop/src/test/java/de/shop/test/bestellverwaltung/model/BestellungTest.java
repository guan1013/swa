package de.shop.test.bestellverwaltung.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.test.util.AbstractDomainTest;

/**
 * Die Klasse testet die Funktionalität der Klasse Bestellung. Folgende
 * Testcases werden durchlaufen:
 * 
 * - Suche einer vorhandenen Bestellung anhand ID<br>
 * - Suche einer nicht vorhandenen Bestellung<br>
 * - Ändern einer vorhandenen Bestellung<br>
 * - Ändern einer vorhandenen Bestellung zu inkonsistenter Bestellung<br>
 * - Einfügen einer Bestellung<br>
 * - Einfügen einer inkonsistenten Bestellung<br>
 * 
 * @see Bestellung
 * @author Andreas Güntzel & Matthias Schnell
 * 
 */
@RunWith(Arquillian.class)
public class BestellungTest extends AbstractDomainTest {

	private static final int VORHANDENE_ID = Integer.valueOf(510);
	private static final int NICHT_VORHANDENE_ID = Integer.valueOf(19123);
	private static final Integer PRODUK_ID_EXIST = Integer.valueOf(412);
	private static final int ID_MIT_MEHREREN_BESTELLPOSTEN = Integer
			.valueOf(515);
	private static final int KONSTANTE_2157 = Integer.valueOf(2157);
	private static final int KONSTANTE_10 = Integer.valueOf(10);
	private static final int KONSTANTE_100 = Integer.valueOf(100);
	private static final int KUNDE_ID_EXIST = Integer.valueOf(103);
	private static final double KONSTANTE_2160_0 = Double.valueOf(2160.0);
	private static final double KONSTANTE_2150_0 = Double.valueOf(2150.0);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@SuppressWarnings("unchecked")
	@Test
	public void testeAddBestellung() {

		Bestellung newBestellungTrue = new Bestellung();
		Produktdaten produktVorhanden = getEntityManager().find(
				Produktdaten.class, PRODUK_ID_EXIST);
		@SuppressWarnings("unused")
		Bestellposten newBestellposten = new Bestellposten(newBestellungTrue,
				produktVorhanden, 1);

		// Neue Bestellung anlegen
		newBestellungTrue.setGesamtpreis(KONSTANTE_2157);

		// Die Bestellung einem bereits vorhandenen Kunden hinzufügen
		Kunde kunde = getEntityManager().find(Kunde.class, KUNDE_ID_EXIST);
		newBestellungTrue.setKunde(kunde);

		getEntityManager().persist(newBestellungTrue);

		List<Bestellung> listeBestellungen = getEntityManager()
				.createNamedQuery(Bestellung.BESTELLUNG_BY_PREISSPANNE)
				.setParameter("min", KONSTANTE_2150_0)
				.setParameter("max", KONSTANTE_2160_0).getResultList();

		assertThat(listeBestellungen.size(), is(1));

	}

	@Test
	public void testeAddInvalidBestellung() {

		// Neue Bestellung anlegen
		Bestellung newBestellungFalse = new Bestellung();
		newBestellungFalse.setGesamtpreis(KONSTANTE_100);
		newBestellungFalse.setKunde(null);
		newBestellungFalse.addBestellposten(new Bestellposten());

		// Mit Fehlermeldung rechnen
		thrown.expect(ConstraintViolationException.class);

		// In Datenbank speichern
		getEntityManager().persist(newBestellungFalse);

		// Datenbank synchronisieren
		getEntityManager().flush();
	}

	/**
	 * Suche eine Bestellung mit der ID 510. Diese ist in der Datenbank
	 * vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingBestellungById() {

		// Suche in Datenbank
		Bestellung gefundeneBestellung = getEntityManager().find(
				Bestellung.class, VORHANDENE_ID);

		// Teste ob das Produkt gefunden wurde
		assertThat(gefundeneBestellung.getBestellungID(), is(VORHANDENE_ID));

	}

	/**
	 * Suche eine Bestellung mit der ID 19123. Diese ist nicht in der Datenbank
	 * vorhand
	 */
	@Test
	public void testeFindNonExistingBestellungById() {

		// Suche in Datenbank
		Bestellung gefundeneBestellung = getEntityManager().find(
				Bestellung.class, NICHT_VORHANDENE_ID);

		// Teste ob wirklich keine Bestellung gefunden wurde
		assertThat(gefundeneBestellung, is(nullValue()));

	}

	@Test
	public void testeUpdateBestellung() {

		// Suche in Datenbank
		Bestellung gefundeneBestellung = getEntityManager().find(
				Bestellung.class, ID_MIT_MEHREREN_BESTELLPOSTEN);

		// Gesamtpreis ändern
		gefundeneBestellung.setGesamtpreis(KONSTANTE_10);

		// Speichern
		getEntityManager().merge(gefundeneBestellung);

		// Neu abfragen
		Bestellung geaenderteBestellung = getEntityManager().find(
				Bestellung.class, ID_MIT_MEHREREN_BESTELLPOSTEN);

		// Testen ob Anzahl Posten übereinstimmen
		assertThat(geaenderteBestellung.getGesamtpreis(),
				is(gefundeneBestellung.getGesamtpreis()));

	}
}