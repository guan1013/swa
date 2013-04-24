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

import de.shop.auth.service.jboss.AuthService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.FileHelper.MimeType;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.ConcurrentDeletedException;
import de.shop.util.exceptions.InvalidEmailException;
import de.shop.util.exceptions.InvalidKundeIdException;
import de.shop.util.exceptions.InvalidNachnameException;
import de.shop.util.exceptions.KundeValidationException;
import de.shop.util.exceptions.EmailExistsException;
import de.shop.util.exceptions.KundeDeleteBestellungException;
import de.shop.util.exceptions.NoMimeTypeException;

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

	// INJECTS
	@Inject
	private ValidatorProvider validatorProvider;

	@Inject
	private transient Logger LOGGER;

	@Inject
	private AuthService authService;	
	
	@Inject
	private FileHelper fileHelper;

	@Inject
	@NeuerKunde
	private transient Event<Kunde> event;

	// LOGGER

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
		 * Pr�fen ob Kundendaten korrekt sind
		 */
		validateKunde(pKD, pLocale, Default.class, PasswordGroup.class);

		/**
		 * Wenn Kunde mit dieser E-Mail Adresse noch nicht existiert, lege ihn
		 * an und schicke eine Best�tigungsnachricht an diesen.
		 */
		List<Kunde> kd = findKundeByMail(FetchType.JUST_KUNDE, pKD.getEmail(),
				pLocale);
		if (kd.isEmpty()) {

			salting(pKD);
			em.persist(pKD);
			event.fire(pKD);
		}

		else {

			throw new EmailExistsException(pKD.getEmail());
		}

		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		return pKD;
	}
	
	/**
	 * Ohne MIME Type fuer Upload bei RESTful WS
	 */
	public void setKundePic(Integer pKID, byte[] pBs, Locale pLocale) {
		final Kunde kd = findKundeById(pKID, pLocale);
		if (kd == null) {
			return;
		}
		final MimeType mimeType = fileHelper.getMimeType(pBs);
		setKundePic(kd, pBs, mimeType);
	}
	
	/**
	 * Mit MIME-Type fuer Upload bei Webseiten
	 */
	public void setKundePic(Kunde pKD, byte[] pBs, String pMTStr) {
		final MimeType mimeType = MimeType.get(pMTStr);
		setKundePic(pKD, pBs, mimeType);
	}
	
	
	private void setKundePic(Kunde pKD, byte[] pBs, MimeType pMT) {
		if (pMT == null) {
			throw new NoMimeTypeException();
		}
		
		final String filename = fileHelper.getFilename(pKD.getClass(), pKD.getKundeID(), pMT);
		
		// Gibt es noch kein (Multimedia-) File
		File pic = pKD.getPic();
		if (pic == null) {
			pic = new File(pBs, filename, pMT);
			pKD.setPic(pic);
			em.persist(pic);
		}
		else {
			pic.set(pBs, filename, pMT);
			em.merge(pic);
		}
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
		 * Pr�fung ob ID Korrekt eingegeben ist
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
		 * Pr�fen ob Email richtig eingegeben ist.
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
		 * Pr�fe ob Nachname richtig eingegeben ist
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
	public Kunde updateKunde(Kunde pKD, Locale pLocale, boolean pDifPass) {
		if (pKD == null) {
			return pKD;
		}

		/**
		 * Pr�fen ob �bergebenes Kundenobjekt korrekt ist
		 */
		validateKunde(pKD, pLocale, Default.class,IdGroup.class, PasswordGroup.class);

		/**
		 * Kunde vom Entitiy Manager trennen
		 */
		em.detach(pKD);
		
		/**
		 * Pr�fen ob �bergebener Kunde konkurrierend gel�scht wurde
		 */
		Kunde existingKunde = findKundeById(pKD.getKundeID(), pLocale);
		if (existingKunde == null) {
			throw new ConcurrentDeletedException(pKD.getKundeID());
		}
		em.detach(existingKunde);

		/**
		 * Pr�fen ob zu �ndernde E-Mail Adresse schon vorhanden ist
		 */
		if (!pKD.getEmail().equals(existingKunde.getEmail())) {
			List<Kunde> kd = findKundeByMail(FetchType.JUST_KUNDE,
					pKD.getEmail(), pLocale);
			if (!kd.isEmpty()) {
				throw new EmailExistsException(pKD.getEmail());
			}
		}
		
		/**
		 * Pr�fen ob Passwort ge�ndert wurde
		 */
		if(pDifPass) { 
			salting(pKD);
		}
		
		pKD =em.merge(pKD);
		
		pKD.setPasswordWdh(pKD.getPassword());

		/**
		 * Datenbank synchronisieren
		 */
		em.flush();

		return pKD;
	}
	
	/**
	 * Kunde l�schen
	 */
	public void deleteKundeById(int pKID,Locale pLocale) {
		Kunde kd;
		try {
			kd = findKundeById(pKID, pLocale);
		}
		catch (InvalidKundeIdException e) {
			return;
		}
		if (kd == null) {
			// Der Kunde existiert nicht oder ist bereits geloescht
			return;
		}

		final boolean hasBestellungen = hasBestellungen(kd);
		if (hasBestellungen) {
			throw new KundeDeleteBestellungException(kd);
		}

		// Kundendaten loeschen
		em.remove(kd);
	}

	// /////////////////////////////////////////////////////////////////////
	// OTHERS

	private void salting(Kunde pKD) {
		LOGGER.debugf("salting BEGINN: %s", pKD);

		final String unverschluesselt = pKD.getPassword();
		final String verschluesselt = authService
				.verschluesseln(unverschluesselt);
		pKD.setPassword(verschluesselt);
		pKD.setPasswordWdh(verschluesselt);

		LOGGER.debugf("salting ENDE: %s", verschluesselt);
	}
	
	/**
	 */
	private boolean hasBestellungen(Kunde pKD) {
		LOGGER.debugf("hasBestellungen BEGINN: %s", pKD);
		
		boolean result = false;
		
		// Gibt es den Kunden und hat er mehr als eine Bestellung?
		// Bestellungen nachladen wegen Hibernate-Caching
		if (pKD != null && pKD.getBestellungen() != null && !pKD.getBestellungen().isEmpty()) {
			result = true;
		}
		
		LOGGER.debugf("hasBestellungen ENDE: %s", result);
		return result;
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