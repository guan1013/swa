package de.shop.bestellverwaltung.rest;


import static de.shop.util.TestConstants.BESTELLPOSTEN_PATH;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_BESTELLUNG_PATH;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.json.JsonObject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;
import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.KUNDEN_PATH;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellpostenResourceTest extends AbstractResourceTest{
	
	private static final Integer EXISTING_ID = Integer.valueOf(602);
	private static final Integer EXISTING_BESTELLUNG_FK = Integer.valueOf(501);
	private static final Integer EXISTING_PRODUKTDATEN_FK = Integer.valueOf(401);
	private static final Integer ID_FOR_DELETE = Integer.valueOf(602);
	// ///////////////////////////////////////////////////////////////////
		// ATTRIBUTES
		private static final Logger LOGGER = Logger.getLogger(MethodHandles
				.lookup().lookupClass().getName());
		

@Ignore		
@Test
//Bei Depression alle anderen Tests ignoren
public void MotivationsTest() {
	
	assertThat(1, is(1));
}
@Ignore
@Test
public void testeAddBestellposten() {

	LOGGER.finer("BEGINN");

	// When
	final JsonObject jsonObject = getJsonBuilderFactory()
			.createObjectBuilder().add("bestellung", 501)
			.add("produktdaten", 408)
			.add("anzahl", 11).build();

	final Response response = given().contentType(APPLICATION_JSON)
			.body(jsonObject.toString()).post(BESTELLPOSTEN_PATH);

	// Then
	assertThat(response.getStatusCode(), is(HTTP_CREATED));
	
	LOGGER.finer("ENDE");

	}

@Ignore
@Test
public void testeDeleteBestellposten() {
	LOGGER.finer("BEGINN");

	// When
	final Response response = given().pathParameter(BESTELLPOSTEN_ID_PATH_PARAM,
			ID_FOR_DELETE).delete(BESTELLPOSTEN_ID_PATH);

	// Then
	assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

	LOGGER.finer("ENDE");
		}
}