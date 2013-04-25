package de.shop.produktverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;

import javax.json.JsonObject;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ProduktResourceTest extends AbstractResourceTest {

	@Test
	public void findProduktByProduktId() {
		
		// Given
		Integer produktId = Integer.valueOf(303);

		// When
		Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktId", produktId)
				.get("/produkte/{produktId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {

			JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("produktID").intValue(),
					is(produktId.intValue()));

		}
	}
	
	@Test
	public void findNonExistingProduktById() {

		// Given
		Integer produktId = Integer.valueOf(1818);

		// When
		Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktId", produktId)
				.get("/produkte/{produktId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
		
	}

}
