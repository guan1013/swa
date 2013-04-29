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
public class ProduktResourceConcurrencyTest extends AbstractResourceTest {

	@Test
	public void updateProdukt() throws InterruptedException, ExecutionException {

		int produktId = 303;

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter("produktId", produktId)
				.get("produkte/{produktId}");
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		assertThat(jsonObject.getJsonNumber("produktID").intValue(),
				is(produktId));

		// Konkurrierendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuer
		// Beschreibung bauen
		JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("hersteller".equals(k)) {
				job.add("hersteller", "ConcurrencyHersteller");
			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		final JsonObject jsonObject2 = job.build();
		final ConcurrentUpdate concurrentUpdate = new ConcurrentUpdate(
				jsonObject2, "/produkte", "guan1013", "abc");
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
			if ("hersteller".equals(k)) {
				job.add("hersteller", "Hersteller Concurrency 2");
			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();
		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic("guan1013", "abc")
				.put("/produkte");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
	}
}