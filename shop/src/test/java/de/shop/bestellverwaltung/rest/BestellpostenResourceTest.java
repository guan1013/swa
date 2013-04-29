package de.shop.bestellverwaltung.rest;

import static de.shop.util.TestConstants.BESTELLPOSTEN_PATH;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH;

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
//import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellpostenResourceTest extends AbstractResourceTest {

	private static final Integer ID_FOR_DELETE = Integer.valueOf(602);
	// ///////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final String JSON_KEY_BESTELLUNG = "bestellung";
	private static final String JSON_KEY_BESTELLUNG_ID = "bestellungID";
	private static final String JSON_KEY_PRODUKTDATEN = "produktdaten";
	private static final String JSON_KEY_PRODUKTDATEN_ID = "produktdatenID";
	private static final String JSON_KEY_ANZAHL = "anzahl";

	private static final String BESTELLUNG_ID_EXIST = "501";
	private static final String PRODUKTDATEN_ID_EXIST = "402";
	private static final int ANZAHL = 12;

	@Test
	public void testeAddBestellposten() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME;
		final String password = PASSWORD;
		JsonObject produktdatenJson = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_PRODUKTDATEN_ID, PRODUKTDATEN_ID_EXIST).build();
		JsonObject bestellungJson = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BESTELLUNG_ID, BESTELLUNG_ID_EXIST).build();

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder().add(JSON_KEY_BESTELLUNG, bestellungJson)
				.add(JSON_KEY_PRODUKTDATEN, produktdatenJson)
				.add(JSON_KEY_ANZAHL, ANZAHL).build();

		final Response response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic(username, password)
				.post(BESTELLPOSTEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

		LOGGER.finer("ENDE");

	}

	@Ignore
	@Test
	public void testeDeleteBestellposten() {
		LOGGER.finer("BEGINN");

		// When
		final Response response = given().pathParameter(
				BESTELLPOSTEN_ID_PATH_PARAM, ID_FOR_DELETE).delete(
				BESTELLPOSTEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

		LOGGER.finer("ENDE");
	}
}