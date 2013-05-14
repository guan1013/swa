package de.shop.produktverwaltung.controller;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

import de.shop.produktverwaltung.domain.Produkt;
import de.shop.produktverwaltung.domain.Produktdaten;
import de.shop.produktverwaltung.service.ProduktService;
import de.shop.produktverwaltung.service.ProduktdatenService;
import de.shop.produktverwaltung.service.util.SuchFilter;
import de.shop.util.Client;
import de.shop.util.Log;
import de.shop.util.Transactional;

@Named("pc")
@SessionScoped
@Log
public class ProduktController implements Serializable {

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES

	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final String FLASH_KEY_SUCHE = "suchergebnis";

	private static final String FLASH_KEY_VIEW = "viewProdukt";

	private static final String FLASH_KEY_EDIT = "editProdukt";

	private static final String FLASH_KEY_EDIT_PRODUKTDATEN = "editProduktdaten";

	private static final long serialVersionUID = 5513563371749151869L;

	@Inject
	private ProduktdatenService produktdatenService;

	@Inject
	private ProduktService produktService;

	@Inject
	private transient HttpServletRequest request;

	@Inject
	private Flash flash;

	private Integer produktId;

	private List<Produkt> produkte;

	private Produkt neuesProdukt;

	private Produkt viewProdukt;

	private Produktdaten neueProduktdaten;

	private SuchFilter suchFilter;

	private Produktdaten editProduktdaten;

	private boolean geaendert = false;

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	@Transactional
	public void sucheById() {

		// Vorherige Suchergebnisse löschen
		flash.put(FLASH_KEY_SUCHE, null);

		// Parameter-Test
		if (produktId == null) {
			return;
		}

		Produkt produkt = produktService.findProduktByID(produktId.intValue(),
				ProduktService.FetchType.NUR_PRODUKTE, null);
		List<Produktdaten> suchErgebnis = new ArrayList<Produktdaten>();

		if (produkt == null) {
			return;
		}

		// Produktdaten nachladen, Suchergebnis der Ergebnisliste hinzufügen,
		// Puffern
		suchErgebnis.addAll(produkt.getProduktdaten());
		flash.put(FLASH_KEY_SUCHE, suchErgebnis);

	}

	@Transactional
	public void sucheByFilter() {

		// Vorherige Suchergebnisse löschen
		flash.put(FLASH_KEY_SUCHE, null);

		// Parameter-Test
		if (suchFilter == null) {
			return;
		}

		// Produktdaten suchen
		List<Produktdaten> result = produktdatenService
				.findProduktdatenByFilter(suchFilter, null);

		// Suchergebnis puffern
		flash.put(FLASH_KEY_SUCHE, result);
	}

	@PostConstruct
	@Transactional
	private void ladeAlleProdukt() {

		if (produkte == null) {
			produkte = produktService.findProdukte();
		}
	}

	@Transactional
	public void createEmptySuchfilter() {

		// Suchfilter ggf initialisieren
		if (suchFilter != null) {
			return;
		}
		suchFilter = new SuchFilter();
	}

	public void createEmptyProdukt() {

		if (neuesProdukt != null) {
			return;
		}

		neuesProdukt = new Produkt();
		neueProduktdaten = new Produktdaten();

		neuesProdukt.addProduktdaten(neueProduktdaten);

	}

	@Transactional
	public String createProdukt() {

		produktService.addProdukt(neuesProdukt, null);
		neuesProdukt = null;

		return "/index";

	}

	@Transactional
	public String updateProdukt() {

		if (!geaendert)
			return "/index";

		Produkt produkt = viewProdukt;

		if (produkt == null) {
			return "";
		}

		produktService.updateProdukt(produkt, new Locale("de"));

		return "viewProdukt?produktId=" + produkt.getProduktId();
	}

	@Transactional
	public void ladeProdukt() {

		geaendert = false;

		// Query-Parameter auslesen
		String produktIdStr = request.getParameter("produktId");
		Integer produktId;

		try {
			// ID in int umwandeln und Produkt laden, Produktdaten nachladen
			produktId = Integer.valueOf(produktIdStr);
			viewProdukt = produktService.findProduktByID(produktId,
					ProduktService.FetchType.NUR_PRODUKTE, null);
			request.setAttribute("anzahlProduktdaten", viewProdukt
					.getProduktdaten().size());
		}
		catch (NumberFormatException e) {
			// TODO: Fehlermeldung implementieren
		}
	}

	public void ladeProduktdaten(Produktdaten pdaten) {
		flash.put(FLASH_KEY_EDIT_PRODUKTDATEN, pdaten);
		this.editProduktdaten = pdaten;
	}

	public String addEmptyProduktdaten() {
		if (viewProdukt == null) {
			return "";
		}

		viewProdukt.addProduktdaten(new Produktdaten());

		return "";
	}

	public void geaendert(ValueChangeEvent event) {

		if (geaendert)
			return;
		if (event.getOldValue() == null) {
			if (event.getNewValue() != null)
				geaendert = true;
			return;
		}
		if (!event.getOldValue().equals(event.getNewValue()))
			geaendert = true;
	}

	@Transactional
	public List<String> findeGroessenByPrefix(String prefix) {

		List<String> result = produktService.findGroessenByPrefix(prefix);

		return result;

	}

	@Transactional
	public List<String> findeHerstellerByPrefix(String prefix) {

		List<String> result = produktService.findHerstellerPrefix(prefix);

		return result;

	}
	
	@Transactional
	public List<String> findeBeschreibungByPrefix(String prefix) {

		List<String> result = produktService.findBeschreibungPrefix(prefix);

		return result;

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public SuchFilter getSuchFilter() {
		return suchFilter;
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

	public Produkt getNeuesProdukt() {
		return neuesProdukt;
	}

	public Produktdaten getNeueProduktdaten() {
		return neueProduktdaten;
	}

	public Produktdaten getEditProduktdaten() {
		return editProduktdaten;
	}

	public Produkt getViewProdukt() {
		return viewProdukt;
	}
}
