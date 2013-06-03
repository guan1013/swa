package de.shop.kundenverwaltung.controller;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static de.shop.util.Messages.MessagesType.KUNDENVERWALTUNG;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import static javax.persistence.PersistenceContextType.EXTENDED;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.xml.bind.DatatypeConverter;

import org.jboss.logging.Logger;
import org.richfaces.cdi.push.Push;
import org.richfaces.component.SortOrder;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import de.shop.auth.controller.AuthController;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.exceptions.EmailExistsException;
import de.shop.util.exceptions.KundeValidationException;
import de.shop.util.exceptions.InvalidNachnameException;
import de.shop.util.exceptions.KundeDeleteBestellungException;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.AbstractShopException;
import de.shop.util.Client;
import de.shop.util.exceptions.ConcurrentDeletedException;
import de.shop.util.Messages;
import de.shop.util.File;
import de.shop.util.FileHelper;

/**
 * Dialogsteuerung fuer die Kundenverwaltung
 */
@Named("kc")
@SessionScoped
@Stateful
@TransactionAttribute(SUPPORTS)
public class KundeController implements Serializable {
	private static final long serialVersionUID = -8817180909526894740L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	private static final String JSF_KUNDENVERWALTUNG = "/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG
			+ "viewKunde";
	private static final String JSF_LIST_KUNDEN = JSF_KUNDENVERWALTUNG
			+ "listKunden";

	private static final String CLIENT_ID_KUNDEID = "form:kundeIdInput";
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "viewKunde.notFound";

	private static final String CLIENT_ID_KUNDEN_NACHNAME = "form:nachname";

	private static final String CLIENT_ID_CREATE_EMAIL = "createKundeForm:email";
	private static final String MSG_KEY_CREATE_KUNDE_EMAIL_EXISTS = "createKunde.emailExists";

	private static final Class<?>[] PASSWORD_GROUP = { PasswordGroup.class };

	private static final String CLIENT_ID_UPDATE_PASSWORD = "updateKundeForm:password";
	private static final String CLIENT_ID_UPDATE_EMAIL = "updateKundeForm:email";
	private static final String MSG_KEY_UPDATE_KUNDE_DUPLIKAT = "updateKunde.duplikat";
	private static final String MSG_KEY_UPDATE_KUNDE_CONCURRENT_UPDATE = "updateKunde.concurrentUpdate";
	private static final String MSG_KEY_UPDATE_KUNDE_CONCURRENT_DELETE = "updateKunde.concurrentDelete";

	private static final String MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG = "listKunden.deleteKundeBestellung";

	@PersistenceContext(type = EXTENDED)
	private transient EntityManager em;

	@Inject
	private KundeService ks;

	@Inject
	private transient HttpServletRequest request;

	@Inject
	private AuthController auth;

	@Inject
	@Client
	private Locale locale;

	@Inject
	private Messages messages;

	@Inject
	@Push(topic = "updateKunde")
	private transient Event<String> updateKundeEvent;

	@Inject
	private FileHelper fileHelper;

	private Integer kundeId;
	private Kunde kunde;

	private String nachname;

	private List<Kunde> kunden = Collections.emptyList();

	private SortOrder vornameSortOrder = SortOrder.unsorted;
	private String vornameFilter = "";

	private boolean geaendertKunde; // fuer ValueChangeListener
	private Kunde newKunde;

	private byte[] bytes;
	private String contentType;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	@Override
	public String toString() {
		return "KundenverwaltungController [kundeId=" + kundeId + ", nachname="
				+ nachname + ", geaendertKunde=" + geaendertKunde + "]";
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// METHODS

	@TransactionAttribute(REQUIRED)
	public String createKunde() {

		try {
			newKunde = (Kunde) ks.addKunde(newKunde, locale);
		} catch (KundeValidationException | EmailExistsException e) {
			final String outcome = createKundeErrorMsg(e);
			return outcome;
		}

		// Aufbereitung fuer viewKunde.xhtml
		kundeId = newKunde.getKundeID();
		kunde = newKunde;
		newKunde = null; // zuruecksetzen

		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}

	private String createKundeErrorMsg(AbstractShopException e) {
		final Class<? extends AbstractShopException> exceptionClass = e
				.getClass();
		if (exceptionClass.equals(EmailExistsException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_CREATE_KUNDE_EMAIL_EXISTS,
					CLIENT_ID_CREATE_EMAIL);
		} else if (exceptionClass.equals(KundeValidationException.class)) {
			final KundeValidationException orig = (KundeValidationException) e;
			messages.error(orig.getViolations(), null);
		}

		return null;
	}

	public void createEmptyKunde() {
		if (newKunde != null) {
			return;
		}

		newKunde = new Kunde();
		final Adresse adresse = new Adresse();
		adresse.setKunde(newKunde);
		newKunde.addAdresse(adresse);
	}

	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * 
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	@TransactionAttribute(REQUIRED)
	public String findKundeById() {
		// Bestellungen werden durch "Extended Persistence Context" nachgeladen
		kunde = ks.findKundeById(kundeId, locale);

		if (kunde == null) {
			// Kein Kunde zu gegebener ID gefunden
			return findKundeByIdErrorMsg(kundeId.toString());
		}

		return JSF_VIEW_KUNDE;
	}

	private String findKundeByIdErrorMsg(String id) {
		messages.error(KUNDENVERWALTUNG, MSG_KEY_KUNDE_NOT_FOUND_BY_ID,
				CLIENT_ID_KUNDEID, id);
		return null;
	}

	@TransactionAttribute(REQUIRED)
	public void loadKundeById() {
		// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
		final String idStr = request.getParameter("kundeId");
		Integer kid;
		try {
			kid = Integer.valueOf(idStr);
		} catch (NumberFormatException e) {
			return;
		}

		// Suche durch den Anwendungskern
		kunde = ks.findKundeById(kid, locale);
		if (kunde == null) {
			return;
		}
	}

	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * 
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	@TransactionAttribute(REQUIRED)
	public String findKundenByNachname() {
		if (nachname == null || nachname.isEmpty()) {
			kunden = ks.findAllKunden();
			return JSF_LIST_KUNDEN;
		}

		try {
			kunden = ks.findKundeByNachname(FetchType.WITH_BESTELLUNGEN,
					nachname, locale);
		} catch (InvalidNachnameException e) {
			final Collection<ConstraintViolation<Kunde>> violations = e
					.getViolations();
			messages.error(violations, CLIENT_ID_KUNDEN_NACHNAME);
			return null;
		}
		return JSF_LIST_KUNDEN;
	}

	/**
	 * https://issues.jboss.org/browse/AS7-1348
	 * http://community.jboss.org/thread/169487
	 */
	public Class<?>[] getPasswordGroup() {
		return PASSWORD_GROUP.clone();
	}

	/**
	 * Verwendung als ValueChangeListener bei updateKunde.xhtml und
	 * updateFirmenkunde.xhtml
	 */
	public void geaendert(ValueChangeEvent e) {
		if (geaendertKunde) {
			return;
		}

		if (e.getOldValue() == null) {
			if (e.getNewValue() != null) {
				geaendertKunde = true;
			}
			return;
		}

		if (!e.getOldValue().equals(e.getNewValue())) {
			geaendertKunde = true;
		}
	}

	@TransactionAttribute(REQUIRED)
	public String updateKunde() {
		auth.preserveLogin();

		if (!geaendertKunde || kunde == null) {
			return JSF_INDEX;
		}

		LOGGER.tracef("Aktualisierter Kunde: %s", kunde);
		try {
			kunde = ks.updateKunde(kunde, locale, false);
		} catch (EmailExistsException | KundeValidationException
				| OptimisticLockException | ConcurrentDeletedException e) {
			final String outcome = updateErrorMsg(e);
			return outcome;
		}

		// Push-Event fuer Webbrowser
		updateKundeEvent.fire(String.valueOf(kunde.getKundeID()));

		// ValueChangeListener zuruecksetzen
		geaendertKunde = false;

		// Aufbereitung fuer viewKunde.xhtml
		kundeId = kunde.getKundeID();

		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}

	private String updateErrorMsg(RuntimeException e) {
		final Class<? extends RuntimeException> exceptionClass = e.getClass();
		if (exceptionClass.equals(KundeValidationException.class)) {
			// Ungueltiges Password: Attribute wurden bereits von JSF validiert
			final KundeValidationException orig = (KundeValidationException) e;
			final Collection<ConstraintViolation<Kunde>> violations = orig
					.getViolations();
			messages.error(violations, CLIENT_ID_UPDATE_PASSWORD);
		} else if (exceptionClass.equals(EmailExistsException.class)) {

			messages.error(KUNDENVERWALTUNG, MSG_KEY_UPDATE_KUNDE_DUPLIKAT,
					CLIENT_ID_UPDATE_EMAIL);

		} else if (exceptionClass.equals(OptimisticLockException.class)) {

			messages.error(KUNDENVERWALTUNG,
					MSG_KEY_UPDATE_KUNDE_CONCURRENT_UPDATE, null);

		} else if (exceptionClass.equals(ConcurrentDeletedException.class)) {

			messages.error(KUNDENVERWALTUNG,
					MSG_KEY_UPDATE_KUNDE_CONCURRENT_DELETE, null);
		}
		return null;
	}

	@TransactionAttribute(REQUIRED)
	public String delete(Kunde ausgewaehlterKunde) {
		try {
			ks.deleteKundeById(ausgewaehlterKunde.getKundeID(), locale);
		} catch (KundeDeleteBestellungException e) {
			messages.error(KUNDENVERWALTUNG,
					MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG, null,
					e.getKundeId(), e.getAnzahlBestellungen());
			return null;
		}

		kunden.remove(ausgewaehlterKunde);
		return null;
	}

	public void uploadListener(FileUploadEvent event) {
		final UploadedFile uploadedFile = event.getUploadedFile();
		contentType = uploadedFile.getContentType();
		bytes = uploadedFile.getData();
	}

	@TransactionAttribute(REQUIRED)
	public String upload() {
		kunde = ks.findKundeById(kundeId, locale);
		if (kunde == null) {
			return null;
		}
		ks.setKundePic(kunde, bytes, contentType);

		kundeId = null;
		bytes = null;
		contentType = null;
		kunde = null;

		return JSF_INDEX;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public Integer getKundeId() {
		return kundeId;
	}

	public void setKundeId(Integer kundeId) {
		this.kundeId = kundeId;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public List<Kunde> getKunden() {
		return kunden;
	}

	public SortOrder getVornameSortOrder() {
		return vornameSortOrder;
	}

	public void setVornameSortOrder(SortOrder vornameSortOrder) {
		this.vornameSortOrder = vornameSortOrder;
	}

	public void sortByVorname() {
		vornameSortOrder = vornameSortOrder.equals(SortOrder.ascending) ? SortOrder.descending
				: SortOrder.ascending;
	}

	public String getVornameFilter() {
		return vornameFilter;
	}

	public void setVornameFilter(String vornameFilter) {
		this.vornameFilter = vornameFilter;
	}

	public Kunde getNewKunde() {
		return newKunde;
	}

	public Date getAktuellesDatum() {
		final Date datum = new Date();
		return datum;
	}

	public String getFilename(File file) {
		if (file == null) {
			return "";
		}

		fileHelper.store(file);
		return file.getFilename();
	}

	public String getBase64(File file) {
		return DatatypeConverter.printBase64Binary(file.getBytes());
	}
}
