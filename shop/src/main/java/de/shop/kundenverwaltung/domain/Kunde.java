package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;
import org.jboss.logging.Logger;

import de.shop.auth.service.jboss.AuthService.RolleType;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.DateFormatter;
import de.shop.util.File;
import de.shop.util.IdGroup;

/**
 * Die Klasse Kunde beschreibt einen Kunden in der Datenbank. Ein Kunde kann
 * eine Adresse und mehrere Bestellungen haben.
 * 
 * @see Adresse
 * @see Bestellung
 * @author Matthias Schnell
 */
// @form:off
@Entity
@Table(name = "Kunde")
@Cacheable
@NamedQueries({
		@NamedQuery(name = Kunde.ALL_KUNDEN, query = "SELECT k FROM Kunde k"),
		@NamedQuery(name = Kunde.KUNDE_BY_NACHNAME, query = "SELECT k FROM Kunde k "
				+ "WHERE k.nachname = :name"),
		@NamedQuery(name = Kunde.KUNDE_BY_NACHNAME_JOIN_BESTELLUNG, query = "SELECT k FROM Kunde k JOIN k.bestellungen b "
				+ "WHERE k.nachname = :name"),
		@NamedQuery(name = Kunde.KUNDE_BY_EMAIL, query = "SELECT k FROM Kunde k "
				+ "WHERE k.email = :mail"),
		@NamedQuery(name = Kunde.KUNDE_BY_EMAIL_JOIN_BESTELLUNG, query = "SELECT k FROM Kunde k JOIN k.bestellungen b "
				+ "WHERE k.email = :mail") })
// @form:on
@ScriptAssert(lang = "javascript", script = "(_this.password == null && _this.passwordWdh == null)"
		+ "|| (_this.password != null && _this.password.equals(_this.passwordWdh))", message = "{kundenverwaltung.kunde.password.notEqual}", groups = PasswordGroup.class)
public class Kunde implements Serializable, Cloneable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 1090137837509194681L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	/**
	 * Prefix die einem Query vorangestellt wird.
	 */
	private static final String PREFIX = "Kunde.";

	/**
	 * Name eines Querys: Finde alle Kunden
	 */
	public static final String ALL_KUNDEN = PREFIX + "AllKunden";

	/**
	 * Name eines Querys: Suche Kunde mittels Nachname
	 */
	public static final String KUNDE_BY_NACHNAME = PREFIX
			+ "findKundeByNachname";

	/**
	 * Name eines Querys: Suche Kunde mittels Nachname Join auf
	 * Bestellungstabelle
	 */
	public static final String KUNDE_BY_NACHNAME_JOIN_BESTELLUNG = PREFIX
			+ "findKundeByNachnameJoinBestellung";

	/**
	 * Name eines Querys: Suche Kunde mittels ID
	 */
	public static final String KUNDE_BY_ID = PREFIX + "findKundeByID";

	/**
	 * Name eines Querys: Suche Kunde mittels ID Join auf Bestellungstabelle
	 */
	public static final String KUNDE_BY_ID_JOIN_BESTELLUNG = PREFIX
			+ "findKundeByIDJoinBestellung";

	/**
	 * Name eines Querys: Suche Kunde mittels E-Mail Adresse
	 */
	public static final String KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";

	/**
	 * Name eines Querys: Suche Kunde mittels E-Mail Adresse Join auf
	 * Bestellungstabelle
	 */
	public static final String KUNDE_BY_EMAIL_JOIN_BESTELLUNG = PREFIX
			+ "findKundeByEmailJoinBestellung";

	/**
	 * Die von Hibernate generierte ID des Kunden
	 */
	@Id
	@GeneratedValue
	@Column(name = "kunde_id")
	@Min(value = 1, groups = IdGroup.class, message = "{kundenverwaltung.kunde.id.min}")
	private Integer kundeID = KEINE_ID;

	@Version
	@Basic(optional = false)
	@JsonProperty
	private int version = ERSTE_VERSION;

	/**
	 * E-Mail Adresse des Kunden
	 */
	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
	@NotNull(message = "kundeverwaltung.kunde.email.notNull")
	@Column(name = "Email")
	private String email;

	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "file_fk")
	@JsonIgnore
	private File pic;

	@Transient
	private URI picUri;

	/**
	 * Erstell Datum des Kunden
	 */
	@Column(name = "Erstellt")
	@JsonIgnore
	private Date erstellt;

	/**
	 * Datum, wann die letzte Änderung an einem Kunden vorgenommen wurde
	 */
	@Column(name = "Geaendert")
	@JsonIgnore
	private Date geaendert;

	/**
	 * Geburtsdatum des Kunden
	 */
	@Column(name = "Geburtsdatum")
	@Temporal(TemporalType.DATE)
	@Past
	private Date geburtsdatum;

	/**
	 * Nachname des Kunden
	 */
	@Column(name = "Nachname")
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = 2, max = 32, message = "{kundenverwaltung.kunde.length}")
	@Pattern(regexp = "[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?", message = "{kundenverwaltung.kunde.nachname.pattern}")
	private String nachname;

	/**
	 * Vorname des Kunden
	 */
	@Column(name = "Vorname")
	@NotNull(message = "{kundenverwaltung.kunde.vorname.notNull}")
	@Size(min = 3, max = 32, message = "{kundenverwaltung.kunde.vorname.length}")
	@Pattern(regexp = "[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?", message = "{kundenverwaltung.kunde.vorname.pattern}")
	private String vorname;

	/**
	 * Liste aller Bestellung des Kunden
	 */
	@OneToMany(mappedBy = "kunde", fetch = EAGER, cascade = { ALL })
	@JsonIgnore
	private List<Bestellung> bestellungen;

	/**
	 * URI für JSon von Bestellungen
	 */
	@Transient
	private URI bestellungenUri;

	/**
	 * Liste aller eingetragenen Adressen des Kunden
	 */
	@OneToMany(mappedBy = "kunde")
	@JsonIgnore
	private List<Adresse> adressen;

	/**
	 * URI für Json von adressen
	 */
	@Transient
	private URI adressenUri;

	@Column(length = 256)
	@Size(max = 256, message = "{kundenverwaltung.kunde.password.length}")
	private String password;

	@Transient
	@JsonIgnore
	private String passwordWdh;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_rolle", joinColumns = @JoinColumn(name = "kunde_fk", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {
			"kunde_fk", "rolle_fk" }))
	@Column(table = "kunde_rolle", name = "rolle_fk", nullable = false)
	private Set<RolleType> rollen;

	// /////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR

	/**
	 * Standart Konstruktur
	 */
	public Kunde() {
		this.email = null;
		this.erstellt = DateFormatter.korrigiereDatum(new Date());
		this.geaendert = DateFormatter.korrigiereDatum(new Date());
		this.nachname = "";
		this.vorname = "";
	}

	/**
	 * Spezieller Konstruktor zum Erstellen eines Kunden
	 * 
	 * @param pNachname
	 *            Nachname des Kunden
	 * @param pVorname
	 *            Vorname des Kunden
	 * @param pEmail
	 *            E-Mail Adresse des Kunden
	 */

	public Kunde(String pNachname, String pVorname, String pEmail) {
		this();
		this.nachname = pNachname;
		this.vorname = pVorname;
		this.email = pEmail;
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS
	@PrePersist
	protected void prePersist() {
		erstellt = new Date();
		geaendert = new Date();
	}

	@PostPersist
	protected void postPersist() {
		LOGGER.debugf("Neuer Kunde mit ID=%d", kundeID);
	}

	@PreUpdate
	protected void preUpdate() {
		geaendert = new Date();
	}

	@PostUpdate
	protected void postUpdate() {
		LOGGER.debugf("Kunde mit ID=%d aktualisiert: version=%d", kundeID,
				version);
	}

	@PostLoad
	protected void postLoad() {
		passwordWdh = password;

	}

	public Kunde addBestellung(Bestellung pBe) {

		// Plausalitätstest
		if (pBe == null) {
			return null;
		}

		// Falls keine Liste existiert, Liste anlegen
		if (bestellungen == null) {
			bestellungen = new ArrayList<Bestellung>();
		}

		// Posten hinzufügen
		bestellungen.add(pBe);

		return this;
	}

	public Kunde addAdresse(Adresse pAd) {

		// Plausalitätstest
		if (pAd == null) {
			return null;
		}

		// Falls keine Liste existiert, Liste anlegen
		if (adressen == null) {
			adressen = new ArrayList<Adresse>();
		}

		// Posten hinzufügen
		adressen.add(pAd);

		return this;
	}

	public Kunde setValues(Kunde pKD) {

		this.email = pKD.getEmail();
		this.geburtsdatum = pKD.getGeburtsdatum();
		this.nachname = pKD.getNachname();
		this.vorname = pKD.getVorname();

		return this;
	}

	// /////////////////////////////////////////////////////////////////////
	// GET & SETS
	public Integer getKundeID() {
		return this.kundeID;
	}

	public void setKundeID(Integer kundeID) {
		this.kundeID = kundeID;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@JsonProperty("erstellt")
	public Date getErstellt() {
		return this.erstellt == null ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
	}

	@JsonProperty("geaendert")
	public Date getGeaendert() {
		return this.geaendert == null ? null : (Date) geaendert.clone();
	}

	public void setGeaendert(Date geaendert) {
		this.geaendert = geaendert == null ? null : (Date) geaendert.clone();
	}

	public Date getGeburtsdatum() {
		return this.geburtsdatum == null ? null : (Date) geburtsdatum.clone();
	}

	public void setGeburtsdatum(Date geburtsdatum) {
		this.geburtsdatum = geburtsdatum == null ? null : (Date) geburtsdatum
				.clone();
	}

	public String getNachname() {
		return this.nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return this.vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}

	public Set<RolleType> getRollen() {
		return rollen;
	}

	public void setRollen(Set<RolleType> rollen) {
		this.rollen = rollen;
	}

	public List<Bestellung> getBestellungen() {
		return this.bestellungen;
	}

	public URI getBestellungenUri() {
		return this.bestellungenUri;
	}

	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	public List<Adresse> getAdressen() {
		return this.adressen;
	}

	public URI getAdressenUri() {
		return this.adressenUri;
	}

	public void setAdressenUri(URI adressenUri) {
		this.adressenUri = adressenUri;
	}

	public File getPic() {
		return pic;
	}

	public void setPic(File pic) {
		this.pic = pic;
	}

	public URI getPicUri() {
		return picUri;
	}

	public void setPicUri(URI picUri) {
		this.picUri = picUri;
	}

	// /////////////////////////////////////////////////////////////////////
	// OVERRIDES
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Kunde other = (Kunde) obj;
		if (kundeID == null) {
			if (other.kundeID != null)
				return false;
		} else if (!kundeID.equals(other.kundeID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Kunde [kundeID=" + kundeID + ", version=" + version
				+ ", nachname=" + nachname + ", vorname=" + vorname
				+ ", email=" + email + ", rollen=" + rollen + ", password="
				+ password + ", passwordWdh=" + passwordWdh + ", erstellt="
				+ erstellt + ", geaendert=" + geaendert + "]";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		final Kunde neuesObjekt = (Kunde) super.clone();
		neuesObjekt.kundeID = this.kundeID;
		neuesObjekt.version = this.version;
		neuesObjekt.nachname = this.nachname;
		neuesObjekt.vorname = this.vorname;
		neuesObjekt.email = this.email;
		neuesObjekt.password = this.password;
		neuesObjekt.passwordWdh = this.passwordWdh;
		neuesObjekt.adressen = this.getAdressen();
		neuesObjekt.erstellt = this.erstellt;
		neuesObjekt.geaendert = this.geaendert;
		return neuesObjekt;
	}

}
