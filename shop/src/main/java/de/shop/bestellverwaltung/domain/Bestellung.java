package de.shop.bestellverwaltung.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.DateFormatter;
import de.shop.util.IdGroup;
import de.shop.util.XmlDateAdapter;

/**
 * Die Klasse Bestellung repräsentiert eine Bestellung eines Kunden. Sie
 * beinhaltet eine Liste mit den Posten der Bestellung, den Gesamtpreis und den
 * Kunden.
 * 
 * @see Bestellposten
 * @see Kunde
 * @author Andreas Güntzel & Matthias Schnell
 * 
 */
// @form:off
@Entity
@Table(name = "Bestellung")
@NamedQueries({
		@NamedQuery(name = Bestellung.ALL_BESTELLUNGEN, query = "from Bestellung b"),
		@NamedQuery(name = Bestellung.BESTELLUNG_BY_PREISSPANNE, query = "from Bestellung b "
				+ "where b.gesamtpreis " + "BETWEEN :min AND :max"),
		@NamedQuery(name = Bestellung.BESTELLUNG_BY_PREISSPANNE_WITH_BESTELLPOSTEN, query = "Select b "
				+ "from Bestellung b "
				+ "join b.bestellposten be "
				+ "where b.gesamtpreis " + "BETWEEN :min AND :max"),
		@NamedQuery(name = Bestellung.BESTELLUNG_BY_PREISSPANNE_WITH_KUNDE, query = "Select b "
				+ "from Bestellung b "
				+ "join b.kunde k "
				+ "where b.gesamtpreis " + "BETWEEN :min AND :max"),
		@NamedQuery(name = Bestellung.BESTELLUNG_BY_KUNDE_ID, query = "SELECT b "
				+ "from Bestellung b "
				+ "JOIN b.kunde k "
				+ "WHERE k.kundeID = :kid")
})
// @form:on
public class Bestellung implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	/**
	 * Prefix dieser Klasse für die Namen der NamedQueries
	 */
	private static final String PREFIX = "Bestellung.";

	/**
	 * Name für NamedQuery zur Suche aller Bestellungen
	 * 
	 */
	public static final String ALL_BESTELLUNGEN = PREFIX
			+ "findAllBestellungen";

	/**
	 * Name für NamedQuery zur Suche einer Bestellung innerhalb einer
	 * Preisspanne
	 */
	public static final String BESTELLUNG_BY_PREISSPANNE = PREFIX
			+ "findBestellungenByPreisspanne";

	/**
	 * Name für NamedQuery zur Suche einer Bestellung mit Bestellposten
	 * innerhalb einer Preisspanne
	 */
	public static final String BESTELLUNG_BY_PREISSPANNE_WITH_BESTELLPOSTEN = PREFIX
			+ "findBestellungenByPreisspanneWithBestellposten";

	/**
	 * Name für NamedQuery zur Suche einer Bestellung mit zugehörigem Kunden
	 * innerhalb einer Preisspanne
	 */
	public static final String BESTELLUNG_BY_PREISSPANNE_WITH_KUNDE = PREFIX
			+ "findBestellungenByPreisspanneWithKunde";

	/**
	 * Name für NamedQuery zur Suche von Bestellungen eines bestimmten Kunden
	 */
	public static final String BESTELLUNG_BY_KUNDE_ID = PREFIX
			+ "findBestellungenByKundeId";

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 7920638273985939888L;

	/**
	 * ID der Bestellung
	 */
	@Id
	@GeneratedValue
	@Column(name = "Bestellung_ID", nullable = false, updatable = false)
	@Min(value = 1, groups = IdGroup.class, message = "{bestellverwaltung.bestellung.id.min}")
	private Integer bestellungID;
	
	@Version
	@Basic(optional = false)
	private int version = 0;
	
	/**
	 * Kunde, der diese Bestellung aufgegeben hat
	 */
	@ManyToOne
	@JoinColumn(name = "Kunde_FK")
	@Valid
	@NotNull(message = "{bestellverwaltung.bestellung.kunde.notNull}")
	@JsonProperty ("kunden")
	@JsonIgnore
	private Kunde kunde;
	
	@Transient
	private URI kundeUri;

	/**
	 * Liste aller Bestellposten dieser Bestellung
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "Bestellung_FK")
	@NotNull(message = "{bestellverwaltung.bestellung.bestellpositionen.notNull}")
	@OrderColumn(name = "idx")
	@JsonIgnore
	private List<Bestellposten> bestellposten;

	/**
	 * URI für XML von Bestellposten
	 */
	@Transient
	private URI bestellpostenUri;

	/**
	 * Gesamtpreis dieser Bestellung
	 */
	@Min(1)
	@Column(name = "Gesamtpreis", nullable = false)
	private double gesamtpreis;

	/**
	 * Erstelldatum
	 */
	@NotNull(message = "{bestellverwaltung.bestellung.bestellpositionen.notNull}")
	@Past
	@Column(name = "Erstellt", nullable = false, updatable = false)
	//@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date erstellt;

	/**
	 * Änderungsdatum
	 */
	@Past
	@Column(name = "Geaendert", nullable = false)
	//@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date geaendert;
	


	// /////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR

	/**
	 * Standardkonstruktor, der alle Felder (leer) initialisiert
	 */
	public Bestellung() {

		this.erstellt = DateFormatter.korrigiereDatum(new Date());
		this.geaendert = DateFormatter.korrigiereDatum(new Date());
		this.gesamtpreis = 0.0;
		this.bestellposten = new ArrayList<>();
		this.kunde = null;
	}

	/**
	 * Spezieller Konstruktor zum Erstellen eines Bestellungsobjekts
	 * 
	 * @param pBP
	 * @param pKD
	 */
	public Bestellung(List<Bestellposten> pBP, Kunde pKD) {

		this();
		this.bestellposten = pBP;
		this.kunde = pKD;
		this.gesamtpreis = errechneGesamtpreis();
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
		geaendert = DateFormatter.korrigiereDatum(geaendert);
	}

	/**
	 * Methode zum errechnen des Gesamtpreises.
	 * 
	 * @param pBP
	 * @return errechneter Gesamtpreis
	 */
	public final Double errechneGesamtpreis() {

		Double gp = 0.0;

		for (Bestellposten bp : bestellposten) {
			gp += (bp.getProduktdaten().getPreis() * bp.getAnzahl());
		}

		return gp;
	}

	/**
	 * Fügt der Bestellung einen neuen Posten hinzu
	 * 
	 * @param neuerPosten
	 *            Der Posten, der hinzugefügt werden soll
	 * @return Die Bestellung, der hinzugefügt wurde
	 */
	public Bestellung addBestellposten(Bestellposten neuerPosten) {

		// Parameter-Test
		if (neuerPosten == null) {
			return null;
		}

		// Falls keine Liste existiert, Liste anlegen
		if (bestellposten == null) {
			bestellposten = new ArrayList<Bestellposten>();
		}

		// Posten hinzufügen
		bestellposten.add(neuerPosten);

		return this;
	}

	public Bestellung setValues(Bestellung pBE) {
		this.gesamtpreis = pBE.getGesamtpreis();
		this.kunde = pBE.getKunde();

		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bestellungID == null) ? 0 : bestellungID.hashCode());
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
		Bestellung other = (Bestellung) obj;
		if (bestellungID == null) {
			if (other.bestellungID != null)
				return false;
		}
		else if (!bestellungID.equals(other.bestellungID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bestellung [bestellungID=" + bestellungID + ", gesamtpreis="
				+ gesamtpreis + ", erstellt=" + erstellt + ", geaendert="
				+ geaendert + "]";
	}

	// /////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public Integer getBestellungID() {
		return bestellungID;
	}

	public void setBestellungID(Integer bestellungID) {
		this.bestellungID = bestellungID;
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

	public double getGesamtpreis() {
		return this.gesamtpreis;
	}

	public void setGesamtpreis(double gesamtpreis) {
		this.gesamtpreis = gesamtpreis;
	}

	public List<Bestellposten> getBestellposten() {
		return Collections.unmodifiableList(bestellposten);
	}

	public void setBestellposten(List<Bestellposten> bestellposten) {
		this.bestellposten = bestellposten;
	}

	public URI getBestellpostenUri() {
		return bestellpostenUri;
	}

	public void setBestellpostenUri(URI bestellpostenUri) {
		this.bestellpostenUri = bestellpostenUri;
	}

	public Kunde getKunde() {
		return this.kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}