package de.shop.bestellverwaltung.rest;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.jayway.restassured.response.Response;

import static de.shop.util.TestConstants.*;
import de.shop.util.AbstractResourceTest;
import static com.jayway.restassured.RestAssured.given;

import java.io.StringReader;
import java.net.HttpURLConnection;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import org.junit.FixMethodOrder;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellungResourceTest extends AbstractResourceTest {
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
	//@Ignore
	@Test
	public void findBestellungById() {
		int bId = 501;

		Response response = given().auth()
				.basic(USERNAME, PASSWORD).header(ACCEPT, APPLICATION_JSON)
				.pathParameter("id", bId).get("/bestellung/{id}");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));

		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("bestellungID").intValue(),
					is(bId));
		}
	}
	//@Ignore
	@Test
	public void createBestellung() {
		final int kundeId = 100;
		final int produktdaten1ID = 401;
		final int produktdaten2ID = 402;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + kundeId)
//				.add("bestellposten",
//						getJsonBuilderFactory()
//								.createArrayBuilder()
//								.add()d
//								.add(getJsonBuilderFactory()
//										.createObjectBuilder()//.add("bestellungID", 501)
//										.add("produktID", 407)
//										.add("anzahl", 8)))
				.build();

		final Response response = given().auth()
				.basic(username, password).contentType(APPLICATION_JSON)
				.body(jsonObject.toString())
				.post(BESTELLUNGEN_PATH);

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final int id = Integer.valueOf(idStr);
		assertThat(id > 0, is(true));
	}
	//@Ignore
	@Test
	public void findBestellpostenByBestellungId() {
		int bId = 501;

		Response response = given().auth()
				.basic(USERNAME, PASSWORD).header("Accept", APPLICATION_JSON)
				.pathParameter("bestellungFk", bId)
				.get("/bestellung/{bestellungFk}/bestellposten");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));

		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			JsonArray jsonObject = jsonReader.readArray();
			assertThat(jsonObject.size(), is(2));
		}
	}

	// @Test
	// public void updateBestellung() {
	//
	// }
	//@Ignore
	@Test
	public void deleteBestellung() {
		int bId = 502;

		Response response = given().auth()
				.basic(USERNAME_ADMIN, PASSWORD_ADMIN).pathParameter("id", bId).delete(
				"/bestellung/{id}");

		assertThat(response.getStatusCode(),
				is(HttpURLConnection.HTTP_NO_CONTENT));
	}
}
