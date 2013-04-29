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
		final String username = USERNAME;
		final String password = PASSWORD;

		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + kundeId)
//				.add("bestellposten",
//						getJsonBuilderFactory()
//								.createArrayBuilder()
//								.add(getJsonBuilderFactory()
//										.createObjectBuilder()
//										.add("anzahl", 1)
//										.add("produktdaten",
//												PRODUKTDATEN + "/"
//														+ produktdaten1ID))
//								.add(getJsonBuilderFactory()
//										.createObjectBuilder()
//										.add("anzahl", 2)
//										.add("produktdaten",
//												PRODUKTDATEN + "/"
//														+ produktdaten2ID)))
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
	@Ignore
	@Test
	public void deleteBestellung() {
		int bId = 502;

		Response response = given().pathParameter("id", bId).delete(
				"/bestellung/{id}");

		assertThat(response.getStatusCode(),
				is(HttpURLConnection.HTTP_NO_CONTENT));
	}
}
