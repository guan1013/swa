package de.shop.test.kundenverwaltung.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.PersistenceException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.test.util.AbstractDomainTest;

/**
 * Die Klasse KundeTest testet die Funktionalit‰t der Klasse Kunde.
 * 
 * Folgende Tests werde durchlaufen:
 * 
 * - Suche eines vorhandenen Kunden<br>
 * - Suche eines nicht vorhandenen Kunde<br>
 * - Anlegen eines neuen Kunden<br>
 * - Anlegen eines fehlerhaften Kunden<br>
 * - Update von Kundendaten<br>
 * 
 * @see Kunde
 * @author Matthias Schnell
 * 
 */
@RunWith(Arquillian.class)
public class KundeTest extends AbstractDomainTest {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Integer ID_EXIST = Integer.valueOf(100);
	private static final Kunde NEW_KUNDE_TRUE = new Kunde("Doe", "John",
			"jd@mail.com");
	private static final Kunde NEW_KUNDE_FALSE = new Kunde("Doe", "John",
			"jd@mail.com");
	private static final String NEW_EMAIL = "mia.mueller@gmail.com";
	private static final Integer ID_NOT_EXIST = Integer.valueOf(9999);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Legt einen neuen Kunden in der Datenbank an. Die Kundedaten sind
	 * syntaktisch korrekt.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testeAddKundeTrue() {

		// In Datenbank speichern
		getEntityManager().persist(NEW_KUNDE_TRUE);

		// Kunde in der DB suchen
		List<Kunde> kunden = getEntityManager()
				.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
				.setParameter("mail", NEW_KUNDE_TRUE.getEmail())
				.getResultList();

		// Testen ob Kunde gefunden wurde
		assertThat(kunden.size(), is(1));

	}
	
	/**
	 * Legt einen neuen Kunden in der Datenbank an. Die Kundedaten sind
	 * syntaktisch korrekt, doch E-mail ist doppelt belegt.
	 */
	@Test
	public void testeAddInvalidKunde() {

		// Erwarte Fehler
		thrown.expect(PersistenceException.class);

		// In Datenbank speichern
		getEntityManager().persist(NEW_KUNDE_TRUE);
		getEntityManager().persist(NEW_KUNDE_FALSE);

		// Mit Datenbank synchronisieren
		getEntityManager().flush();

	}
	
	/**
	 * Es wird ein Kunde mit der Kundennummer 100 gesucht. Dieser Kunde ist in
	 * der Datenbank vorhanden und muss gefunden werden.
	 */
	@Test
	public void testeFindExistingKundeById() {

		// Suche in Datenbank
		Kunde kundeExist = getEntityManager().find(Kunde.class, ID_EXIST);

		// Teste ob Kunde existiert
		assertThat(kundeExist.getKundeID(), is(ID_EXIST));

	}

	/**
	 * Es wird ein Kunde mit der Kundennummer 9999 gesucht. Diese ID verl‰sst
	 * den Nummerkreis unser Kunden ID und darf nicht gefunden werden.
	 */
	@Test
	public void testeFindNonExistingKundeById() {

		// Suche in Datenbank
		Kunde kundeNotExist = getEntityManager()
				.find(Kunde.class, ID_NOT_EXIST);

		// Teste ob Kunde nicht existiert
		assertThat(kundeNotExist, is(nullValue()));

	}

	/**
	 * Ein vorhandener Kunde wird zuerst gesucht, dann updated und anschlieﬂend
	 * wieder gesucht.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void updateKunde() {

		// ID des Kunden
		Kunde kundenUpdate = getEntityManager().find(Kunde.class, ID_EXIST);

		// Email ‰ndern
		kundenUpdate.setEmail(NEW_EMAIL);

		// Update speichern
		getEntityManager().merge(kundenUpdate);

		// Kunde anhand neuer Email suchen
		List<Kunde> kunden = getEntityManager()
				.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
				.setParameter("mail", NEW_EMAIL).getResultList();

		// Test ob Kunde gefunden wurde
		assertThat(kunden.size(), is(1));

	}
}