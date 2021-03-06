package de.shop.produktverwaltung.service.util;

import java.io.Serializable;

public class SuchFilter implements Serializable {

	private static final long serialVersionUID = 8947790766585826410L;

	private Integer anzahl;
	
	private Double preisUnten;
	
	private Double preisOben;
	
	private String farbe;
	
	private String groesse;
	
	private String hersteller;
	
	private String beschreibung;

	public SuchFilter() {
		super();
		this.anzahl = 0;
		this.preisUnten = 0.0;
		this.preisOben = 0.0;
		this.farbe = "";
		this.groesse = "";
		this.hersteller = "";
		this.beschreibung = "";
	}

	public Integer getAnzahl() {
		return anzahl;
	}

	public void setAnzahl(Integer anzahl) {
		this.anzahl = anzahl;
	}

	public Double getPreisUnten() {
		return preisUnten;
	}

	public void setPreisUnten(Double preisUnten) {
		this.preisUnten = preisUnten;
	}

	public Double getPreisOben() {
		return preisOben;
	}

	public void setPreisOben(Double preisOben) {
		this.preisOben = preisOben;
	}

	public String getFarbe() {
		return farbe;
	}

	public void setFarbe(String farbe) {
		this.farbe = farbe;
	}

	public String getGroesse() {
		return groesse;
	}

	public void setGroesse(String groesse) {
		this.groesse = groesse;
	}

	public String getHersteller() {
		return hersteller;
	}

	public void setHersteller(String hersteller) {
		this.hersteller = hersteller;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	@Override
	public String toString() {
		return "SuchFilter [anzahl=" + anzahl + ", preisUnten=" + preisUnten
				+ ", preisOben=" + preisOben + ", farbe=" + farbe
				+ ", groesse=" + groesse + ", hersteller=" + hersteller
				+ ", beschreibung=" + beschreibung + "]";
	}
	
	
	
}
