package de.shop.produktverwaltung.domain;

import static javax.persistence.TemporalType.DATE;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import static javax.persistence.CascadeType.ALL;

import de.shop.util.DateFormatter;
import de.shop.util.IdGroup;

/**
 * Die Klasse Produkt repräsentiert ein Produkt des Shops. Von jedem Produkt
 * kann es Varianten (Produktdaten) geben, die sich in Farbe, Größe, etc
 * unterscheiden.
 * 
 * @see Produktdaten
 * @author Andreas Güntzel
 * 
 */
// @formatter:off
@Entity
@Table(name = "Produkt")
@NamedQueries({
		@NamedQuery(name = Produkt.PRODUKT_KOMPLETT, query = "FROM Produkt p"),
		@NamedQuery(name = Produkt.PRODUKT_ID_FETCH, query = "SELECT DISTINCT p FROM Produkt p LEFT JOIN p.produktdaten WHERE p.produktID = :id"),
		@NamedQuery(name = Produkt.PRODUKT_MIT_PRODUKTDATEN, query = "SELECT distinct p FROM Produkt p JOIN p.produktdaten"),
		@NamedQuery(name = Produkt.PRODUKT_BY_HERSTELLER, query = "FROM Produkt p WHERE p.hersteller = :hersteller"),
		@NamedQuery(name = Produkt.PRODUKT_BY_LIKE_BESCHREIBUNG, query = "SELECT produkt FROM Produkt as produkt "
				+ "WHERE beschreibung LIKE CONCAT('%',:beschreibung,'%')") })
// @formatter:on
public class Produkt implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	/**
	 * ID zum Serialisieren/Deserialisieren
	 */
	private static final long serialVersionUID = -1541274621941405339L;

	/**
	 * Prefix dieser Klasse, welches den Namen der Queries vorangestellt wird
	 */
	private static final String PREFIX = "Produkt.";

	public static final String PRODUKT_ID_FETCH = "SucheNachIdMitFetchJoin";

	/**
	 * Name für eine Query, die nach bestimmten Herstellern sucht
	 */
	public static final String PRODUKT_BY_HERSTELLER = PREFIX
			+ "SucheNachHersteller";

	/**
	 * Name für eine Query, die nur Produkte mit Produktdaten sucht
	 */
	public static final String PRODUKT_MIT_PRODUKTDATEN = PREFIX
			+ "SucheMitProduktdaten";

	/**
	 * Name für eine Query, die Produkte mit einer bestimmten Beschreibung
	 * (LIKE) sucht
	 */
	public static final String PRODUKT_BY_LIKE_BESCHREIBUNG = PREFIX
			+ "SucheAehnlicheBeschreibung";

	public static final String PRODUKT_KOMPLETT = PREFIX + "SucheAlleProdukte";

	/**
	 * Die ID des Produktes. Wird von Hibernate automatisch generiert
	 */
	@Id
	@GeneratedValue
	@Column(name = "Produkt_ID", updatable = false, nullable = false)
	@Min(value = 1, groups = IdGroup.class, message = "{produktverwaltung.id.min}")
	@NotNull(groups = IdGroup.class, message = "{produktverwaltung.id.notnull}")
	private Integer produktID;

	// TODO Für Titel und Beschreibung eigene Attribute einführen
	/**
	 * Die Beschreibung/Titel des Produktes
	 */
	@NotEmpty(message = "{produktverwaltung.beschreibung.notempty}")
	@Column(name = "Beschreibung", length = 255)
	private String beschreibung;

	// TODO Eventuell Hersteller in eigene Klasse/Tabelle auslagern
	/**
	 * Der Hersteller des Produktes
	 */
	@NotEmpty(message = "{produktverwaltung.hersteller.notempty}")
	@Column(name = "Hersteller", length = 32)
	private String hersteller;

	/**
	 * Liste aller Varianten dieses Produktes
	 */
	@OneToMany(cascade = { ALL })
	@JoinColumn(name = "Produkt_FK")
	@NotNull(message = "{produktverwaltung.produktdaten.notnull}")
	@Valid
	@JsonIgnore
	private List<Produktdaten> produktdaten;

	@Transient
	private URI produktdatenURI;

	/**
	 * Erstelldatum des Produktes
	 */
	@Column(name = "Erstellt", nullable = false, updatable = false)
	@Past(message = "{produktverwaltung.erstellt.past}")
	@Temporal(DATE)
	@JsonIgnore
	private Date erstellt;

	/**
	 * Datum der letzten Änderung des Produktes
	 */
	@Column(name = "Geaendert", nullable = false)
	@Past(message = "{produktverwaltung.geaendert.past}")
	@Temporal(DATE)
	@JsonIgnore
	private Date geaendert;

	@Version
	@Basic(optional = false)
	@JsonProperty
	private int version = 0;

	// /////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR

	/**
	 * Standardkonstruktor, der die Attribute initialisiert
	 */
	public Produkt() {

		this.produktID = null;
		this.beschreibung = "";
		this.hersteller = "";
		this.produktdaten = new ArrayList<Produktdaten>();
		this.erstellt = DateFormatter.korrigiereDatum(new Date());
		this.geaendert = DateFormatter.korrigiereDatum(new Date());
	}

	/**
	 * Konstruktor mit Parameter für Beschreibung und Hersteller
	 * 
	 * @param beschreibung
	 *            Der Beschreibungstext des Produktes
	 * @param hersteller
	 *            Name des Herstellers des Produktes
	 */
	public Produkt(String beschreibung, String hersteller) {

		this();
		this.beschreibung = beschreibung;
		this.hersteller = hersteller;
	}

	// /////////////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Wird vor dem Speichern in die DB aufgerufen
	 */
	@PrePersist
	private void bereiteSpeichernVor() {

		this.erstellt = new Date();
		this.geaendert = new Date();

		erstellt = DateFormatter.korrigiereDatum(erstellt);
		geaendert = DateFormatter.korrigiereDatum(geaendert);
	}

	/**
	 * Wird vor dem Aktualisieren/Updaten in der DB aufgerufen
	 */
	@PreUpdate
	private void bereiteUpdateVor() {

		this.geaendert = new Date();

		DateFormatter.korrigiereDatum(geaendert);

	}

	/**
	 * Fügt der Liste der Produktdaten eine neue Variante hinzu
	 * 
	 * @param neueProduktdaten
	 *            Die neue Produktvariante, die hinzugefügt werden soll
	 * @return Das Produkt selbst
	 */
	public Produkt addProduktdaten(Produktdaten neueProduktdaten) {

		if (neueProduktdaten != null) {
			if (produktdaten == null) {
				produktdaten = new ArrayList<>();
			}
			produktdaten.add(neueProduktdaten);
			neueProduktdaten.setProdukt(this);
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((beschreibung == null) ? 0 : beschreibung.hashCode());
		result = prime * result
				+ ((produktID == null) ? 0 : produktID.hashCode());
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
		final Produkt other = (Produkt) obj;
		if (beschreibung == null) {
			if (other.beschreibung != null)
				return false;
		}
		else if (!beschreibung.equals(other.beschreibung))
			return false;
		if (produktID == null) {
			if (other.produktID != null)
				return false;
		}
		else if (!produktID.equals(other.produktID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Produkt [produktID=" + produktID + ", beschreibung="
				+ beschreibung + ", hersteller=" + hersteller + ", erstellt="
				+ erstellt + ", geaendert=" + geaendert + "]";
	}

	// /////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public Integer getProduktID() {
		return this.produktID;
	}

	public void setProduktID(Integer produktID) {
		this.produktID = produktID;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Date getErstellt() {
		return erstellt == null ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
	}

	public Date getGeaendert() {
		return geaendert == null ? null : (Date) geaendert.clone();
	}

	public void setGeaendert(Date geaendert) {
		this.geaendert = geaendert == null ? null : (Date) geaendert.clone();
	}

	public String getHersteller() {
		return this.hersteller;
	}

	public void setHersteller(String hersteller) {
		this.hersteller = hersteller;
	}

	public List<Produktdaten> getProduktdaten() {
		return Collections.unmodifiableList(produktdaten);
	}

	public void setProduktdaten(List<Produktdaten> produktdaten) {
		this.produktdaten = new ArrayList<>();
		this.produktdaten.addAll(produktdaten);
	}

	public URI getProduktdatenURI() {
		return produktdatenURI;
	}

	public void setProduktdatenURI(URI produktdatenURI) {
		this.produktdatenURI = produktdatenURI;
	}

}
