package de.shop.produktverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;
import de.shop.util.ConcurrentUpdate;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProduktdatenResourceConcurrencyTest extends AbstractResourceTest {

	private static final int EXISTING_PRODUKTDATEN_ID = 404;

	@Test
	public void updateProduktdaten() throws InterruptedException,
			ExecutionException {

		final int produktId = EXISTING_PRODUKTDATEN_ID;

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter("produktdatenId", produktId)
				.get("produktdaten/{produktdatenId}");
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		assertThat(jsonObject.getJsonNumber("produktdatenID").intValue(),
				is(produktId));

		// Konkurrierendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuer
		// Beschreibung bauen
		JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("farbe".equals(k)) {
				job.add("farbe", "blau");
			}
			else {
				job.add(k, jsonObject.get(k));
			}
		}
		final JsonObject jsonObject2 = job.build();
		final ConcurrentUpdate concurrentUpdate = new ConcurrentUpdate(
				jsonObject2, "/produktdaten", "guan1013", "abc");
		final ExecutorService executorService = Executors
				.newSingleThreadExecutor();
		final Future<Response> future = executorService
				.submit(concurrentUpdate);
		response = future.get(); // Warten bis der "parallele" Thread fertig ist
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

		// Fehlschlagendes Update
		job = getJsonBuilderFactory().createObjectBuilder();
		keys = jsonObject.keySet();
		for (String k : keys) {
			if ("farbe".equals(k)) {
				job.add("farbe", "rot-weiﬂ");
			}
			else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();
		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic("guan1013", "abc")
				.put("/produktdaten");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
	}
}
