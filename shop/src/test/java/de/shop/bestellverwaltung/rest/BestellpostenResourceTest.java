package de.shop.bestellverwaltung.rest;

import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.BESTELLPOSTEN_PATH;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLPOSTEN_ID_PATH;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

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
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
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
	private static final String JSON_KEY_BESTELLPOSTEN_ID = "bestellpostenID";
	private static final String JSON_KEY_ANZAHL = "anzahl";

	private static final Integer BESTELLUNG_ID_EXIST = Integer.valueOf(501);
	private static final Integer BESTELLUNG_ID_INVALID = Integer.valueOf(999);
	private static final Integer ID_EXIST = Integer.valueOf(603);
	private static final Integer ID_UPDATE_EXIST = Integer.valueOf(602);
	private static final Integer PRODUKTDATEN_ID_EXIST = Integer.valueOf(402);
	private static final Integer PRODUKTDATEN_ID_INVALID = Integer.valueOf(999);
	private static final int ANZAHL = 12;
	private static final int ANZAHL_UPDATE = 88;
	private static final int ANZAHL_INVALID = -1;
	
	
	@Test
	public void testeAddBestellposten() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME_MITARBEITER;
		final String password = PASSWORD;
		final JsonObject produktdatenJson = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_PRODUKTDATEN_ID, PRODUKTDATEN_ID_EXIST).build();
		final JsonObject bestellungJson = getJsonBuilderFactory()
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
	
	@Test
	public void testeAddInvalidBestellposten() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME;
		final String password = PASSWORD;
		final JsonObject produktdatenJson = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_PRODUKTDATEN_ID, PRODUKTDATEN_ID_INVALID).build();
		final JsonObject bestellungJson = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BESTELLUNG_ID, BESTELLUNG_ID_INVALID).build();

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder().add(JSON_KEY_BESTELLUNG, bestellungJson)
				.add(JSON_KEY_PRODUKTDATEN, produktdatenJson)
				.add(JSON_KEY_ANZAHL, ANZAHL_INVALID).build();

		final Response response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic(username, password)
				.post(BESTELLPOSTEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));

		LOGGER.finer("ENDE");

	}
	@Ignore
	@Test
	public void testeUpdateBestellposten() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME_MITARBEITER;
		final String password = PASSWORD;

		// When
		Response response = given()
				.header(ACCEPT, APPLICATION_JSON)
				.auth()
				.basic(username, password)
				.pathParameter(BESTELLPOSTEN_ID_PATH_PARAM, ID_UPDATE_EXIST)
				.get(BESTELLPOSTEN_ID_PATH);

		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		assertThat(jsonObject.getJsonNumber(JSON_KEY_BESTELLPOSTEN_ID).intValue(),
				is(ID_UPDATE_EXIST.intValue()));

		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuer
		// Anzahl bauen
		final JsonObjectBuilder job = getJsonBuilderFactory()
				.createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if (JSON_KEY_ANZAHL.equals(k)) {
				job.add(JSON_KEY_ANZAHL, ANZAHL_UPDATE);
			} 
			else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();

		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic(username, password)
				.put(BESTELLPOSTEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
	}
	
	@Ignore
	@Test
	public void testeFindExistingBestellpostenById() {
		LOGGER.finer("BEGINN");
		// When
		final String username = USERNAME;
		final String password = PASSWORD;
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(BESTELLPOSTEN_ID_PATH_PARAM, ID_EXIST).auth()
				.basic(username, password).get(BESTELLPOSTEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));

		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("bestellpostenID").intValue(),
					is(ID_EXIST.intValue()));
		}

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
