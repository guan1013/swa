package de.shop.produktverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ProduktdatenResourceTest extends AbstractResourceTest {

	private static final double UPDATE_PREIS = 9999.99;
	private static final double CREATE_PREIS = 99.99;
	private static final int CREATE_ANZAHL = 3;
	private static final int EXISTING_PRODUKT_ID = 301;
	private static final int NON_EXISTING_PRODUKTDATEN_ID = 1717;
	private static final int EXISTING_PRODUKTDATEN_ID = 404;
	private static final String PATH = "/produktdaten";
	private static final String PATH_PARAM_PRODUKTDATEN_ID = "produktdatenId";
	private static final String PATH_WITH_PARAM_ID = PATH + "/{"
			+ PATH_PARAM_PRODUKTDATEN_ID + "}";
	private static final String ACCEPT = "Accept";
	private static final String BASIC_PASSWORD = "abc";
	private static final String BASIC_USER = "guan1013";
	private static final int EXISTING_ID = 404;
	private static final String JSON_KEY_ID = "produktdatenID";
	private static final String JSON_KEY_FARBE = "farbe";
	private static final String JSON_KEY_GROESSE = "groesse";
	private static final String JSON_KEY_PREIS = "preis";

	/**
	 * GET Request
	 */
	@Test
	public void findProduktdatenById() {

		// Given
		final Integer produktdatenId = Integer
				.valueOf(EXISTING_PRODUKTDATEN_ID);

		// When
		final Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktdatenId", produktdatenId)
				.get("/produktdaten/{produktdatenId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {

			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("produktdatenID").intValue(),
					is(produktdatenId.intValue()));

		}
	}

	/**
	 * GET Request fehlerhaft (ID existiert nicht)
	 */
	@Test
	public void findNonExistingProduktdatenById() {

		// Given
		final Integer produktdatenId = Integer
				.valueOf(NON_EXISTING_PRODUKTDATEN_ID);

		// When
		final Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktdatenId", produktdatenId)
				.get("/produktdaten/{produktdatenId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));

	}

	/**
	 * POST Request
	 */
	@Test
	public void createProduktdaten() {

		// Given
		final JsonObject produktJson = getJsonBuilderFactory()
				.createObjectBuilder().add("produktID", EXISTING_PRODUKT_ID)
				.build();

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder().add("anzahlVerfuegbar", CREATE_ANZAHL)
				.add("groesse", "XL TEST").add("preis", CREATE_PREIS)
				.add("farbe", "TEST/rot/gold").add("produkt", produktJson)
				.build();
		final Response response = given().auth()
				.basic(BASIC_USER, BASIC_PASSWORD)
				.contentType(APPLICATION_JSON).body(jsonObject.toString())
				.post(PATH);

		// Then TODO: GGf. Location überprüfen
		assertThat(response.statusCode(), is(HTTP_CREATED));
	}

	/**
	 * POST Request fehlerhaft (Preis fehlt)
	 */
	@Test
	public void createProduktdatenInvalid() {

		// Given
		final JsonObject produktJson = getJsonBuilderFactory()
				.createObjectBuilder().add("produktID", EXISTING_PRODUKT_ID)
				.build();

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder().add("anzahlVerfuegbar", CREATE_ANZAHL)
				.add("groesse", "XL TEST").add("farbe", "TEST/rot/gold")
				.add("produkt", produktJson).build();
		final Response response = given().auth()
				.basic(BASIC_USER, BASIC_PASSWORD)
				.contentType(APPLICATION_JSON).body(jsonObject.toString())
				.post(PATH);

		assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
	}

	/**
	 * PUT Request
	 */
	@Test
	public void updateProduktdaten() {

		// When

		// Vorhandene Produktdaten abfragen
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKTDATEN_ID, EXISTING_ID)
				.get(PATH_WITH_PARAM_ID);
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		assertThat(jsonObject.getJsonNumber(JSON_KEY_ID).intValue(),
				is(EXISTING_ID));

		final JsonObjectBuilder job = getJsonBuilderFactory()
				.createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String key : keys) {

			if (key.equals(JSON_KEY_FARBE)) {
				job.add(JSON_KEY_FARBE, "NEUE FARBE");
			}
			else if (key.equals(JSON_KEY_GROESSE)) {
				job.add(JSON_KEY_GROESSE, "NEUE GROESSE");
			}
			else if (key.equals(JSON_KEY_PREIS)) {
				job.add(JSON_KEY_PREIS, UPDATE_PREIS);
			}
			else {
				job.add(key, jsonObject.get(key));
			}

		}

		jsonObject = job.build();

		response = given().auth().basic(BASIC_USER, BASIC_PASSWORD)
				.contentType(APPLICATION_JSON).body(jsonObject.toString())
				.put(PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

	}

	/**
	 * PUT Request fehlerhaft (Farbe/Preis/Groesse empty)
	 */
	@Test
	public void updateProduktdatenInvalid() {
		// When

		// Vorhandene Produktdaten abfragen
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKTDATEN_ID, EXISTING_ID)
				.get(PATH_WITH_PARAM_ID);
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		assertThat(jsonObject.getJsonNumber(JSON_KEY_ID).intValue(),
				is(EXISTING_ID));

		final JsonObjectBuilder job = getJsonBuilderFactory()
				.createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String key : keys) {

			if (key.equals(JSON_KEY_FARBE)) {
				job.add(JSON_KEY_FARBE, "");
			}
			else if (key.equals(JSON_KEY_GROESSE)) {
				job.add(JSON_KEY_GROESSE, "");
			}
			else if (key.equals(JSON_KEY_PREIS)) {
				job.add(JSON_KEY_PREIS, 0);
			}
			else {
				job.add(key, jsonObject.get(key));
			}

		}

		jsonObject = job.build();

		response = given().auth().basic(BASIC_USER, BASIC_PASSWORD)
				.contentType(APPLICATION_JSON).body(jsonObject.toString())
				.put(PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
	}
}
