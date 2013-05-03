package de.shop.produktverwaltung.controller;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.util.Log;
import de.shop.util.Transactional;

@Named("pc")
@SessionScoped
@Log
public class ProduktController implements Serializable {

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final long serialVersionUID = 5513563371749151869L;

	@Inject
	private ProduktdatenService pdatenService;

	@Inject
	private ProduktService pService;

	@Inject
	private transient HttpServletRequest request;

	private Produkt produkt;

	private Integer produktId;

	private List<Produkt> produkte;

	@Inject
	private Flash flash;

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	@Transactional
	public String sucheProdukt() {

		produkt = pService.findProduktByID(produktId.intValue(),
				ProduktService.FetchType.NUR_PRODUKTE, null);

		if (produkt == null) {
			flash.remove("produkt");
			return null;
		}

		flash.put("produkt", produkt);

		return "/produktverwaltung/viewProdukt";
	}

	@Transactional
	public void sucheAlleProdukte() {

		produkte = pService.findProdukte();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public Produkt getProdukt() {
		return produkt;
	}

	public Integer getProduktId() {
		return produktId;
	}

	public void setProduktId(Integer produktId) {
		this.produktId = produktId;
	}

	public List<Produkt> getProdukte() {
		return produkte;
	}
}
