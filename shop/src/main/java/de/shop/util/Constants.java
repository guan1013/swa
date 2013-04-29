package de.shop.util;

public final class Constants {
	// JPA
	public static final Integer KEINE_ID = null;
	public static final long MIN_ID = 1L;
	public static final int ERSTE_VERSION = 0;
	
	// JAAS
	public static final String SECURITY_DOMAIN = "shop";
	public static final String KUNDE_ROLLE_TABELLE = "kunde_rolle";
	
	public static final String HASH_ALGORITHM = "SHA-256";
	public static final String HASH_ENCODING = "base64";
	public static final String HASH_CHARSET = "UTF-8";
	
	// REST
	public static final String PRODUKTVERWALTUNG_NS = "urn:shop:produktverwaltung";
	public static final String BESTELLVERWALTUNG_NS = "urn:shop:bestellverwaltung";
	public static final String KUNDENVERWALTUNG_NS = "urn:shop:kundenverwaltung";
	
	private Constants() {
	}
}
