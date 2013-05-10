package de.shop.kundenverwaltung.service;

import java.io.Serializable;
import java.util.Iterator;
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
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Adresse_;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.FileHelper.MimeType;
import de.shop.util.IdGroup;
import de.shop.util.ValidatorProvider;
import de.shop.util.exceptions.AdresseValidationException;
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
	private transient Logger logger;

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
		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		logger.debugf("CDI-faehiges Bean %s wird geloescht", this);
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

		// Prüfen ob Kundendaten korrekt sind

		validateKunde(pKD, pLocale, Default.class, PasswordGroup.class);

		// Wenn Kunde mit dieser E-Mail Adresse noch nicht existiert, lege ihn
		// an und schicke eine Bestätigungsnachricht an diesen.

		final Kunde kd = findKundeByMail(FetchType.JUST_KUNDE, pKD.getEmail(),
				pLocale);
		if (kd == null) {

			salting(pKD);
			em.persist(pKD);
			event.fire(pKD);
		}

		else {

			throw new EmailExistsException(pKD.getEmail());
		}

		// Datenbank synchronisieren

		em.flush();

		return pKD;
	}

	/**
	 * Fuegt eine neue Adresse hinzu
	 * 
	 * @param adresse
	 * @return
	 */
	public Adresse addAdresse(Adresse pAD, Locale pLocale) {

		// Validierung Adresse
		validateAdresse(validatorProvider.getValidator(pLocale).validate(pAD,
				Default.class));

		// Neue Adresse speichern
		em.persist(pAD);

		return pAD;
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

		final String filename = fileHelper.getFilename(pKD.getClass(),
				pKD.getKundeID(), pMT);

		// Gibt es noch kein (Multimedia-) File
		File pic = pKD.getPic();
		if (pic == null) {
			pic = new File(pBs, filename, pMT);
			pKD.setPic(pic);
			em.persist(pic);
		} else {
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
		final List<Kunde> kd = em.createNamedQuery(Kunde.ALL_KUNDEN)
				.getResultList();

		return kd;
	}

	/**
	 * Finde einen Kunde anhand seiner ID
	 * 
	 * @param pID
	 * @return gefundenen Kunde
	 */

	public Kunde findKundeById(Integer pID, Locale pLocale) {

		/**
		 * Prüfung ob ID Korrekt eingegeben ist
		 */

		validateKundeId(pID, pLocale);
		Kunde kd = null;

		/**
		 * Gefundenen Kunde speichern
		 */

		kd = em.find(Kunde.class, pID);

		return kd;
	}

	/**
	 * Finde eine Adresse anhand ihrer AdressenID
	 * 
	 * @param adresseID
	 * @return gefundene Adresse
	 */

	public Adresse findAdresseById(Integer pAID, Locale pLocale) {

		// / Validierung ID
		validateAdresse(validatorProvider.getValidator(pLocale).validateValue(
				Adresse.class, Adresse_.adresseID.getName(), pAID,
				IdGroup.class));

		// In DB suchen
		final Adresse ad = em.find(Adresse.class, pAID);

		return ad;
	}

	/**
	 * Finde einen Kunde anhand seiner E-Mail
	 * 
	 * @param pMail
	 * @return gefundenen Kunden als Liste
	 */
	public Kunde findKundeByMail(FetchType pFe, String pMail, Locale pLocale) {
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
		Kunde kd = null;

		switch (pFe) {

		case JUST_KUNDE:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */
			kd = (Kunde) em.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
					.setParameter("mail", pMail).getSingleResult();
			break;

		case WITH_BESTELLUNGEN:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */

			kd = (Kunde) em
					.createNamedQuery(Kunde.KUNDE_BY_EMAIL_JOIN_BESTELLUNG)
					.setParameter("mail", pMail).getSingleResult();
			break;

		default:
			/**
			 * Gefundenen Kunde in eine Liste speichern
			 */
			kd = (Kunde) em.createNamedQuery(Kunde.KUNDE_BY_EMAIL)
					.setParameter("mail", pMail).getSingleResult();
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

	@SuppressWarnings("unchecked")
	public List<Adresse> findAdressenByKundeId(Integer id) {

		/**
		 * Alle gefundenen Adressen speichern
		 */
		final List<Adresse> ad = em
				.createNamedQuery(Adresse.ADRESSE_BY_KUNDEID)
				.setParameter("id", id).getResultList();

		return ad;
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

		// Prüfen ob übergebenes Kundenobjekt korrekt ist
		validateKunde(pKD, pLocale, Default.class, IdGroup.class,
				PasswordGroup.class);

		// Kunde vom Entitiy Manager trennen
		em.detach(pKD);

		// Prüfen ob übergebener Kunde konkurrierend gelöscht wurde
		final Kunde existingKunde = findKundeById(pKD.getKundeID(), pLocale);
		if (existingKunde == null) {
			throw new ConcurrentDeletedException(pKD.getKundeID());
		}
		em.detach(existingKunde);

		// Prüfen ob zu ändernde E-Mail Adresse schon vorhanden ist
		if (!pKD.getEmail().equals(existingKunde.getEmail())) {
			final Kunde kd = findKundeByMail(FetchType.JUST_KUNDE,
					pKD.getEmail(), pLocale);
			if (kd != null) {
				throw new EmailExistsException(pKD.getEmail());
			}
		}

		// Prüfen ob Passwort geändert wurde
		if (pDifPass) {
			salting(pKD);
		}

		pKD = em.merge(pKD);

		pKD.setPasswordWdh(pKD.getPassword());

		// Datenbank synchronisieren
		em.flush();

		return pKD;
	}

	/**
	 * Aendert eine vorhandene Adresse
	 * 
	 * @param adresse
	 * @return
	 */
	public Adresse updateAdresse(Adresse pAD, Locale pLocale) {

		// Validierung Adresse
		validateAdresse(validatorProvider.getValidator(pLocale).validate(pAD));

		// Adresse vom Entitiy Manager trennen
		em.detach(pAD);

		// Prüfen ob übergebener Kunde konkurrierend gelöscht wurde
		final Adresse existingAdresse = findAdresseById(pAD.getAdresseID(),
				pLocale);
		if (existingAdresse == null) {
			throw new ConcurrentDeletedException(pAD.getAdresseID());
		}

		// Neue Adresse speichern
		em.merge(pAD);

		// Datenbank synchronisieren
		em.flush();

		return pAD;
	}

	/**
	 * Kunde löschen
	 */
	public void deleteKundeById(int pKID, Locale pLocale) {
		Kunde kd;
		try {
			kd = findKundeById(pKID, pLocale);
		} catch (InvalidKundeIdException e) {
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
		logger.debugf("salting BEGINN: %s", pKD);

		final String unverschluesselt = pKD.getPassword();
		final String verschluesselt = authService
				.verschluesseln(unverschluesselt);
		pKD.setPassword(verschluesselt);
		pKD.setPasswordWdh(verschluesselt);

		logger.debugf("salting ENDE: %s", verschluesselt);
	}

	/**
	 */
	private boolean hasBestellungen(Kunde pKD) {
		logger.debugf("hasBestellungen BEGINN: %s", pKD);

		boolean result = false;

		// Gibt es den Kunden und hat er mehr als eine Bestellung?
		// Bestellungen nachladen wegen Hibernate-Caching
		if (pKD != null && pKD.getBestellungen() != null
				&& !pKD.getBestellungen().isEmpty()) {
			result = true;
		}

		logger.debugf("hasBestellungen ENDE: %s", result);
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
			final StringBuffer temp = new StringBuffer();

			final java.util.Iterator<ConstraintViolation<Kunde>> it = violations
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

	private void validateAdresse(Set<ConstraintViolation<Adresse>> violations) {

		if (!violations.isEmpty()) {
			final StringBuffer buffer = new StringBuffer();
			final Iterator<ConstraintViolation<Adresse>> it = violations
					.iterator();
			while (it.hasNext()) {
				buffer.append(it.next().getMessage());
				buffer.append('\n');
			}
			throw new AdresseValidationException(buffer.toString());
		}
	}

}
