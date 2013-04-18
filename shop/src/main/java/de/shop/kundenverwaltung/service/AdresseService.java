package de.shop.kundenverwaltung.service;

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

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Adresse_;
import de.shop.util.IdGroup;
import de.shop.util.ValidationService;
import de.shop.util.exceptions.AdresseValidationException;

/**
 * 
 * Die Klasse AdresseService implementiert die Use-Cases, die die Klasse Adresse
 * betreffen. Folgende Use-Cases sind implementiert: <br>
 * 
 * - Suche Adresse nach Ort<br>
 * - Suche Adresse nach Strasse<br>
 * - Suche Adresse nach PLZ<br>
 * - Suche Adresse nach ID<br>
 * - Adresse hinzufügen<br>
 * - Adresse aendern<br>
 * 
 * @author Yannick Gentner
 * 
 */

public class AdresseService implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	/**
	 * 
	 */
	private static final long serialVersionUID = 9185897502827894129L;

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private ValidationService validationService;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	public enum FetchType {
		NUR_ADRESSE, MIT_KUNDE
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Validator wird implementiert
	 */
	private Validator getValidator(Locale l) {
		return validationService.getValidator(l);
	}

	private void checkViolations(Set<ConstraintViolation<Adresse>> violations) {

		if (!violations.isEmpty()) {
			LOGGER.log(SEVERE, "{0} Fehler bei der Validierung",
					violations.size());
			StringBuffer buffer = new StringBuffer();
			Iterator<ConstraintViolation<Adresse>> it = violations.iterator();
			while (it.hasNext()) {
				buffer.append(it.next().getMessage());
				buffer.append('\n');
			}
			throw new AdresseValidationException(buffer.toString());
		}
	}

	/**
	 * Fuegt eine neue Adresse hinzu
	 * 
	 * @param adresse
	 * @return
	 */
	public Adresse addAdresse(Adresse neueAdresse, Locale locale) {

		// Log
		LOGGER.log(FINER, "Add neue Adresse={0}", neueAdresse);

		// Validierung Adresse
		checkViolations(getValidator(locale).validate(neueAdresse,
				Default.class));

		// Neues Adresse speichern
		em.persist(neueAdresse);

		return neueAdresse;
	}

	/**
	 * Aendert eine vorhandene Adresse
	 * 
	 * @param adresse
	 * @return
	 */
	public Adresse updateAdresse(Adresse adresse, Locale locale) {

		// Log
		LOGGER.log(FINER, "Update Adresse={0}", adresse);

		// Validierung Adresse
		checkViolations(getValidator(locale).validate(adresse));

		// Neue Adresse speichern
		em.merge(adresse);

		return adresse;
	}

	/**
	 * Findet alle vorhandenen Adressen
	 * 
	 * @param adresse
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Adresse> findAllAdressen() {
		LOGGER.log(FINER, "BEGINN: findAllAdressen");

		/**
		 * Alle gefundenen Adressen speichern.
		 */
		List<Adresse> ad = em.createNamedQuery(Adresse.ALL_ADRESSEN)
				.getResultList();

		LOGGER.log(FINER, "END: finAllAdressen");
		return ad;
	}

	@SuppressWarnings("unchecked")
	public List<Adresse> findAdressenByKundeId(Integer id) {

		LOGGER.log(FINER, "BEGINN: findAllAdressenByKundeId");

		/**
		 * Alle gefundenen Adressen speichern
		 */
		List<Adresse> ad = em.createNamedQuery(Adresse.ADRESSE_BY_KUNDEID)
				.setParameter("id", id).getResultList();

		LOGGER.log(FINER, "END: findAllAdressenByKundeId");
		return ad;
	}

	/**
	 * Finde eine Adresse anhand ihres Orts
	 * 
	 * @param ort
	 * @return gefundene Adressen
	 */
	@SuppressWarnings("unchecked")
	public List<Adresse> findAdresseByOrt(FetchType fetch, String ort,
			Locale locale) {

		// Validierung Ort
		checkViolations(getValidator(locale).validateValue(Adresse.class,
				Adresse_.ort.getName(), ort));

		List<Adresse> ad;
		switch (fetch) {
		case NUR_ADRESSE:

			// Log
			LOGGER.log(FINER, "NUR_ADRESSE: findAdresseByOrt mit ort= {0}", ort);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_WOHNORT)
					.setParameter("ort", ort).getResultList();
			break;

		case MIT_KUNDE:

			// Log
			LOGGER.log(FINER,
					"MIT_KUNDE: findeKundeMitAdresseNachWohnort mit ort= {0}",
					ort);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_WOHNORT)
					.setParameter("ort", ort).getResultList();

			break;

		default:

			// Log
			LOGGER.log(FINER, "DEFAULT: findAdresseByOrt mit ort= {0}", ort);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_WOHNORT)
					.setParameter("ort", ort).getResultList();
			break;

		}
		return ad;
	}

	/**
	 * Finde eine Adresse anhand ihrer Strasse
	 * 
	 * @param strasse
	 * @return gefundene Adressen
	 */
	@SuppressWarnings("unchecked")
	public List<Adresse> findAdresseByStrasse(FetchType fetch, String strasse,
			Locale locale) {

		// Validierung Strasse
		checkViolations(getValidator(locale).validateValue(Adresse.class,
				Adresse_.strasse.getName(), strasse));

		List<Adresse> ad;
		switch (fetch) {
		case NUR_ADRESSE:

			// Log
			LOGGER.log(FINER,
					"NUR_ADRESSE: findAdresseByStrasse mit strasse= {0}",
					strasse);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_STRASSE)
					.setParameter("strasse", strasse).getResultList();

			break;

		case MIT_KUNDE:

			// Log
			LOGGER.log(
					FINER,
					"MIT_KUNDE: findeKundeMitAdresseNachStrasse mit strasse= {0}",
					strasse);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_STRASSE)
					.setParameter("strasse", strasse).getResultList();
			break;

		default:
			// Log
			LOGGER.log(
					FINER,
					"DEFAULT: findeKundeMitAdresseNachStrasse mit strasse= {0}",
					strasse);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_STRASSE)
					.setParameter("strasse", strasse).getResultList();

		}
		return ad;
	}

	/**
	 * Finde eine Adresse anhand ihrer PLZ
	 * 
	 * @param plz
	 * @return gefundene Adressen
	 */
	@SuppressWarnings("unchecked")
	public List<Adresse> findAdresseByPLZ(FetchType fetch, Integer plz,
			Locale locale) {

		// Validierung PLZ
		checkViolations(getValidator(locale).validateValue(Adresse.class,
				Adresse_.plz.getName(), plz));

		List<Adresse> ad;
		switch (fetch) {
		case NUR_ADRESSE:

			// Log
			LOGGER.log(FINER, "NUR_ADRESSE: findAdresseByPLZ mit plz = {0}",
					plz);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_PLZ)
					.setParameter("plz", plz).getResultList();

			break;

		case MIT_KUNDE:

			// Log
			LOGGER.log(FINER,
					"MIT_KUNDE: findeKundeMitAdresseNachPLZ mit plz = {0}", plz);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_PLZ)
					.setParameter("plz", plz).getResultList();

			break;

		default:

			// Log
			LOGGER.log(FINER,
					"DEFAULT: findeKundeMitAdresseNachPLZ mit plz = {0}", plz);

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_PLZ)
					.setParameter("plz", plz).getResultList();

			break;
		}
		return ad;
	}

	/**
	 * Finde eine Adresse anhand ihrer AdressenID
	 * 
	 * @param adresseID
	 * @return gefundene Adresse
	 */

	public Adresse findAdresseByAdresseID(Integer adresseID, Locale locale) {

		// / Validierung ID
		checkViolations(getValidator(locale).validateValue(Adresse.class,
				Adresse_.adresseID.getName(), adresseID, IdGroup.class));

		// Log
		LOGGER.log(FINER, "BEGINN: findAdresseByAdresseID mit adresseID = {0}",
				adresseID);

		// In DB suchen
		Adresse ad = em.find(Adresse.class, adresseID);

		// Log
		LOGGER.log(FINER, "ENDE: findAdresseByAdresseID mit adresseID = {0}",
				ad);

		return ad;
	}

}
