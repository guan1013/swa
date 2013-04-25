package de.shop.produktverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
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
public class ProduktdatenResourceTest extends AbstractResourceTest {

	@Test
	public void findProduktdatenById() {

		// Given
		Integer produktdatenId = Integer.valueOf(404);

		// When
		Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktdatenId", produktdatenId)
				.get("/produktdaten/{produktdatenId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		try (JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {

			JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("produktdatenID").intValue(),
					is(produktdatenId.intValue()));

		}
	}

	@Test
	public void findNonExistingProduktdatenById() {

		// Given
		Integer produktdatenId = Integer.valueOf(1717);

		// When
		Response response = given().header("Accept", APPLICATION_JSON)
				.pathParameter("produktdatenId", produktdatenId)
				.get("/produktdaten/{produktdatenId}");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
		
	}
}
