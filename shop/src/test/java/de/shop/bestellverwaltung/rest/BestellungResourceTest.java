package de.shop.bestellverwaltung.rest;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.jayway.restassured.response.Response;

import static de.shop.util.TestConstants.*;
import de.shop.util.AbstractResourceTest;
import static com.jayway.restassured.RestAssured.given;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import org.junit.FixMethodOrder;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellungResourceTest extends AbstractResourceTest {
	
	private static final int BEISPIEL_ID = 501;
	private static final int EINS_ZWEI_DREI = 123;
	private static final int INVALID_ID = 65365754;
	private static final int VALID_KUNDE_ID = 100;
	private static final int INVALID_KUNDE_ID = 900;
	private static final int VALID_BESTELLUNG_ID = 501;
	private static final double PREIS = 666.0;
	private static final String USERNAME = USERNAME_ADMIN;
	private static final String PASSWORD = PASSWORD_ADMIN;
	//@Ignore
	@Test
	public void findBestellungById() {
		
		final Response response = given().auth()
				.basic(USERNAME, PASSWORD).header(ACCEPT, APPLICATION_JSON)
				.pathParameter("id", BEISPIEL_ID).get("/bestellung/{id}");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));

		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("bestellungID").intValue(),
					is(BEISPIEL_ID));
		}
	}
	
	@Test
	public void dontfindBestellungById() {
		Response response = given().auth()
				.basic(USERNAME, PASSWORD).header(ACCEPT, APPLICATION_JSON)
				.pathParameter("id", INVALID_ID).get("/bestellung/{id}");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
	}
	
	//@Ignore
	@Test
	public void createBestellung() {

		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + VALID_KUNDE_ID)
				.add("gesamtpreis", EINS_ZWEI_DREI)
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
	
	@Test
	public void dontcreateBestellung() {
		
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + INVALID_KUNDE_ID)
				.add("gesamtpreis", EINS_ZWEI_DREI)
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
				.basic(USERNAME, PASSWORD).contentType(APPLICATION_JSON)
				.body(jsonObject.toString())
				.post(BESTELLUNGEN_PATH);

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
	}
	
	
	//@Ignore
	@Test
	public void findBestellpostenByBestellungId() {

		final Response response = given().auth()
				.basic(USERNAME, PASSWORD).header("Accept", APPLICATION_JSON)
				.pathParameter("bestellungFk", VALID_BESTELLUNG_ID)
				.get("/bestellung/{bestellungFk}/bestellposten");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));

		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			final JsonArray jsonObject = jsonReader.readArray();
			assertThat(jsonObject.size(), is(2));
		}
	}
	
//	@Test
//	public void dontfindBestellpostenByBestellungId() {
//		int bId = 201;
//
//		Response response = given().auth()
//				.basic(USERNAME, PASSWORD).header("Accept", APPLICATION_JSON)
//				.pathParameter("bestellungFk", bId)
//				.get("/bestellung/{bestellungFk}/bestellposten");
//
//		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
//	}

	 @Test
	 public void updateBestellung() {
			final String username = USERNAME_ADMIN;
			final String password = PASSWORD_ADMIN;
			
			// When
			Response response = given().auth()
					.basic(username, username).header(ACCEPT, APPLICATION_JSON)
					.pathParameter("id", VALID_BESTELLUNG_ID).get("/bestellung/{id}");
			
			JsonObject jsonObject;
			try (final JsonReader jsonReader =
					              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
				jsonObject = jsonReader.readObject();
			}
	    	assertThat(jsonObject.getJsonNumber("bestellungID").intValue(), is(VALID_BESTELLUNG_ID));
	    	
	    	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
	    	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
	    	final Set<String> keys = jsonObject.keySet();
	    	for (String k : keys) {
	    		if ("gesamtpreis".equals(k)) {
	    			job.add("gesamtpreis", PREIS);
	    		}
	    		else {
	    			job.add(k, jsonObject.get(k));
	    		}
	    	}
	    	jsonObject = job.build();
	    	
			response = given().auth()
	                          .basic(username, password).contentType(APPLICATION_JSON)
					          .body(jsonObject.toString())
	                          .put(BESTELLUNGEN_PATH);
			
			// Then
			assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NO_CONTENT));
	 }
	 
	
	 
//	//@Ignore
//	@Test
//	public void deleteBestellung() {
//		int bId = 502;
//
//		Response response = given().auth()
//				.basic(USERNAME_ADMIN, PASSWORD_ADMIN).pathParameter("id", bId).delete(
//				"/bestellung/{id}");
//
//		assertThat(response.getStatusCode(),
//				is(HttpURLConnection.HTTP_NO_CONTENT));
//	}
}
