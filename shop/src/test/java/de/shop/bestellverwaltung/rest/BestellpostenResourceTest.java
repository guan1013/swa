package de.shop.bestellverwaltung.rest;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import de.shop.util.AbstractResourceTest;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellpostenResourceTest extends AbstractResourceTest{
	
	private static final Integer EXISTING_ID = Integer.valueOf(602);
	private static final Integer EXISTING_BESTELLUNG_FK = Integer.valueOf(501);
	private static final Integer EXISTING_PRODUKTDATEN_FK = Integer.valueOf(401);
	// ///////////////////////////////////////////////////////////////////
		// ATTRIBUTES
		private static final Logger LOGGER = Logger.getLogger(MethodHandles
				.lookup().lookupClass().getName());


@Ignore
//@Test
public void testeAddBestellposten() {

	LOGGER.finer("BEGINN");

	// Hier passiert ein Wunder
	
	LOGGER.finer("ENDE");

	}

@Ignore
//@Test
public void testeDeleteBestellposten() {
	LOGGER.finer("BEGINN");

	// Hier passiert ein Wunder

	LOGGER.finer("ENDE");
		}
}