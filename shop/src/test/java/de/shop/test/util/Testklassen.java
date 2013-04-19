package de.shop.test.util;

import java.util.Arrays;
import java.util.List;

import de.shop.test.kundenverwaltung.model.KundeTest;
import de.shop.test.kundenverwaltung.service.KundeServiceTest;

public enum Testklassen {
	INSTANCE;
	
	// Testklassen aus *VERSCHIEDENEN* Packages auflisten (durch Komma getrennt):
	// so dass alle darin enthaltenen Klassen ins Web-Archiv mitverpackt werden
	private List<Class<? extends AbstractTest>> classes = Arrays.asList(KundeTest.class, KundeServiceTest.class);
	
	public static Testklassen getInstance() {
		return INSTANCE;
	}
	
	public List<Class<? extends AbstractTest>> getTestklassen() {
		return classes;
	}
}
