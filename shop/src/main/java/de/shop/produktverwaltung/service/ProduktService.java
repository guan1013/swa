package de.shop.produktverwaltung.service;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produkt_;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.ConcurrentDeletedException;
import de.shop.util.exceptions.ProduktValidationException;

/**
 * Die Klasse ProduktService implementiert die Use-Cases, die die Klasse Produkt
 * betreffen. Folgende Use-Cases sind implementiert:<br>
 * 
 * - Suche Produkt nach ID<br>
 * - Suche Produkt nach Hersteller<br>
 * - Suche Produkt nach Beschreibung<br>
 * - Hinzufügen Produkt<br>
 * 
 * @author Andreas Güntzel
 * @see Produkt
 * 
 */
public class ProduktService implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final long serialVersionUID = -2526893314875328558L;

	@PersistenceContext
	private transient EntityManager entityManager;

	@Inject
	private ValidatorProvider validatorProvider;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	@SuppressWarnings("unchecked")
	public List<String> findGroessenByPrefix(String prefix) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche Groessen by Prefix");

		final List<String> results = entityManager
				.createNamedQuery(Produkt.PRODUKT_LISTE_GROESSEN)
				.setParameter("prefix", prefix).getResultList();

		// Log
		LOGGER.log(FINER, "ENDE: Suche Groessen by Prefix");

		return results;

	}

	@SuppressWarnings("unchecked")
	public List<String> findHerstellerPrefix(String prefix) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche Hersteller by Prefix");

		final List<String> results = entityManager
				.createNamedQuery(Produkt.PRODUKT_LISTE_HERSTELLER)
				.setParameter("prefix", prefix).getResultList();

		// Log
		LOGGER.log(FINER, "ENDE: Suche Hersteller by Prefix");

		return results;

	}
	
	@SuppressWarnings("unchecked")
	public List<String> findBeschreibungPrefix(String prefix) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche Beschreibung by Prefix");

		final List<String> results = entityManager
				.createNamedQuery(Produkt.PRODUKT_LISTE_PRODUKTE)
				.setParameter("prefix", prefix).getResultList();

		// Log
		LOGGER.log(FINER, "ENDE: Suche Beschreibung by Prefix");

		return results;

	}

	public Produkt addProdukt(Produkt neuesProdukt, Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Add neues Produkt={0}", neuesProdukt);

		// Validierung Produkt
		checkViolations(getValidator(locale).validate(neuesProdukt,
				Default.class));

		// Neues Produkt speichern
		entityManager.persist(neuesProdukt);

		// Log
		LOGGER.log(FINER, "ENDE: Add neues Produkt={0}", neuesProdukt);

		return neuesProdukt;
	}

	@SuppressWarnings("unchecked")
	public List<Produkt> findProdukte() {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach allen Produkten");

		final List<Produkt> results = entityManager.createNamedQuery(
				Produkt.PRODUKT_KOMPLETT).getResultList();

		// Log
		LOGGER.log(FINER, "ENDE: Suche nach allen Produkten");

		return results;

	}

	/**
	 * Suche Produkt nach ID
	 * 
	 * @param id
	 *            ProduktID
	 * @return Gefundenes Produkt mit entsprechender ID
	 */
	public Produkt findProduktByID(Integer id, FetchType fetchType,
			Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach ProduktID={0}", id);

		// Validierung
		checkViolations(getValidator(locale).validateValue(Produkt.class,
				Produkt_.produktId.getName(), id, IdGroup.class));

		// Suchen in DB
		Produkt result = null;
		switch (fetchType) {
		case KOMPLETT:
			result = (Produkt) entityManager
					.createNamedQuery(Produkt.PRODUKT_ID_FETCH)
					.setParameter("id", id).getSingleResult();
			break;

		case NUR_PRODUKTE:
			result = entityManager.find(Produkt.class, id);
			break;

		default:
			throw new RuntimeException("Kein Fetch-Type ausgewählt");
		}

		// Log
		LOGGER.log(FINER, "ENDE: Suche nach ProduktID. Ergebnis={0}", result);

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Produkt> findProduktByHersteller(String hersteller,
			FetchType fetchType, Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach Hersteller={0}", hersteller);

		// Validierung Hersteller
		checkViolations(getValidator(locale).validateValue(Produkt.class,
				Produkt_.hersteller.getName(), hersteller));

		// Named Query aufrufen
		final List<Produkt> results = entityManager
				.createNamedQuery(Produkt.PRODUKT_BY_HERSTELLER)
				.setParameter("hersteller", hersteller).getResultList();

		// Log
		LOGGER.log(FINER,
				"ENDE: Suche nach Hersteller. Ergebnis: {0} Produkt(e)",
				results.size());

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<Produkt> findProduktByBeschreibung(String beschreibung,
			FetchType fetchType, Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach Beschreibung={0}", beschreibung);

		// Validierung Beschreibung
		checkViolations(getValidator(locale).validateValue(Produkt.class,
				Produkt_.beschreibung.getName(), beschreibung));

		// Named Query aufrufen
		final List<Produkt> results = entityManager
				.createNamedQuery(Produkt.PRODUKT_BY_LIKE_BESCHREIBUNG)
				.setParameter("beschreibung", beschreibung).getResultList();

		// Log
		LOGGER.log(FINER,
				"ENDE: Suche nach Beschreibung. Ergebnis: {0} Produkt(e)",
				results.size());

		return results;
	}

	public ProduktService updateProdukt(Produkt produkt, Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Update Produkt={0}", produkt);

		// Validierung Produkt
		checkViolations(getValidator(locale).validate(produkt, Default.class,
				IdGroup.class));

		entityManager.detach(produkt);

		final Produkt tmp = findProduktByID(produkt.getProduktId(),
				FetchType.NUR_PRODUKTE, locale);
		if (tmp == null) {
			throw new ConcurrentDeletedException(produkt.getProduktId());
		}
		else {
			entityManager.detach(tmp);
		}

		// Neues Produkt speichern
		entityManager.merge(produkt);

		// Log
		LOGGER.log(FINER, "ENDE: Update Produkt={0}", produkt);

		return this;
	}

	private Validator getValidator(Locale l) {
		return validatorProvider.getValidator(l);
	}

	/*
	 * Überprüft, ob in dem übergebenen Set Vialations vorhanden sind und wirft
	 * ggf. eine Exception
	 */
	private void checkViolations(Set<ConstraintViolation<Produkt>> violations) {

		if (!violations.isEmpty()) {
			LOGGER.log(SEVERE, "{0} Fehler bei der Validierung",
					violations.size());
			final StringBuffer buffer = new StringBuffer();
			final Iterator<ConstraintViolation<Produkt>> it = violations
					.iterator();
			while (it.hasNext()) {
				buffer.append(it.next().getMessage());
				buffer.append('\n');
			}
			throw new ProduktValidationException(buffer.toString());
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// INNER CLASSES

	public enum FetchType {
		KOMPLETT, NUR_PRODUKTE;
	}
}
