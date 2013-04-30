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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ProduktResourceTest extends AbstractResourceTest {

	private static final String BASIC_PASSWORD = "abc";
	private static final String BASIC_USER = "guan1013";
	private static final String JSON_KEY_ID = "produktID";
	private static final String JSON_KEY_HERSTELLER = "hersteller";
	private static final String JSON_KEY_BESCHREIBUNG = "beschreibung";
	private static final String HERSTELLER_CREATE = "JUnit Hersteller Create";
	private static final String HERSTELLER_CREATE_INVALID = "";
	private static final String HERSTELLER_UPDATE = "JUnit Hersteller Update";
	private static final String BESCHREIBUNG_UPDATE = "JUnit Beschreibung Update";
	private static final String BESCHREIBUNG_CREATE = "JUnit Beschreibung Create";
	private static final String BESCHREIBUNG_CREATE_INVALID = "";
	private static final int NON_EXISTING_ID = 1818;
	private static final int EXISTING_ID = 303;
	private static final String PATH_PARAM_PRODUKT_ID = "produktId";
	private static final String PATH = "/produkte";
	private static final String PATH_WITH_PARAM_ID = PATH + "/{"
			+ PATH_PARAM_PRODUKT_ID + "}";
	private static final String ACCEPT = "Accept";

	/**
	 * GET Request
	 */
	@Test
	public void findProduktByProduktId() {

		// Given
		final Integer produktId = Integer.valueOf(EXISTING_ID);

		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKT_ID, produktId)
				.get(PATH_WITH_PARAM_ID);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {

			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber(JSON_KEY_ID).intValue(),
					is(produktId.intValue()));

		}
	}

	/**
	 * GET Request fehlerhaft (ID existiert nicht)
	 */
	@Test
	public void findNonExistingProduktById() {

		// Given
		final Integer produktId = Integer.valueOf(NON_EXISTING_ID);

		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKT_ID, produktId)
				.get(PATH_WITH_PARAM_ID);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));

	}

	/**
	 * GET Request für alle Produktdaten eines Produktes
	 */
	@Test
	@Ignore
	public void findAlleProduktdatenByProduktId() {

		// TODO: Test implementieren
	}

	/**
	 * GET Request für alle Produktdaten eines Produktes, fehlerhaft (ID exis.
	 * nicht)
	 */
	@Test
	@Ignore
	public void findAlleProduktdatenByNonExistingProduktId() {

		// TODO: Test implementieren

	}

	/**
	 * POST Request
	 */
	@Test
	public void createProdukt() {
		// Given

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BESCHREIBUNG, BESCHREIBUNG_CREATE)
				.add(JSON_KEY_HERSTELLER, HERSTELLER_CREATE).build();
		final Response response = given().auth()
				.basic(BASIC_USER, BASIC_PASSWORD)
				.contentType(APPLICATION_JSON).body(jsonObject.toString())
				.post(PATH);

		// Then TODO: GGf. Location überprüfen
		assertThat(response.statusCode(), is(HTTP_CREATED));
	}

	/**
	 * POST Request fehlerhaft (ungültige Beschreibung + Hersteller)
	 */
	@Test
	public void createProduktInvalid() {

		// Given

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BESCHREIBUNG, BESCHREIBUNG_CREATE_INVALID)
				.add(JSON_KEY_HERSTELLER, HERSTELLER_CREATE_INVALID).build();
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
	public void updateProdukt() {

		// Given

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKT_ID, EXISTING_ID)
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

			if (key.equals(JSON_KEY_BESCHREIBUNG)) {
				job.add(JSON_KEY_BESCHREIBUNG, BESCHREIBUNG_UPDATE);

			}
			else if (key.equals(JSON_KEY_HERSTELLER)) {
				job.add(JSON_KEY_HERSTELLER, HERSTELLER_UPDATE);

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
	 * PUT Request fehlerhaft (ID wird nicht übergeben)
	 */
	@Test
	public void updateProduktInvalid() {

		// Given

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(PATH_PARAM_PRODUKT_ID, EXISTING_ID)
				.get(PATH_WITH_PARAM_ID);
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		assertThat(jsonObject.getJsonNumber("produktID").intValue(),
				is(EXISTING_ID));

		final JsonObjectBuilder job = getJsonBuilderFactory()
				.createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String key : keys) {

			if (key.equals(JSON_KEY_BESCHREIBUNG)) {
				job.add(JSON_KEY_BESCHREIBUNG, BESCHREIBUNG_UPDATE);

			}
			else if (key.equals(JSON_KEY_HERSTELLER)) {
				job.add(JSON_KEY_HERSTELLER, HERSTELLER_UPDATE);
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
