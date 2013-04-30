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
	
	@Test
	public void dontfindBestellungById() {
		int bId = 65365754;

		Response response = given().auth()
				.basic(USERNAME, PASSWORD).header(ACCEPT, APPLICATION_JSON)
				.pathParameter("id", bId).get("/bestellung/{id}");

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
	}
	
	//@Ignore
	@Test
	public void createBestellung() {
		final int kundeId = 100;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + kundeId)
				.add("gesamtpreis", 123)
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
		final int kundeId = 900;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add("kundeUri", KUNDEN_URI + "/" + kundeId)
				.add("gesamtpreis", 123)
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

		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_NOT_FOUND));
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
		 final int bestellungId = 501;
			final double neuerPreis = 666.0;
			final String username = USERNAME_ADMIN;
			final String password = PASSWORD_ADMIN;
			
			// When
			Response response = given().auth()
					.basic(username, username).header(ACCEPT, APPLICATION_JSON)
					.pathParameter("id", bestellungId).get("/bestellung/{id}");
			
			JsonObject jsonObject;
			try (final JsonReader jsonReader =
					              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
				jsonObject = jsonReader.readObject();
			}
	    	assertThat(jsonObject.getJsonNumber("bestellungID").intValue(), is(bestellungId));
	    	
	    	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
	    	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
	    	final Set<String> keys = jsonObject.keySet();
	    	for (String k : keys) {
	    		if ("gesamtpreis".equals(k)) {
	    			job.add("gesamtpreis", neuerPreis);
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
