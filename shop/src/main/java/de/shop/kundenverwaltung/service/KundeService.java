package de.shop.kundenverwaltung.service;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.InvalidEmailException;
import de.shop.util.exceptions.InvalidKundeIdException;
import de.shop.util.exceptions.InvalidNachnameException;
import de.shop.util.exceptions.KundeValidationException;

/**
 * Anwendungslogik fuer die Kunden Services
 * 
 * @author Matthias Schnell
 */
public class KundeService implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final long serialVersionUID = -3457208054417097021L;

	public enum FetchType {
		JUST_KUNDE, WITH_BESTELLUNGEN
	}

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private ValidatorProvider validatorProvider;

	@Inject
	private transient Logger LOGGER;

	@Inject
	@NeuerKunde
	private transient Event<Kunde> event;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS
	/**
	 * Lege einen Kunden an
	 * 
	 * @param pKD
	 * @return
	 */
	public Kunde addKunde(Kunde pKD, Locale pLocale) {

		if (pKD == null)
			return pKD;

		/**
		 * Prüfen ob Kundendaten korrekt sind
		 */
		validateKunde(pKD, pLocale, Default.class, PasswordGroup.class);

		/**
		 * Wenn Kunde mit dieser E-Mail Adresse noch nicht existiert, lege ihn
		 * an und schicke eine Bestätigungsnachricht an diesen.
		 */
		List<Kunde> kd = findKundeByMail(FetchType.JUST_KUNDE, pKD.getEmail(),
				pLocale);
		if (kd.isEmpty()) {
			em.persist(pKD);
			event.fire(pKD);
		}

		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		return pKD;
	}

	/**
	 * Finde alle Kunden
	 */
	@SuppressWarnings("unchecked")
	public List<Kunde> findAllKunden() {

		/**
		 * Alle gefunden Kunden speichern.
		 */
		List<Kunde> kd = em.createNamedQuery(Kunde.ALL_KUNDEN).getResultList();

		return kd;
	}

	/**
	 * Finde einen Kunde anhand seiner ID
	 * 
	 * @param pID
	 * @return gefundenen Kunde
	 */

	public Kunde findKundeById(Integer pID, Locale pLocale) {
		if (pID == null) {
			return null;
		}

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateKundeId(pID, pLocale);
		Kunde kd;

		/**
		 * Gefundenen Kunde speichern
		 */

		kd = em.find(Kunde.class, pID);

		return kd;
	}

	/**
	 * Finde einen Kunde anhand seiner E-Mail
	 * 
	 * @param pMail
	 * @return gefundenen Kunden als Liste
	 */
	@SuppressWarnings("unchecked")
	public List<Kunde> findKundeByMail(FetchType pFe, String pMail,
			Locale pLocale) {
		if (pMail == null) {
			return null;
		}

		/**
		 * Prüfen ob Email richtig eingegeben ist.
		 */

		validateKundeEmail(pMail, pLocale);

		/**
		 * Initalisiere Liste von Kunden kd
		 */
		List<Kunde> kd;

		switch (pFe) {

		case JUST_KUNDE:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */
			kd = em.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
					.setParameter("mail", pMail).getResultList();
			break;

		case WITH_BESTELLUNGEN:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */

			kd = em.createNamedQuery(Kunde.KUNDE_BY_EMAIL_JOIN_BESTELLUNG)
					.setParameter("mail", pMail).getResultList();
			break;

		default:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */
			kd = em.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
					.setParameter("mail", pMail).getResultList();
			break;

		}

		return kd;
	}

	/**
	 * Finde Kunden anhand der Nachnamen
	 * 
	 * @param pNachname
	 * @return gefundene Kundenliste
	 */
	@SuppressWarnings("unchecked")
	public List<Kunde> findKundeByNachname(FetchType pFe, String pName,
			Locale pLocale) {
		if (pName == null) {
			return null;
		}

		/**
		 * Prüfe ob Nachname richtig eingegeben ist
		 */
		validateKundeNachname(pName, pLocale);

		/**
		 * Initalisiere Liste von Kunden kd
		 */
		List<Kunde> kd;

		switch (pFe) {
		case JUST_KUNDE:
			/**
			 * Gefundene Kunden in eine Liste speichern
			 */
			kd = em.createNamedQuery(Kunde.KUNDE_BY_NACHNAME)
					.setParameter("name", pName).getResultList();
			break;

		case WITH_BESTELLUNGEN:
			/**
			 * Gefundene Kunden in eine Liste speichern
			 */

			kd = em.createNamedQuery(Kunde.KUNDE_BY_NACHNAME_JOIN_BESTELLUNG)
					.setParameter("name", pName).getResultList();
			break;

		default:
			/**
			 * Gefundene Kunden in eine Liste speichern
			 */
			kd = em.createNamedQuery(Kunde.KUNDE_BY_NACHNAME)
					.setParameter("name", pName).getResultList();
			break;

		}

		return kd;
	}

	/**
	 * Update einen Kunden
	 * 
	 * @param pKD
	 * @return
	 */
	public Kunde updateKunde(Kunde pKD, Locale pLocale) {
		if (pKD == null) {
			return pKD;
		}

		validateKunde(pKD, pLocale, Default.class, PasswordGroup.class);

		/**
		 * Prüfen ob zu ändernde Kunde existiert
		 */
		Kunde existingKunde = findKundeById(pKD.getKundeID(), pLocale);
		if (existingKunde == null) {
			return null;
		}

		/**
		 * Prüfen ob zu ändernde E-Mail Adresse schon vorhanden ist
		 */
		if (!pKD.getEmail().equals(existingKunde.getEmail())) {
			List<Kunde> kd = findKundeByMail(FetchType.JUST_KUNDE,
					pKD.getEmail(), pLocale);
			if (!kd.isEmpty()) {
				return null;
			}
		}

		em.merge(pKD);

		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		return pKD;
	}

	// /////////////////////////////////////////////////////////////////////
	// VALIDATES

	private void validateKundeId(Integer pKID, Locale pLocale) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Kunde>> violations = validator
				.validateValue(Kunde.class, "kundeID", pKID, IdGroup.class);
		if (!violations.isEmpty()) {
			throw new InvalidKundeIdException(pKID, violations);
		}

	}

	private void validateKundeNachname(String pName, Locale pLocale) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Kunde>> violations = validator
				.validateValue(Kunde.class, "nachname", pName, Default.class);
		if (!violations.isEmpty()) {
			StringBuffer temp = new StringBuffer();

			java.util.Iterator<ConstraintViolation<Kunde>> it = violations
					.iterator();

			while (it.hasNext()) {
				temp.append(it.next().getMessage());
				temp.append('\n');
			}
			throw new InvalidNachnameException(temp.toString());
		}
	}

	private void validateKundeEmail(String pMail, Locale pLocale) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Kunde>> violations = validator
				.validateValue(Kunde.class, "email", pMail, Default.class);

		if (!violations.isEmpty()) {
			throw new InvalidEmailException(pMail, violations);
		}

	}

	private void validateKunde(Kunde pKD, Locale pLocale, Class<?>... pGroups) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Kunde>> violations = validator.validate(
				pKD, pGroups);

		if (!violations.isEmpty()) {
			throw new KundeValidationException(pKD, violations);
		}

	}

}