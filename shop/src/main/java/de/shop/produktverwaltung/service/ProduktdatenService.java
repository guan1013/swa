package de.shop.produktverwaltung.service;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produkt_;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.domain.Produktdaten_;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.ProduktdatenValidationException;

/**
 * Die Klasse ProduktdatenService bietet Dienste zum Verwalten der Produktdaten.
 * Folgende Use-Cases werden durch diesen Service implementiert:
 * 
 * 1) Suche nach Produktdaten anhand von<br>
 * - a) Produktdaten-ID<br>
 * - b) Such-Filter<br>
 * 
 * 2) Hinzufügen neuer Produktdaten
 * 
 * 3) Aktualisieren (Bearbeiten) von Produktdaten
 * 
 * @see Produktdaten
 * @see SuchFilter
 * @author Andreas Güntzel
 */
public class ProduktdatenService implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final long serialVersionUID = 834626904238820805L;

	@PersistenceContext
	private transient EntityManager entityManager;

	@Inject
	private ValidatorProvider validatorProvider;

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/*
	 * Hinzufügen neuer Produktdaten
	 */
	public Produktdaten addProduktdaten(Produktdaten neueProduktdaten,
			Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Fuege Produktdaten={0} hinzu",
				neueProduktdaten);

		// Validierung
		checkViolations(getValidator(locale).validate(neueProduktdaten,
				Default.class));

		// Speichern
		entityManager.persist(neueProduktdaten);

		// Log
		LOGGER.log(FINER, "ENDE: Fuege Produktdaten={0} hinzu",
				neueProduktdaten);

		return neueProduktdaten;
	}

	@SuppressWarnings("unchecked")
	public List<Produktdaten> findProduktdatenKomplett() {

		// Log
		LOGGER.log(FINER, "BEGINN: Finde alle Produktdaten");

		final List<Produktdaten> result = entityManager.createNamedQuery(
				Produktdaten.PRODUKTDATEN_KOMPLETT).getResultList();

		// Log
		LOGGER.log(FINER, "ENDE: Finde alle Produktdaten");

		return result;
	}

	/**
	 * Suche nach Produktdaten anhand von ProduktdatenID
	 * 
	 * @param produktdatenID
	 *            ID des Produktes
	 * @param locale
	 * @return Produkt mit entsprechender ID
	 */
	public Produktdaten findProduktdatenByID(Integer produktdatenID,
			Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach Produktdaten anhand ID={0}",
				produktdatenID);

		// Validierung
		checkViolations(getValidator(locale).validateValue(Produktdaten.class,
				Produktdaten_.produktdatenID.getName(), produktdatenID,
				IdGroup.class));

		// Suche in DB
		final Produktdaten produktdaten = entityManager.find(
				Produktdaten.class, produktdatenID);

		// Log
		LOGGER.log(FINER, "ENDE: Suche nach Produktdaten. Ergebnis={0}",
				produktdaten);

		return produktdaten;
	}

	/*
	 * Suche nach Produktdaten anhand von Such-Filter
	 */
	public List<Produktdaten> findProduktdatenByFilter(SuchFilter filter,
			Locale locale) {

		/*
		 * HQL QUERY:
		 * =====================================================================
		 * SELECT pd FROM Produktdaten as pd JOIN pd.produkt p WHERE
		 * pd.anzahlVerfuegbar > :anzahl AND pd.preis BETWEEN :preis_unten AND
		 * :preis_oben AND LOWER(pd.farbe) LIKE LOWER(CONCAT('%',:farbe,'%'))
		 * AND pd.groesse = :groesse AND LOWER(p.hersteller) LIKE
		 * LOWER(CONCAT('%',:hersteller,'%')) AND LOWER(p.beschreibung) LIKE
		 * LOWER(CONCAT('%',:beschreibung,'%'))
		 * =====================================================================
		 */

		// Log
		LOGGER.log(FINER,
				"BEGINN: Suche nach Produktdaten anhand Suchfilter={0}", filter);

		final CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		// SELECT pd
		final CriteriaQuery<Produktdaten> query = builder
				.createQuery(Produktdaten.class);

		// FROM Produktdaten
		final Root<Produktdaten> pd = query.from(Produktdaten.class);

		// JOIN pd.produkt
		final Join<Produktdaten, Produkt> join = pd.join(Produktdaten_.produkt);

		// WHERE
		final List<Predicate> predicates = new LinkedList<Predicate>();

		// ANZAHL
		if (filter.getAnzahl() != null && filter.getAnzahl().intValue() != 0) {
			final Predicate predAnzahl = builder.gt(
					pd.get(Produktdaten_.anzahlVerfuegbar), filter.getAnzahl());
			predicates.add(predAnzahl);
		}

		// PREIS
		if (filter.getPreisOben() != null && filter.getPreisUnten() != null
				&& filter.getPreisOben().doubleValue() != 0.0) {
			final Predicate predPreis = builder.between(
					pd.get(Produktdaten_.preis), filter.getPreisUnten(),
					filter.getPreisOben());
			predicates.add(predPreis);
		}

		// FARBE
		if (filter.getFarbe() != null && !filter.getFarbe().isEmpty()) {
			final Predicate predFarbe = builder.like(
					builder.lower(pd.get(Produktdaten_.farbe)), "%"
							+ filter.getFarbe().toLowerCase() + "%");
			predicates.add(predFarbe);
		}

		// GROESSE
		if (filter.getGroesse() != null && !filter.getGroesse().isEmpty()) {
			final Predicate predGroesse = builder.equal(builder.lower(pd
					.get(Produktdaten_.groesse)), filter.getGroesse()
					.toLowerCase());
			predicates.add(predGroesse);
		}

		// HERSTELLER
		if (filter.getHersteller() != null && !filter.getHersteller().isEmpty()) {
			final Predicate predHersteller = builder.like(
					builder.lower(join.get(Produkt_.hersteller)), "%"
							+ filter.getHersteller().toLowerCase() + "%");
			predicates.add(predHersteller);
		}

		// BESCHREIBUNG
		if (filter.getBeschreibung() != null
				&& !filter.getBeschreibung().isEmpty()) {
			final Predicate predBeschreibung = builder.like(
					builder.lower(join.get(Produkt_.beschreibung)), "%"
							+ filter.getBeschreibung().toLowerCase() + "%");
			predicates.add(predBeschreibung);
		}

		final Predicate[] predArray = new Predicate[predicates.size()];
		query.where(builder.and(predicates.toArray(predArray)));

		// Suche in DB
		final List<Produktdaten> gefundeneProduktdaten = entityManager
				.createQuery(query).getResultList();

		// Log
		LOGGER.log(
				FINER,
				"ENDE: Suche nach Produktdaten anhand Suchfilter. Ergebnis: {0} Produktdaten",
				gefundeneProduktdaten.size());

		return gefundeneProduktdaten;
	}

	@SuppressWarnings("unchecked")
	public List<Produktdaten> findProduktdatenByProduktId(Integer produktId) {

		// Log
		LOGGER.log(FINER, "BEGINN: Suche nach Produktdaten by ProduktID={0}",
				produktId);

		final List<Produktdaten> results = entityManager
				.createNamedQuery(Produktdaten.PRODUKTDATEN_BY_PRODUKT_ID)
				.setParameter("id", produktId).getResultList();

		// Log
		LOGGER.log(FINER,
				"ENDE: Suche nach Produktdaten by ProduktID. Gefunden: {0}",
				results.size());

		return results;
	}

	/*
	 * Aktualisieren (Bearbeiten) von Produktdaten
	 */
	public ProduktdatenService updateProduktdaten(Produktdaten produktdaten,
			Locale locale) {

		// Log
		LOGGER.log(FINER, "BEGINN: Aktualisiere Produktdaten={0}", produktdaten);

		// Validierung
		checkViolations(getValidator(locale).validate(produktdaten,
				Default.class, IdGroup.class));

		// Update in DB
		entityManager.merge(produktdaten);

		// Log
		LOGGER.log(FINER, "ENDE: Aktualisiere Produktdaten={0}", produktdaten);

		return this;
	}

	/*
	 * Liefert den entsprechenden Validator
	 */
	private Validator getValidator(Locale l) {
		return validatorProvider.getValidator(l);
	}

	/*
	 * Überprüft, ob in dem übergebenen Set Vialations vorhanden sind und wirft
	 * ggf. eine Exception
	 */
	private void checkViolations(
			Set<ConstraintViolation<Produktdaten>> violations) {

		if (!violations.isEmpty()) {
			LOGGER.log(SEVERE, "{0} Fehler bei der Validierung",
					violations.size());
			final StringBuffer buffer = new StringBuffer();
			final Iterator<ConstraintViolation<Produktdaten>> it = violations
					.iterator();
			while (it.hasNext()) {
				buffer.append(it.next().getMessage());
				buffer.append('\n');
			}
			throw new ProduktdatenValidationException(buffer.toString());
		}
	}
}
