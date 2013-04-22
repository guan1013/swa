package de.shop.kundenverwaltung.service;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Adresse_;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
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
 * @author Yannick Gentner & Matthias Schnell
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
	private ValidatorProvider validatorProvider;

	@Inject
	private transient Logger LOGGER;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	public enum FetchType {
		NUR_ADRESSE, MIT_KUNDE
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Validator wird implementiert
	 */
	private Validator getValidator(Locale l) {
		return validatorProvider.getValidator(l);
	}

	private void checkViolations(Set<ConstraintViolation<Adresse>> violations) {

		if (!violations.isEmpty()) {
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

		/**
		 * Alle gefundenen Adressen speichern.
		 */
		List<Adresse> ad = em.createNamedQuery(Adresse.ALL_ADRESSEN)
				.getResultList();
		return ad;
	}

	@SuppressWarnings("unchecked")
	public List<Adresse> findAdressenByKundeId(Integer id) {

		/**
		 * Alle gefundenen Adressen speichern
		 */
		List<Adresse> ad = em.createNamedQuery(Adresse.ADRESSE_BY_KUNDEID)
				.setParameter("id", id).getResultList();

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

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_WOHNORT)
					.setParameter("ort", ort).getResultList();
			break;

		case MIT_KUNDE:

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_WOHNORT)
					.setParameter("ort", ort).getResultList();

			break;

		default:

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

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_STRASSE)
					.setParameter("strasse", strasse).getResultList();

			break;

		case MIT_KUNDE:

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_STRASSE)
					.setParameter("strasse", strasse).getResultList();
			break;

		default:

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

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_BY_PLZ)
					.setParameter("plz", plz).getResultList();

			break;

		case MIT_KUNDE:

			// Named Query aufrufen
			ad = em.createNamedQuery(Adresse.ADRESSE_MIT_KUNDE_BY_PLZ)
					.setParameter("plz", plz).getResultList();

			break;

		default:

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

		// In DB suchen
		Adresse ad = em.find(Adresse.class, adresseID);

		return ad;
	}

}
