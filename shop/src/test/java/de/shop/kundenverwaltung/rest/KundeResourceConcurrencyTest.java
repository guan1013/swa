package de.shop.kundenverwaltung.rest;

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
public class KundeResourceConcurrencyTest extends AbstractResourceTest {

	@Test
	public void updateKunde() throws InterruptedException, ExecutionException {

		int kundeId = 101;

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON).auth()
				.basic("scma1078", "abc").pathParameter("kid", kundeId)
				.get("/kunden/{kid}");
		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}

		// Konkurrierendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem
		// Nachnamen bauen
		JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("nachname".equals(k)) {
				job.add("nachname", "Concurrencyeins");
			} else if ("version".equals(k)) {
			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		final JsonObject jsonObject2 = job.build();
		final ConcurrentUpdate concurrentUpdate = new ConcurrentUpdate(
				jsonObject2, "kunden/", "scma1078", "abc");
		final ExecutorService executorService = Executors
				.newSingleThreadExecutor();
		final Future<Response> future = executorService
				.submit(concurrentUpdate);
		response = future.get(); // Warten bis der "parallele" Thread fertig ist
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

		// Fehlschlagendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem
		// Nachnamen bauen
		job = getJsonBuilderFactory().createObjectBuilder();
		keys = jsonObject.keySet();
		for (String k : keys) {
			if ("nachname".equals(k)) {
				job.add("nachname", "Concurrencyzwei");
			} else if ("version".equals(k)) {

			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();
		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic("scma1078", "abc")
				.put("kunden/");

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
	}
}