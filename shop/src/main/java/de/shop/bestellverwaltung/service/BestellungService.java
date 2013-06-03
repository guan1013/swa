package de.shop.bestellverwaltung.service;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
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

import de.shop.bestellverwaltung.domain.Bestellposten;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.IdGroup;
import de.shop.util.Transactional;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.BestellungValidationException;
import de.shop.util.exceptions.ConcurrentDeletedException;
import de.shop.util.exceptions.InvalidBestellungIdException;
import de.shop.util.exceptions.InvalidGesamtpreisException;

/**
 * Anwendungslogik fuer die Bestellung Services
 * 
 * @author Matthias Schnell
 */
public class BestellungService implements Serializable {

	private static final long serialVersionUID = 6656323828540928985L;

	public enum FetchType {
		JUST_BESTELLUNG, WITH_BESTELLPOSTEN, WITH_KUNDE
	}

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private ValidatorProvider validatorProvider;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());
	@Inject
	private BestellpostenService bps;
	
	
	// /////////////////////////////////////////////////////////////////////
	// METHODS
	/**
	 * Lege eine Bestellung an
	 * 
	 * @param pBD
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	public Bestellung addBestellung(Bestellung pBD, Locale pLocale) throws Exception {

		if (pBD == null)
			return pBD;
		LOGGER.log(FINER, "SERVICE BEGINN: createBestellung with pKD= {0}", pBD);

		/**
		 * Prüfen ob Bestelldaten korrekt sind
		 */
		validateBestellung(pBD, pLocale, Default.class);
		LOGGER.log(FINEST, "SERVICE: Bestellung {0} validierung erfolgreich.",
				pBD.getBestellungID());

		/**
		 * Die Bestellung wird an die Datenbank übergeben
		 */
		em.persist(pBD);
		
		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		LOGGER.log(FINEST, "SERVICE: Bestellung {0} Persist erfolgreich.",
				pBD.getBestellungID());

		LOGGER.log(FINER, "SERVICE END: createBestellung with pBD= {0}", pBD);
		return pBD;
	}

	/**
	 * Finde alle Bestellungen
	 */
	@SuppressWarnings("unchecked")
	public List<Bestellung> findAllBestellungen() {
		LOGGER.log(FINER, "SERVICE BEGINN: findAllBestellungen");

		/**
		 * Gefundene Bestellung speichern
		 */

		final List<Bestellung> be = em.createNamedQuery(Bestellung.ALL_BESTELLUNGEN)
				.getResultList();

		LOGGER.log(FINER, "SERVICE END: findAllBestellungen");
		return be;
	}
	@Transactional
	public void addBestellposten(Bestellung b) {
		for (Bestellposten posten : b.getBestellposten()) {
			posten.setBestellung(b);
			bps.updateAfterCreateBestellung(posten.getBestellpostenID(), b.getBestellungID());
		}
	}

	/**
	 * Finde eine Bestellung anhand seiner ID
	 * 
	 * @param pID
	 * @return gefundene Bestellung
	 */

	public Bestellung findBestellungById(Integer pID, Locale pLocale) {
		if (pID == null) {
			return null;
		}
		LOGGER.log(FINER, "SERVICE BEGINN: findBestellungById with pID= {0}",
				pID);

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateBestellungId(pID, pLocale);
		LOGGER.log(FINEST, "SERVICE: ID {0} validierung erfolgreich.", pID);
		Bestellung be;

		/**
		 * Gefundene Bestellung speichern
		 */

		be = em.find(Bestellung.class, pID);

		LOGGER.log(FINER, "SERVICE END: findBestellungById with pID= {0}", pID);
		return be;
	}

	/**
	 * Find eine Bestellung anhand einer Spreisspanne
	 * 
	 * @param pMin
	 * @param pMax
	 * @return gefundene Bestellungen als Liste
	 */
	@SuppressWarnings("unchecked")
	public List<Bestellung> findBestellungByPreisspanne(FetchType pFe,
			Double pMin, Double pMax, Locale pLocale) {
		if (pMin == null || pMax == null) {
			return null;
		}

		LOGGER.log(FINER,
				"SERVICE BEGINN: findBestellungBySpreisspanne with pMin ={0}",
				pMin);
		LOGGER.log(FINER, "and pMax= {0}", pMax);

		/**
		 * Prüfung ob Preise Korrekt eingegeben sind
		 */

		validateBestellungGesamtpreis(pMin, pLocale);
		LOGGER.log(FINEST, "SERVICE: pMin {0} validierung erfolgreich.", pMin);
		validateBestellungGesamtpreis(pMax, pLocale);
		LOGGER.log(FINEST, "SERVICE: pMax {0} validierung erfolgreich.", pMax);

		/**
		 * Initalisiere Liste von Bestellung be
		 */
		List<Bestellung> be;

		final String paraMin = "min";
		final String paraMax = "max";
		switch (pFe) {

		case JUST_BESTELLUNG:
			/**
			 * Gefundene Bestellungen in eine Liste speichern
			 */

			be = em.createNamedQuery(Bestellung.BESTELLUNG_BY_PREISSPANNE)
					.setParameter(paraMin, pMin).setParameter(paraMax, pMax)
					.getResultList();
			LOGGER.log(FINEST, "SERVICE: {0} Bestellungen gefunden.", be.size());
			break;

		case WITH_KUNDE:
			/**
			 * Gefundene Bestellungen in eine Liste speichern
			 */

			be = em.createNamedQuery(
					Bestellung.BESTELLUNG_BY_PREISSPANNE_WITH_KUNDE)
					.setParameter(paraMin, pMin).setParameter(paraMax, pMax)
					.getResultList();
			LOGGER.log(FINEST, "SERVICE: {0} Bestellungen gefunden.", be.size());
			break;

		case WITH_BESTELLPOSTEN:
			/**
			 * Gefundene Bestellungen in eine Liste speichern
			 */

			be = em.createNamedQuery(
					Bestellung.BESTELLUNG_BY_PREISSPANNE_WITH_BESTELLPOSTEN)
					.setParameter(paraMin, pMin).setParameter(paraMax, pMax)
					.getResultList();
			LOGGER.log(FINEST, "SERVICE: {0} Bestellungen gefunden.", be.size());
			break;

		default:
			/**
			 * Gefundene Bestellungen in eine Liste speichern
			 */
			be = em.createNamedQuery(Bestellung.BESTELLUNG_BY_PREISSPANNE)
					.setParameter(paraMin, pMin).setParameter(paraMax, pMax)
					.getResultList();
			LOGGER.log(FINEST, "SERVICE: {0} Bestellungen gefunden.", be.size());
			break;

		}

		LOGGER.log(FINER,
				"SERVICE END: findBestellungBySpreisspanne with pMin= {0}",
				pMin);
		LOGGER.log(FINER, "and pMax = {0}", pMax);

		return be;
	}

	/**
	 * Finde alle Bestellungen eines Bestimmten Kunden
	 */
	@SuppressWarnings("unchecked")
	public List<Bestellung> findBestellungenByKundeId(Integer pKID) {
		LOGGER.log(FINER,
				"SERVICE BEGINN: findBestellungenByKundeId with pKID={0}", pKID);

		if (pKID == null) {
			return null;
		}

		/**
		 * Gefundene Bestellung speichern
		 */
		final List<Bestellung> be = em
				.createNamedQuery(Bestellung.BESTELLUNG_BY_KUNDE_ID)
				.setParameter("kid", pKID).getResultList();
		LOGGER.log(FINEST, "SERVICE: {0} Bestellungen gefunden.", be.size());

		LOGGER.log(FINER,
				"SERVICE END: findBestellungenByKundeId with pKID = {0}", pKID);
		return be;
	}
	
	public void deleteBestellungById(int pKID, Locale pLocale) {
		Bestellung kd;
		try {
			kd = findBestellungById(pKID, pLocale);
		} catch (InvalidBestellungIdException e) {
			return;
		}
		if (kd == null) {
			// Der Kunde existiert nicht oder ist bereits geloescht
			return;
		}
		
		em.remove(kd);
	}

	/**
	 * Update eine Bestellung
	 * 
	 * @param pBD
	 * @return
	 */
	public Bestellung updateBestellung(Bestellung pBD, Locale pLocale) {
		if (pBD == null) {
			return pBD;
		}

		LOGGER.log(FINER, "SERVICE BEGINN: updateBestellung with pBD= {0}", pBD);

		validateBestellung(pBD, pLocale, Default.class);

		LOGGER.log(FINEST, "SERVICE: Bestellung {0} validierung erfolgreich.", pBD);
				em.detach(pBD);

		// Prüfen ob übergebener Kunde konkurrierend gelöscht wurde
		final Bestellung existingBestellung = findBestellungById(pBD.getBestellungID(), pLocale);
		if (existingBestellung == null) {
			throw new ConcurrentDeletedException(pBD.getBestellungID());
		}
		em.detach(existingBestellung);
		
		/**
		 * Prüfen ob zu ändernde Bestellung existiert
		 */
		if (findBestellungById(pBD.getBestellungID(), pLocale) == null) {

			LOGGER.log(FINEST, "SERVICE: Bestellung {0} wurde nicht gefunden",
					pBD.getBestellungID());

			return null;
		}

		/**
		 * Update auf übergebene Bestellung
		 */
		pBD = em.merge(pBD);

		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		LOGGER.log(FINEST, "SERVICE: Bestellung {0} Merge erfolgreich.",
				pBD.getBestellungID());

		LOGGER.log(FINER, "SERVICE END: updateBestellung with pBD= {0}", pBD);
		return pBD;
	}

	// /////////////////////////////////////////////////////////////////////
	// VALIDATES

	private void validateBestellungId(Integer pBID, Locale pLocale) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Bestellung>> violations = validator
				.validateValue(Bestellung.class, "bestellungID", pBID,
						IdGroup.class);
		if (!violations.isEmpty()) {
			throw new InvalidBestellungIdException(pBID, violations);
		}

	}

	private void validateBestellungGesamtpreis(Double pPreis, Locale pLocale) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Bestellung>> violations = validator
				.validateValue(Bestellung.class, "gesamtpreis", pPreis,
						Default.class);

		if (!violations.isEmpty()) {
			throw new InvalidGesamtpreisException(pPreis, violations);
		}

	}

	private void validateBestellung(Bestellung pBD, Locale pLocale,
			Class<?>... pGroups) {
		final Validator validator = validatorProvider.getValidator(pLocale);
		final Set<ConstraintViolation<Bestellung>> violations = validator
				.validate(pBD, pGroups);

		if (!violations.isEmpty()) {
			throw new BestellungValidationException(pBD, violations);
		}

	}

}
