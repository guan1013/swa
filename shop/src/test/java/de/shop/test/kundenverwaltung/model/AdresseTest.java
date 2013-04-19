package de.shop.test.kundenverwaltung.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import de.shop.test.util.AbstractDomainTest;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Kunde;

import org.junit.Test;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Die Klasse AdresseTest testet die Funktionalität der Klasse Adresse. Folgende
 * Testcases werden durchlaufen.
 * 
 * - Suche einer vorhandenen Adresse<br>
 * - Suche einer nicht vorhandenen Adresse<br>
 * - Ändern einer vorhandenen Adresse<br>
 * 
 * @see Kunde
 * @author Yannick Gentner
 * 
 */

@RunWith(Arquillian.class)
public class AdresseTest extends AbstractDomainTest {

	private static final Integer ID_EXISTS = new Integer(203);

	private static final Integer ID_NOT_EXISTS = new Integer(20000);

	private static final String ORT_EXISTS = "Wiesental";

	private static final Integer PLZ_EXISTS = new Integer(12345);

	private static final String STRASSE_EXISTS = "Stadtweg 3";

	private static final Integer ID_KUNDE_EXISTS = new Integer(103);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testeFindExistingAdresseById() {

		// Suche in Datenbank
		Adresse gefundeneAdresse = getEntityManager().find(Adresse.class,
				ID_EXISTS);

		// Teste ob die Adresse gefunden wurde
		assertThat(gefundeneAdresse.getAdresseID(), is(ID_EXISTS));

	}

	/**
	 * Die Methode sucht eine Adresse anhand eines Primärschlüssels (20000). Es
	 * darf kein entsprechender Datensatz gefunden werden, da dieser
	 * Primärschlüssel in der Datenbank nicht vorhanden ist.
	 */
	@Test
	public void testeFindNonExistingAdresseById() {

		// Suche in Datenbank
		Adresse gefundeneAdresse = getEntityManager().find(Adresse.class,
				ID_NOT_EXISTS);

		// Teste ob wirklich keine Adresse gefunden wurde
		assertThat(gefundeneAdresse, is(nullValue()));
	}

	@Test
	public void testeUpdateAdresse() {

		// ID der zu ändernden Adresse
		Adresse gefundeneAdresse = getEntityManager().find(Adresse.class,
				ID_EXISTS);

		// Ort ändern
		gefundeneAdresse.setOrt(ORT_EXISTS);

		// Adresse speichern
		getEntityManager().merge(gefundeneAdresse);

		// Adresse mit neuem Ort suchen
		Adresse neueAdresse = getEntityManager().find(Adresse.class, ID_EXISTS);

		// Teste ob die Adresse mit neuem Ort gefunden wurde
		assertThat(neueAdresse.getOrt(), is(ORT_EXISTS));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testeAddAdresse() {

		// Neue Adresse anlegen
		Adresse neueAdresse = new Adresse();
		neueAdresse.setOrt(ORT_EXISTS);
		neueAdresse.setPlz(PLZ_EXISTS);
		neueAdresse.setStrasse(STRASSE_EXISTS);
		Kunde gefundenerKunde = getEntityManager().find(Kunde.class,
				ID_KUNDE_EXISTS);
		neueAdresse.setKunde(gefundenerKunde);

		// In Datenbank speichern
		getEntityManager().persist(neueAdresse);

		// Adresse in DB suchen
		List<Adresse> adressen = getEntityManager()
				.createNamedQuery(Adresse.ADRESSE_BY_WOHNORT)
				.setParameter("ort", ORT_EXISTS).getResultList();

		Boolean test = false;
		for (Adresse a : adressen) {
			if (a.getOrt().equals(neueAdresse.getOrt())) {
				test = true;
			}
			else {
				test = false;
				break;
			}
		}

		assertThat(test, is(true));

	}

}