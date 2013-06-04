package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import de.shop.util.DateFormatter;
import de.shop.util.IdGroup;

/**
 * Die Klasse Adresse repräsentiert eine Adresse eines Kunden. Jede Adresse hat
 * eine ID, einen Ort, eine PLZ und eine Straße.
 * 
 * @see Kunde
 * @author Yannick Gentner & Matthias Schnell
 * 
 */

// @formatter:off
@Entity
@Table(name = "Adresse")
@Cacheable
@NamedQueries({
		@NamedQuery(
				name = Adresse.ALL_ADRESSEN, 
				query = "SELECT DISTINCT a from Adresse a"),
		@NamedQuery(
				name = Adresse.ADRESSE_MIT_KUNDE, 
				query = "SELECT DISTINCT a from Adresse as a join a.kunde"),
		@NamedQuery(
				name = Adresse.ADRESSE_BY_KUNDEID, 
				query = "SELECT adresse from Adresse as adresse WHERE adresse.kunde.kundeID = :id"),
		@NamedQuery(
				name = Adresse.ADRESSE_MIT_KUNDE_BY_WOHNORT, 
				query = "SELECT DISTINCT a from Adresse as a join a.kunde where a.ort=:ort"),
		@NamedQuery(
				name = Adresse.ADRESSE_BY_WOHNORT, 
				query = "SELECT DISTINCT a FROM Adresse a WHERE a.ort = :ort"),
		@NamedQuery(
				name = Adresse.ADRESSE_MIT_KUNDE_BY_PLZ, 
				query = "SELECT DISTINCT a from Adresse as a join a.kunde where a.plz=:plz"),
		@NamedQuery(
				name = Adresse.ADRESSE_BY_PLZ, 
				query = "SELECT DISTINCT a FROM Adresse a WHERE a.plz = :plz"),
		@NamedQuery(
				name = Adresse.ADRESSE_MIT_KUNDE_BY_STRASSE, 
				query = "SELECT DISTINCT a from Adresse as a join a.kunde where a.strasse=:strasse"),
		@NamedQuery(
				name = Adresse.ADRESSE_BY_STRASSE, 
				query = "SELECT DISTINCT a FROM Adresse a WHERE a.strasse = :strasse"),
		@NamedQuery(
				name = Adresse.ADRESSE_MIT_KUNDE_BY_ADRESSEID, 
				query = "SELECT DISTINCT a from Adresse as a join a.kunde where a.adresseID=:adresseID"),
		@NamedQuery(
				name = Adresse.ADRESSE_BY_ADRESSEID, 
				query = "SELECT DISTINCT a FROM Adresse a WHERE a.adresseID = :adresseID") })
// @formatter:on
public class Adresse implements Serializable {

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// Attribute

	/**
	 * ID zum Serialisieren/Deserialisieren
	 */
	private static final long serialVersionUID = 8172291051549706788L;

	/**
	 * Prefix der Klasse, welches den Namen der Queries vorangestellt wird
	 */
	private static final String PREFIX = "Adresse.";

	/**
	 * Name eines Querys: Finde alle Adressen
	 */
	public static final String ALL_ADRESSEN = PREFIX + "AllAdressen";

	/**
	 * Name eines Querys: Finde alle Adressen nach Kunde ID
	 */
	public static final String ADRESSE_BY_KUNDEID = PREFIX
			+ "findeAdresseByKundeId";

	/**
	 * Name für eine Query, die nach einem bestimmten Wohnort sucht
	 */
	public static final String ADRESSE_BY_WOHNORT = PREFIX
			+ "findeKundeByWohnort";

	/**
	 * Name für eine Query, die nach einer bestimmten PLZ sucht
	 */
	public static final String ADRESSE_BY_PLZ = PREFIX + "findeKundeByPLZ";

	/**
	 * Name für eine Query, die nach einer bestimmten Strasse sucht
	 */
	public static final String ADRESSE_BY_STRASSE = PREFIX
			+ "findeKundeByStrasse";

	/**
	 * Name für eine Query, die nach einer bestimmten AdressenID sucht
	 */
	public static final String ADRESSE_BY_ADRESSEID = PREFIX
			+ "findeKundeByAdresseID";

	/**
	 * Name für eine Query, die nur nach Kunden mit Adresse sucht
	 */
	public static final String ADRESSE_MIT_KUNDE = PREFIX
			+ "findeKundeMitAdresse";

	/**
	 * Name für eine Query, die nach Kunden mit Wohnort sucht
	 */
	public static final String ADRESSE_MIT_KUNDE_BY_WOHNORT = PREFIX
			+ "findeKundeMitAdresseNachWohnort";

	/**
	 * Name für eine Query, die nach Kunden mit PLZ sucht
	 */
	public static final String ADRESSE_MIT_KUNDE_BY_PLZ = PREFIX
			+ "findeKundeMitAdresseNachPLZ";

	/**
	 * Name für eine Query, die nach Kunden mit STRASSE sucht
	 */
	public static final String ADRESSE_MIT_KUNDE_BY_STRASSE = PREFIX
			+ "findeKundeMitAdresseNachStrasse";

	/**
	 * Name für eine Query, die nach Kunden mit STRASSE sucht
	 */
	public static final String ADRESSE_MIT_KUNDE_BY_ADRESSEID = PREFIX
			+ "findeKundeMitAdresseNachAdresseID";

	/**
	 * Die ID der Adresse. Wird von Hibernate automatisch generiert
	 */
	@NotNull(groups = IdGroup.class, message = "{kundenverwaltung.adresse.id.NotNull}")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "adresse_id")
	@Min(value = 1, groups = IdGroup.class, message = "{kundenverwaltung.adresse.id.min}")
	private Integer adresseID = KEINE_ID;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;

	/**
	 * Erstelldatum der Adresse
	 */
	@Past(message = "{kundenverwaltung.adresse.erstellt.past}")
	@Column(name = "Erstellt", length = 32)
	@JsonIgnore
	private Date erstellt;

	/**
	 * Datum der letzten Änderung der Adresse
	 */
	@Past(message = "{kundenverwaltung.adresse.geaendert.past}")
	@Column(name = "Geaendert", length = 32)
	@JsonIgnore
	private Date geaendert;

	/**
	 * Ort der Adresse
	 */
	@NotEmpty(message = "{kundenverwaltung.adresse.ort.NotNull}")
	@Column(name = "Ort", length = 32)
	@Pattern(regexp = "[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?", message = "{kundenverwaltung.adresse.ort.pattern}")
	private String ort;

	/**
	 * PLZ der Adresse
	 */
	@Min(value = 10000, message = "{kundenverwaltung.adresse.plz.min}")
	@Column(name = "PLZ")
	private int plz;

	/**
	 * Strasse der Adresse
	 */
	@NotEmpty(message = "{kundenverwaltung.adresse.strasse.NotNull}")
	@Column(name = "Strasse", length = 32)
	private String strasse;

	/**
	 * Referenz der Adresse zu Kunde
	 */
	@ManyToOne
	@JoinColumn(name = "Kunde_FK")
	@Valid
	private Kunde kunde;

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// Constructor

	/**
	 * Standardkonstruktor, der die Attribute initialisiert
	 */
	public Adresse() {

		this.adresseID = null;
		this.ort = "";
		this.plz = 0;
		this.strasse = "";
		this.erstellt = DateFormatter.korrigiereDatum(new Date());
		this.geaendert = DateFormatter.korrigiereDatum(new Date());
	}

	/**
	 * Spezieller Konstruktor zum Erstellen einer Adresse
	 * 
	 * @param pAdresseID
	 *            ID der Adresse
	 * @param pOrt
	 *            Ort der Adresse
	 * @param pPlz
	 *            PLZ der Adresse
	 * @param pStrasse
	 *            Strasse der Adresse
	 */

	public Adresse(Integer pAdresseID, String pOrt, Integer pPlz,
			String pStrasse) {
		this();
		this.adresseID = pAdresseID;
		this.ort = pOrt;
		this.plz = pPlz;
		this.strasse = pStrasse;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// Methoden

	@PrePersist
	private void bereiteSpeichernVor() {
		this.erstellt = new Date();
		this.geaendert = new Date();

		erstellt = DateFormatter.korrigiereDatum(erstellt);
		geaendert = DateFormatter.korrigiereDatum(geaendert);
	}

	@PreUpdate
	private void bereiteUpdateVor() {
		this.geaendert = new Date();

		DateFormatter.korrigiereDatum(geaendert);

	}

	public Adresse setValues(Adresse adresse) {

		this.ort = adresse.getOrt();
		this.plz = adresse.getPlz();
		this.strasse = adresse.getStrasse();

		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adresseID == null) ? 0 : adresseID.hashCode());
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
		final Adresse other = (Adresse) obj;
		if (adresseID == null) {
			if (other.adresseID != null)
				return false;
		}
		else if (!adresseID.equals(other.adresseID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Adresse [adresseID=" + adresseID + ", erstellt=" + erstellt
				+ ", geaendert=" + geaendert + ", ort=" + ort + ", plz=" + plz
				+ ", strasse=" + strasse + "]";
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// Getter & Setter

	public int getAdresseID() {
		return this.adresseID;
	}

	public void setAdresseID(int adresseID) {
		this.adresseID = adresseID;
	}

	@JsonProperty("erstellt")
	public Date getErstellt() {
		return erstellt == null ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
	}

	@JsonProperty("geaendert")
	public Date getGeaendert() {
		return geaendert == null ? null : (Date) geaendert.clone();
	}

	public void setGeaendert(Date geaendert) {
		this.geaendert = geaendert == null ? null : (Date) geaendert.clone();
	}

	public String getOrt() {
		return this.ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public int getPlz() {
		return this.plz;
	}

	public void setPlz(int plz) {
		this.plz = plz;
	}

	public String getStrasse() {
		return this.strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public Kunde getKunde() {
		return this.kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
}
