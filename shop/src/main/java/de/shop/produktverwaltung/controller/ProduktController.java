package de.shop.produktverwaltung.controller;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
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

	private static final long serialVersionUID = 5513563371749151869L;

	@Inject
	private ProduktService produktService;

	@Inject
	private ProduktdatenService produktdatenService;

	@Inject
	private transient HttpServletRequest request;

	private Integer produktId;

	private Produkt produktSearch;

	private Produkt produktView;

	public Produkt getProduktView() {
		return produktView;
	}

	private Produkt produktCreate;

	private Produkt produktUpdate;

	private List<Produktdaten> produktdatenSuche;

	private List<Produkt> produkteKomplett;

	private Produktdaten produktdatenCreate;

	private SuchFilter suchFilter;

	private List<String> hersteller;

	public List<String> getHersteller() {
		return hersteller;
	}

	private boolean geaendert = false;

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	@Transactional
	public void sucheById() {

		// Parameter-Test
		if (produktId == null) {
			LOGGER.debug("Produkt ID ist null!");
			return;
		}

		// Produkt suchen
		produktSearch = produktService.findProduktByID(produktId.intValue(),
				ProduktService.FetchType.NUR_PRODUKTE, null);

		produktUpdate = produktSearch;

		if (produktSearch == null) {
			return;
		}

		// Produktdaten nachladen
		request.setAttribute("sucheById", produktSearch.getProduktdaten()
				.size());

	}

	@Transactional
	public void sucheByFilter() {

		// Parameter-Test
		if (suchFilter == null) {
			LOGGER.debug("SuchFilter ist null!");
			return;
		}

		// Produktdaten suchen
		produktdatenSuche = produktdatenService.findProduktdatenByFilter(
				suchFilter, null);
	}

	@Transactional
	public void createEmptySuchfilter() {
		
		hersteller = produktService.findAlleHersteller();

		if (suchFilter != null) {
			return;
		}

		suchFilter = new SuchFilter();
	}

	public void createEmptyProdukt() {

		if (produktCreate != null) {
			return;
		}

		produktCreate = new Produkt();
		produktdatenCreate = new Produktdaten();

		produktCreate.addProduktdaten(produktdatenCreate);

	}

	@Transactional
	public String createProdukt() {

		produktService.addProdukt(produktCreate, null);

		produktCreate = null;

		ladeAlleProdukt();

		return "/index";
	}

	@Transactional
	public String updateProdukt() {

		if (!geaendert) {
			LOGGER.debug("Es fanden keine Veränderungen statt!");
			return "viewProdukt.jsf?produktId=" + produktUpdate.getProduktId();
		}

		produktService.updateProdukt(produktUpdate, null);
		produktView = produktUpdate;

		return "viewProdukt.jsf?produktId=" + produktUpdate.getProduktId();
	}

	@Transactional
	public void ladeProdukt() {

		// Flag setzen
		geaendert = false;

		// Query-Parameter auslesen
		String produktIdStr = request.getParameter("produktId");
		Integer produktId;

		try {
			// ID in int umwandeln und Produkt laden, Produktdaten nachladen
			produktId = Integer.valueOf(produktIdStr);
			produktView = produktService.findProduktByID(produktId,
					ProduktService.FetchType.NUR_PRODUKTE, null);
			request.setAttribute("anzahlProduktdaten", produktView
					.getProduktdaten().size());
		}
		catch (NumberFormatException e) {
			LOGGER.debugf("ProduktId=%s ist keine ZahL", produktIdStr);
		}
		finally {
			produktUpdate = produktView;
		}
	}

	public String addEmptyProduktdaten() {

		geaendert = true;
		produktUpdate.addProduktdaten(new Produktdaten());

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

	@PostConstruct
	@Transactional
	private void ladeAlleProdukt() {

		produkteKomplett = produktService.findProdukte();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER

	public Integer getProduktId() {
		return produktId;
	}

	public void setProduktId(Integer produktId) {
		this.produktId = produktId;
	}

	public List<Produktdaten> getProduktdatenSuche() {
		return produktdatenSuche;
	}

	public Produkt getProduktSearch() {
		return produktSearch;
	}

	public List<Produkt> getProdukteKomplett() {
		return produkteKomplett;
	}

	public SuchFilter getSuchFilter() {
		return suchFilter;
	}

	public Produkt getProduktCreate() {
		return produktCreate;
	}

	public Produktdaten getProduktdatenCreate() {
		return produktdatenCreate;
	}

	public Produkt getProduktUpdate() {
		return produktUpdate;
	}
}
