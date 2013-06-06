package de.shop.kundenverwaltung.rest;

import static de.shop.util.TestConstants.KUNDEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.KUNDEN_ID_PATH;
import static de.shop.util.TestConstants.KUNDEN_PATH;
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

	// ///////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Integer ID_EXIST = Integer.valueOf(101);
	private static final String JSON_KEY_NACHNAME = "nachname";
	private static final String JSON_KEY_VERSION = "version";
	private static final String CONCURRENT_NACHNAME_1 = "Concurrencyeins";
	private static final String CONCURRENT_NACHNAME_2 = CONCURRENT_NACHNAME_1
			+ "2";

	@Test
	public void updateKunde() throws InterruptedException, ExecutionException {

		final String username = USERNAME_MITARBEITER;
		final String password = PASSWORD;

		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON).auth()
				.basic(username, password)
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST)
				.get(KUNDEN_ID_PATH);
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
			if (JSON_KEY_NACHNAME.equals(k)) {
				job.add(JSON_KEY_NACHNAME, CONCURRENT_NACHNAME_1);
			} else if (JSON_KEY_VERSION.equals(k)) {
			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		final JsonObject jsonObject2 = job.build();
		final ConcurrentUpdate concurrentUpdate = new ConcurrentUpdate(
				jsonObject2, KUNDEN_PATH, USERNAME_MITARBEITER, PASSWORD);
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
			if (JSON_KEY_NACHNAME.equals(k)) {
				job.add(JSON_KEY_NACHNAME, CONCURRENT_NACHNAME_2);
			} else if (JSON_KEY_VERSION.equals(k)) {

			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();
		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth()
				.basic(USERNAME_MITARBEITER, PASSWORD).put(KUNDEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
	}
}
