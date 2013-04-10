package de.shop.kundenverwaltung.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.DateFormatter;
import de.shop.util.IdGroup;
import de.shop.util.XmlDateAdapter;

/**
 * Die Klasse Kunde beschreibt einen Kunden in der Datenbank. Ein Kunde kann
 * eine Adresse und mehrere Bestellungen haben.
 * 
 * @see Adresse
 * @see Bestellung
 * @author Matthias Schnell
 */
//@form:off
@Entity
@XmlRootElement
@Table(name = "Kunde")
@NamedQueries({
	@NamedQuery(
		name = Kunde.ALL_KUNDEN,
		query = "SELECT k FROM Kunde k"
	),
	@NamedQuery(
		name = Kunde.KUNDE_BY_NACHNAME, 
		query = "SELECT k FROM Kunde k " 
				+ "WHERE k.nachname = :name"),
	@NamedQuery(
		name = Kunde.KUNDE_BY_NACHNAME_JOIN_BESTELLUNG,
		query = "SELECT k FROM Kunde k JOIN k.bestellungen b " 
				+ "WHERE k.nachname = :name"),
	@NamedQuery(
		name = Kunde.KUNDE_BY_EMAIL,
		query = "SELECT k FROM Kunde k " 
				+ "WHERE k.email = :mail"),
	@NamedQuery(
		name = Kunde.KUNDE_BY_EMAIL_JOIN_BESTELLUNG,
		query = "SELECT k FROM Kunde k JOIN k.bestellungen b " 
				+ "WHERE k.email = :mail")
})
//@form:on
public class Kunde implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	/**
	 * Serial
	 */
	private static final long serialVersionUID = 1090137837509194681L;

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
	@XmlAttribute
	private Integer kundeID;

	/**
	 * E-Mail Adresse des Kunden
	 */
	@Column(name = "Email")
	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
	@XmlElement(required = true)
	private String email;

	/**
	 * Erstell Datum des Kunden
	 */
	@Column(name = "Erstellt")
	@Past(message = "{kundenverwaltung.kunde.seit.past}")
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date erstellt;

	/**
	 * Datum, wann die letzte Änderung an einem Kunden vorgenommen wurde
	 */
	@Column(name = "Geaendert")
	@Past
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date geaendert;

	/**
	 * Geburtsdatum des Kunden
	 */
	@Column(name = "Geburtsdatum")
	@Temporal(TemporalType.DATE)
	@Past
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date geburtsdatum;

	/**
	 * Nachname des Kunden
	 */
	@Column(name = "Nachname")
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = 2, max = 32, message = "{kundenverwaltung.kunde.length}")
	@Pattern(regexp = "[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?", message = "{kundenverwaltung.kunde.nachname.pattern}")
	@XmlElement(required = true)
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
	@OneToMany(mappedBy = "kunde")
	@XmlTransient
	@JsonIgnore
	private List<Bestellung> bestellungen;

	/**
	 * URI für XML von Bestellungen
	 */
	@Transient
	@XmlElement(name = "bestellungen")
	private URI bestellungenUri;

	/**
	 * Liste aller eingetragenen Adressen des Kunden
	 */
	@OneToMany(mappedBy = "kunde")
	@XmlTransient
	@JsonIgnore
	private List<Adresse> adressen;

	/**
	 * URI für XML von adressen
	 */
	@Transient
	@XmlElement(name = "adressen")
	private URI adressenUri;

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

	public Date getErstellt() {
		return this.erstellt == null ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
	}

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

	// /////////////////////////////////////////////////////////////////////
	// OVERRIDES
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + email.hashCode();
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
		Kunde other = (Kunde) obj;
		if (kundeID == null) {
			if (other.kundeID != null)
				return false;
		}
		else if (!kundeID.equals(other.kundeID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Kunde [kundeID=" + kundeID + ", email=" + email
				+ ", geburtsdatum=" + geburtsdatum + ", nachname=" + nachname
				+ ", vorname=" + vorname + "]";
	}

}