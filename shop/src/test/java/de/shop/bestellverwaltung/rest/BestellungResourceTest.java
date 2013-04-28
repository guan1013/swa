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
	
	@Test
	public void findBestellungById() {
		Long bId = Long.valueOf(501);
		
		Response response = given().header("Accept", APPLICATION_JSON)
							.pathParameter("id", bId)
							.get("/bestellung/{id}");
		
		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));
		
		try(JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
				JsonObject jsonObject = jsonReader.readObject();
				assertThat(jsonObject.getJsonNumber("bestellungID").intValue(), is(bId.intValue()));
		}
	}
	
	@Test
	public void createBestellung() {
		final int kundeId = 100;
		final int artikelId1 = 401;
		final int artikelId2 = 402;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
				                      .add("kundeUri", KUNDEN_URI + "/" + kundeId)
				                      .add("bestellpositionen", getJsonBuilderFactory().createArrayBuilder()
				            		                            .add(getJsonBuilderFactory().createObjectBuilder()
				            		                                 .add("artikelUri", ARTIKEL_URI + "/" + artikelId1)
				            		                                 .add("anzahl", 1))
				            		                            .add(getJsonBuilderFactory().createObjectBuilder()
				            		                                 .add("artikelUri", ARTIKEL_URI + "/" + artikelId2)
				            		                                 .add("anzahl", 2)))
				                      .build();

		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
				                         .auth()
				                         .basic(username, password)
				                         .post(BESTELLUNGEN_PATH);
		
		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_CREATED));
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final int id = Integer.valueOf(idStr);
		assertThat(id > 0, is(true));
	}
	
	@Test
	public void findBestellpostenByBestellungId() {
		Long bId = Long.valueOf(501);
		
		Response response = given().header("Accept", APPLICATION_JSON)
							.pathParameter("bestellungFk", bId)
							.get("/bestellung/{id}/bestellposten");
		
		assertThat(response.getStatusCode(), is(HttpURLConnection.HTTP_OK));
		
		try(JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
				JsonArray jsonObject = jsonReader.readArray();
				assertThat(jsonObject.size(), is(2));
		}
	}
	
	@Test
	public void updateBestellung() {
		
	}
	
}
