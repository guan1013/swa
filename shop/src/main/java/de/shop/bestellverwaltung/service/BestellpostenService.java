package de.shop.bestellverwaltung.service;

import static java.util.logging.Level.FINER;

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
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.BestellpostenValidationException;
import de.shop.util.exceptions.InvalidBestellpostenIdException;
import de.shop.util.exceptions.InvalidBestellungIdException;

public class BestellpostenService implements Serializable {

	/**
	 * Die Klasse BestellpostenService implementiert die Use-Cases, die die Klasse 
	 * Bestellposten betreffen. Folgende Use-Cases sind implementiert:<br>
	 * 
	 * - Hinzufügen von Bestellposten<br>
	 * - Suche nach allen vorhandenen Bestellposten<br>
	 * - Suche eines Bestellpostens nach ID<br>
	 * - Suche der Bestellposten nach ID der Bestellung<br>
	 * - Suche von Bestellposten nach Anzahl<br>
	 * 
	 * @author Dennis Brull
	 * @see Bestellposten
	 */
	private static final long serialVersionUID = 5223927625138619476L;



	public enum FetchType {

		JUST_BESTELLPOSTEN,
		WITH_BESTELLUNG,
	}

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	// /////////////////////////////////////////////////////////////////////

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private ValidatorProvider validatorProvider;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	// /////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////
	
	/**
	 * Lege einen Bestellposten an
	 * 
	 * @param Bestellposten
	 * @return Bestellposten
	 */	
	
	public Bestellposten addBestellposten(Bestellposten nBP, Locale locale) {

		if (nBP == null) {
			return nBP; 
		}
		LOGGER.log(FINER, "BEGIN: Füge ein Bestellposten zu " + nBP);

		/**
		 * Prüfen ob Daten korrekt
		 */
		validateBestellposten(nBP, locale, Default.class);
		/**
		 * Wenn Bestellposten mit dieser Id noch nicht existiert, lege ihn
		 * an.
		 */
		final Bestellposten bp = findBestellpostenByIdObjekt(nBP.getBestellpostenID(), locale);
		if (bp == null) {
			em.persist(nBP);
		}
		
		/**
		 * Datenbank synchronisieren
		 */
		em.flush();
		
		LOGGER.log(FINER, "ENDE: Füge ein Bestellposten zu " + nBP);
		return nBP;
	}
	
	/**
	 * Finde alle Bestellposten
	 */
	@SuppressWarnings("unchecked")
	public List<Bestellposten> findAllBestellposten() {
		
		// Log
		LOGGER.log(FINER, "BEGINN: Suche alle Bestellposten");
		
		final List<Bestellposten> bp = em.createNamedQuery(
				Bestellposten.ALL_BESTELLPOSTEN).getResultList();
		
		// Log
		LOGGER.log(FINER, "ENDE: Suche alle Bestellposten");
		
		return bp;
	}
	
	/**
	 * Finde einen Bestellposten anhand der ID
	 * @param justBestellposten 
	 * @param iD
	 * @return 1 Objekt Bestellposten
	 */

	public Bestellposten findBestellpostenByIdObjekt(Integer iD, Locale locale) {
		if (iD == null) {
			return null;
		}
		LOGGER.log(FINER, "BEGINN: Suche Bestellposten als Objekt nach ID " + iD);

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateBestellpostenId(iD, locale);
		Bestellposten bp;

		/**
		 * Gefundenen Bestellposten speichern
		 */

		bp = em.find(Bestellposten.class, iD);

		LOGGER.log(FINER, "ENDE: Suche Bestellposten  als Objekt nach ID " + iD);
		return bp;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Finde einen Bestellposten anhand der ID
	 * @param justBestellposten 
	 * @param iD
	 * @return Liste von Bestellposten
	 */
	public List<Bestellposten> findBestellpostenById(FetchType fetchType, Integer iD, Locale locale) {
		if (iD == null) {
			return null;
		}
		LOGGER.log(FINER, "BEGINN: Suche Bestellposten als Liste nach ID " + iD);

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateBestellpostenId(iD, locale);
		List<Bestellposten> bp;

		/**
		 * Gefundenen Bestellposten speichern
		 */

		switch (fetchType) {


		case JUST_BESTELLPOSTEN:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.NUR_BESTELLPOSTEN_NACH_ID).setParameter(
					"id", iD).getResultList();
			break;

		case WITH_BESTELLUNG:
			/**
			 * Gefundenen Bestellposten speichern
			 */

			bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_MIT_BESTELLUNG).setParameter(
					"bestellposten_id", iD).getResultList();
			
			break;

		default:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.NUR_BESTELLPOSTEN_NACH_ID).setParameter(
					"id", iD).getResultList();
			break;
		}
		LOGGER.log(FINER, "END: Suche Bestellposten als Liste nach ID " + iD);
		return bp;
	}

	/**
	 * Finde einen Bestellposten anhand der Bestellung ID
	 * @param justBestellposten 
	 * @param bFK
	 * @return Bestellposten
	 */

	@SuppressWarnings("unchecked")
	public List<Bestellposten> findBestellpostenByBestellungId(FetchType fetchType, Integer bFK, Locale locale) {
		if (bFK == null) {
			return null;
		}
		LOGGER.log(FINER, "BEGINN: Suche Bestellposten nach Bestellung ID " + bFK);

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateBestellungId(bFK, locale);
		List<Bestellposten> bp;

		/**
		 * Gefundenen Bestellposten speichern
		 */

		switch (fetchType) {


		case JUST_BESTELLPOSTEN:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_NACH_BESTELLUNG).setParameter(
					"bestellungFk", bFK).getResultList();
			break;
		
		case WITH_BESTELLUNG:
		
		bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_MIT_BESTELLUNG).setParameter(
				"bestellungFk", bFK).getResultList();
		break;
		
		default:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_NACH_BESTELLUNG).setParameter(
					"bestellungFk", bFK).getResultList();
			break;
		}
		LOGGER.log(FINER, "ENDE: Suche Bestellposten nach Bestellung ID " + bFK);
		return bp;
	}
	
	/**
	 * Suche Bestellposten nach Anzahl
	 * 
	 * @param Anzahl
	 * @return List Bestellposten
	 */
	@SuppressWarnings("unchecked")
	public List<Bestellposten> findBestellpostenByAnzahl(FetchType fetchType, int anz, Locale locale) {
		if (anz <= 0) {
			return null;
		}
		LOGGER.log(FINER, "BEGINN: Suche Bestellposten nach Anzahl " + anz);

		List<Bestellposten> bp;

		/**
		 * Gefundenen Bestellposten speichern
		 */

		switch (fetchType) {

		case JUST_BESTELLPOSTEN:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_NACH_ANZAHL).setParameter(
					"anzahl", anz).getResultList();
			break;


		default:
			/**
			 * Gefundenen Bestellposten speichern
			 */
			bp = em.createNamedQuery(Bestellposten.BESTELLPOSTEN_NACH_ANZAHL).setParameter(
					"anzahl", anz).getResultList();
			break;
		}
		LOGGER.log(FINER, "ENDE: Suche Bestellposten nach Anzahl " + anz);
		return bp;
	}	
	
	
	public void updateAfterCreateBestellung(int bpId, int bId) {
		em.createNamedQuery(Bestellposten.BESTELLPOSTEN_UPDATE_FK).setParameter("bestellungfk", bpId).setParameter
		("id", bId).executeUpdate();
	}
	
	
	
	/**
	 * Update einen Bestellposten
	 * 
	 * @param Bestellposten
	 * @return Bestellposten
	 */
	public Bestellposten updateBestellposten(Bestellposten bP, Locale locale) {
		if (bP == null) {
			return bP;
		}

		LOGGER.log(FINER, "BEGINN: Update Bestellpoosten " + bP);

		 validateBestellposten(bP, locale, IdGroup.class, Default.class);
		//Validator validator = validationService.getValidator(locale);
		//checkViolations(validator.validate(bP, IdGroup.class, Default.class));

		/**
		 * Prüfen ob zu ändernde Bestellposten existiert
		 */
		if (findBestellpostenByIdObjekt(bP.getBestellpostenID(), locale) == null) {
			return null;
		}

		/**
		 * Update auf übergebenen Bestellposten
		 */
		em.merge(bP);

		LOGGER.log(FINER, "END: Update Bestellpoosten " + bP);
		return bP;
	}
	/**
	 * Löschen eines Bestellpostens
	 */
	public void deleteBestellpostenById(Integer iD, Locale locale) {
		Bestellposten bp;
		
		//nach BP suchen
			bp = findBestellpostenByIdObjekt(iD, locale);
		
		if (bp == null) {
			return;
		}
		// Löschen
		em.remove(bp);
		
	}
	
	// /////////////////////////////////////////////////////////////////////
	// VALIDATE
	// /////////////////////////////////////////////////////////////////////

	private void validateBestellpostenId(Integer bpid, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Bestellposten>> violations = validator.validateValue(
				Bestellposten.class, "bestellpostenID", bpid, IdGroup.class);
		if (!violations.isEmpty())
			throw new InvalidBestellpostenIdException(bpid, violations);

	}

	private void validateBestellposten(Bestellposten iD, Locale locale, Class<?>... groups) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Bestellposten>> violations = validator.validate(
				iD, groups);

		if (!violations.isEmpty()) {
			throw new BestellpostenValidationException(iD, violations);
		}

	}

	private void validateBestellungId(Integer iD, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Bestellung>> violations = validator
				.validateValue(Bestellung.class, "bestellungID", iD, IdGroup.class);
		if (!violations.isEmpty()) {
			throw new InvalidBestellungIdException(iD, violations);
		}

	}
}
