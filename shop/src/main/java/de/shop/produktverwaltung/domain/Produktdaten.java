package de.shop.produktverwaltung.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

import de.shop.util.DateFormatter;
import de.shop.util.IdGroup;
import de.shop.util.ProduktdatenIdGroup;

/**
 * Diese Klasse Produktdaten repräsentiert eine Variante eines bestimmten
 * Produktes. So kann ein Produkt unterschieden werden in Farbe, Groesse und
 * auch im Preis.
 * 
 * @see Produkt
 * @author Andreas Güntzel
 * 
 */
//@formatter:off
@Entity
@Table(name = "Produktdaten")
@NamedQueries({ 
	@NamedQuery(
			name = Produktdaten.PRODUKTDATEN_KOMPLETT,
			query = "FROM Produktdaten pd"),
	@NamedQuery(
			name = Produktdaten.PRODUKTDATEN_BY_GROESSE, 
			query = "FROM Produktdaten pd WHERE pd.groesse = :groesse"),
	@NamedQuery(
			name = Produktdaten.PRODUKTDATEN_BY_PRODUKT_ID,
			query = "select produktdaten from Produktdaten as produktdaten "
					+ "WHERE produktdaten.produkt.produktId = :id") })
//@formatter:on
public class Produktdaten implements Serializable {

	// /////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final long serialVersionUID = 5848387327360651803L;

	private static final String PREFIX = "Produktdaten.";

	public static final String PRODUKTDATEN_BY_GROESSE = PREFIX
			+ "findeProduktdatenByGroesse";

	public static final String PRODUKTDATEN_KOMPLETT = PREFIX
			+ "findeAlleProduktdaten";
	
	public static final String PRODUKTDATEN_BY_PRODUKT_ID = PREFIX + "findeProduktdatenByProduktId";

	@Id
	@GeneratedValue
	@Column(name = "Produktdaten_ID", updatable = false)
	@NotNull(groups = ProduktdatenIdGroup.class, message = "{produktverwaltung.id.daten.notnull}")
	private Integer produktdatenID;

	@Column(name = "Anzahl_verfuegbar")
	@Min(value = 0, message = "{produktverwaltung.anzahl.min}")
	private int anzahlVerfuegbar;

	@Column(name = "Groesse")
	@NotEmpty(message = "{produktverwaltung.groesse.notempty}")
	private String groesse;

	@Column(name = "Preis")
	@Min(value = 1, message = "{produktverwaltung.preis.min}")
	private double preis;

	@Column(name = "Farbe")
	@NotEmpty(message = "{produktverwaltung.farbe.notempty}")
	private String farbe;

	@ManyToOne
	@JoinColumn(name = "Produkt_FK")
	@Valid
	@NotNull(message = "{produktverwaltung.produkt.notnull}")
	private Produkt produkt;

	@Column(name = "Erstellt")
	@Past
	@JsonIgnore
	private Date erstellt;

	@Column(name = "Geaendert")
	@Past
	@JsonIgnore
	private Date geaendert;
	
	@Version
	@Basic(optional = false)
	private int version = 0;

	// /////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR

	public Produktdaten() {

		this.produktdatenID = null;
		this.anzahlVerfuegbar = 0;
		this.groesse = "";
		this.preis = 0;
		this.farbe = "";
		this.produkt = null;
		this.erstellt = DateFormatter.korrigiereDatum(new Date());
		this.geaendert = DateFormatter.korrigiereDatum(new Date());

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

	@Override
	public String toString() {
		return "Produktdaten [produktdatenID=" + produktdatenID
				+ ", anzahl_verfuegbar=" + anzahlVerfuegbar + ", groesse="
				+ groesse + ", preis=" + preis + ", farbe=" + farbe + "]";
	}

	// /////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + anzahlVerfuegbar;
		result = prime * result + ((farbe == null) ? 0 : farbe.hashCode());
		result = prime * result + ((groesse == null) ? 0 : groesse.hashCode());
		long temp;
		temp = Double.doubleToLongBits(preis);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((produktdatenID == null) ? 0 : produktdatenID.hashCode());
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
		final Produktdaten other = (Produktdaten) obj;
		if (anzahlVerfuegbar != other.anzahlVerfuegbar)
			return false;
		if (farbe == null) {
			if (other.farbe != null)
				return false;
		}
		else if (!farbe.equals(other.farbe))
			return false;
		if (groesse == null) {
			if (other.groesse != null)
				return false;
		}
		else if (!groesse.equals(other.groesse))
			return false;
		if (Double.doubleToLongBits(preis) != Double
				.doubleToLongBits(other.preis))
			return false;
		if (produktdatenID == null) {
			if (other.produktdatenID != null)
				return false;
		}
		else if (!produktdatenID.equals(other.produktdatenID))
			return false;
		return true;
	}

	public Integer getProduktdatenID() {
		return this.produktdatenID;
	}

	public void setProduktdatenID(Integer produktdatenID) {
		this.produktdatenID = produktdatenID;
	}

	public String getGroesse() {
		return groesse;
	}

	public void setGroesse(String groesse) {
		this.groesse = groesse;
	}

	public int getAnzahlVerfuegbar() {
		return this.anzahlVerfuegbar;
	}

	public void setAnzahlVerfuegbar(int anzahlVerfuegbar) {
		this.anzahlVerfuegbar = anzahlVerfuegbar;
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

	public String getFarbe() {
		return this.farbe;
	}

	public void setFarbe(String farbe) {
		this.farbe = farbe;
	}

	public double getPreis() {
		return this.preis;
	}

	public void setPreis(double preis) {
		this.preis = preis;
	}

	public Produkt getProdukt() {
		return this.produkt;
	}

	public void setProdukt(Produkt produkt) {
		this.produkt = produkt;
	}
}
