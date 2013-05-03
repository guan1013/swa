package de.shop.produktverwaltung.controller;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.util.Log;
import de.shop.util.Transactional;

@Named("pc")
@RequestScoped
@Log
public class ProduktController implements Serializable {

	private static final long serialVersionUID = 5513563371749151869L;

	@Inject
	private ProduktdatenService pdatenService;

	@Inject
	private ProduktService pService;

	@Inject
	private transient HttpServletRequest request;

	private Produkt produkt;

	private Integer produktId;

	@Inject
	private Flash flash;

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
	public void sucheAlleProduktdaten() {

		List<Produktdaten> liste = pdatenService.findProduktdatenKomplett();
		request.setAttribute("produktdaten", liste);

	}

	@Transactional
	public void sucheAlleProdukte() {
		List<Produkt> liste = pService.findProdukte();
		request.setAttribute("produkte", liste);
	}

	public Produkt getProdukt() {
		return produkt;
	}

	public Integer getProduktId() {
		return produktId;
	}

	public void setProduktId(Integer produktId) {
		this.produktId = produktId;
	}

}
