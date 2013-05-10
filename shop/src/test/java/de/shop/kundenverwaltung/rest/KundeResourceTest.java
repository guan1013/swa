package de.shop.kundenverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.BASEPATH;
import static de.shop.util.TestConstants.BASEURI;
import static de.shop.util.TestConstants.KUNDEN_ID_FILE_PATH;
import static de.shop.util.TestConstants.KUNDEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.KUNDEN_ID_PATH;
import static de.shop.util.TestConstants.KUNDEN_PATH;
import static de.shop.util.TestConstants.LOCATION;
import static de.shop.util.TestConstants.PORT;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.xml.bind.DatatypeConverter;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;
import de.shop.util.exceptions.NoMimeTypeException;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class KundeResourceTest extends AbstractResourceTest {

	// ///////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final String JSON_KEY_NACHNAME = "nachname";
	private static final String JSON_KEY_VORNAME = "vorname";
	private static final String JSON_KEY_EMAIL = "email";
	private static final Integer ID_EXIST = Integer.valueOf(101);
	private static final String VORNAME_CORRECT = "Matthias";
	private static final String NACHNAME_CORRECT = "Schnell";
	private static final String EMAIL_CORRECT = "scma1078@hs-karlsruhe.de";

	private static final Integer ID_NOT_EXIST = Integer.valueOf(9999);
	private static final String JSON_KEY_ID = "kundeID";

	private static final String MAIL_UPDATE = "mia.mueller@gmail.com";
	private static final Integer ID_UPDATE_EXIST = Integer.valueOf(102);

	private static final Integer ID_DELETE_WITHOUT_BESTELLUNGEN_EXIST = Integer
			.valueOf(105);

	private static final Integer ID_DELETE_WITH_BESTELLUNGEN_EXIST = Integer
			.valueOf(103);

	private static final String JSON_KEY_BYTES = "bytes";

	private static final String PIC = "pic.jpg";
	private static final String PIC_UPLOAD = "src/test/resources/rest/" + PIC;
	private static final String PIC_DOWNLOAD = "target/" + PIC;
	private static final CopyOption[] COPY_OPTIONS = { REPLACE_EXISTING };

	private static final String PIC_INVALID_MIMETYPE = "pic.bmp";
	private static final String PIC_UPLOAD_INVALID_MIMETYPE = "src/test/resources/rest/"
			+ PIC_INVALID_MIMETYPE;

	@Test
	public void testeAddKunde() {

		LOGGER.finer("BEGINN");

		// When
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder().add(JSON_KEY_NACHNAME, NACHNAME_CORRECT)
				.add(JSON_KEY_VORNAME, VORNAME_CORRECT)
				.add(JSON_KEY_EMAIL, EMAIL_CORRECT).build();

		final Response response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).post(KUNDEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CREATED));

		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Integer id = Integer.valueOf(idStr);
		assertThat(id.intValue() > 0, is(true));

		LOGGER.finer("ENDE");

	}

	@Ignore
	@Test
	public void testeUploadKundePic() throws IOException {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME;
		final String password = PASSWORD;

		// Datei als byte[] einlesen
		byte[] bytes;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Files.copy(Paths.get(PIC_UPLOAD), stream);
			bytes = stream.toByteArray();
		}

		// byte[] als Inhalt eines JSON-Datensatzes mit Base64-Codierung
		JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BYTES, DatatypeConverter.printBase64Binary(bytes))
				.build();

		// When
		Response response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString())
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST).auth()
				.basic(username, password).post(KUNDEN_ID_FILE_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
		// id extrahieren aus http://localhost:8080/shop2/rest/kunden/<id>/file
		final String idStr = response
				.getHeader(LOCATION)
				.replace(BASEURI + ":" + PORT + BASEPATH + KUNDEN_PATH + '/',
						"").replace("/pic", "");
		assertThat(idStr, is(ID_EXIST.toString()));

		// When (2)
		// Download der zuvor hochgeladenen Datei
		response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST).auth()
				.basic(username, password).get(KUNDEN_ID_FILE_PATH);

		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		final String base64String = jsonObject.getString(JSON_KEY_BYTES);
		final byte[] downloaded = DatatypeConverter
				.parseBase64Binary(base64String);

		// Then (2)
		// Dateigroesse vergleichen: hochgeladene Datei als byte[] einlesen
		byte[] uploaded;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Files.copy(Paths.get(PIC_UPLOAD), stream);
			uploaded = stream.toByteArray();
		}
		assertThat(uploaded.length, is(downloaded.length));

		// Abspeichern der heruntergeladenen Datei im Unterverzeichnis target
		// zur manuellen Inspektion
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
				downloaded)) {
			Files.copy(inputStream, Paths.get(PIC_DOWNLOAD), COPY_OPTIONS);
		}

		LOGGER.info("Heruntergeladene Datei abgespeichert: " + PIC_DOWNLOAD);
		LOGGER.finer("ENDE");

	}

	@Test
	public void testeUploadInvalidKundePic() throws IOException {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME;
		final String password = PASSWORD;

		// Datei als byte[] einlesen
		byte[] bytes;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Files.copy(Paths.get(PIC_UPLOAD_INVALID_MIMETYPE), stream);
			bytes = stream.toByteArray();
		}

		// byte[] als Inhalt eines JSON-Datensatzes mit Base64-Codierung
		final JsonObject jsonObject = getJsonBuilderFactory()
				.createObjectBuilder()
				.add(JSON_KEY_BYTES, DatatypeConverter.printBase64Binary(bytes))
				.build();

		// When
		final Response response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString())
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST).auth()
				.basic(username, password).post(KUNDEN_ID_FILE_PATH);

		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
		assertThat(response.asString(), is(NoMimeTypeException.MESSAGE));
	}

	@Ignore
	@Test
	public void downloadKundePic() {

	}

	@Test
	public void testeFindExistingKundeById() {
		LOGGER.finer("BEGINN");
		// When
		final String username = USERNAME;
		final String password = PASSWORD;
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST).auth()
				.basic(username, password).get(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));

		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber(JSON_KEY_ID).intValue(),
					is(ID_EXIST.intValue()));
		}

		LOGGER.finer("ENDE");

	}

	@Test
	public void testeFindExistingKundeByIdUnAuth() {
		LOGGER.finer("BEGINN");
		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_EXIST)
				.get(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_UNAUTHORIZED));

		LOGGER.finer("ENDE");

	}

	@Test
	public void testeFindNonExistingKundeById() {
		LOGGER.finer("BEGINN");

		// When
		final String username = USERNAME;
		final String password = PASSWORD;
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_NOT_EXIST).auth()
				.basic(username, password).get(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
		LOGGER.finer("ENDE");

	}

	@Ignore
	@Test
	public void testeFindKundeByIdWithValidationError() {

	}

	@Ignore
	@Test
	public void testeFindExistingKundeByMailFetchBestellungen() {

	}

	@Test
	public void testeUpdateKunde() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME_MITARBEITER;
		final String password = PASSWORD;

		// When
		Response response = given()
				.header(ACCEPT, APPLICATION_JSON)
				.auth()
				.basic("scma1078", "abc")
				.pathParameter(KUNDEN_ID_PATH_PARAM, ID_UPDATE_EXIST.intValue())
				.get(KUNDEN_ID_PATH);

		JsonObject jsonObject;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(
				new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		assertThat(jsonObject.getJsonNumber(JSON_KEY_ID).intValue(),
				is(ID_UPDATE_EXIST.intValue()));

		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuer EMAIL
		// bauen
		final JsonObjectBuilder job = getJsonBuilderFactory()
				.createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if (JSON_KEY_EMAIL.equals(k)) {
				job.add(JSON_KEY_EMAIL, MAIL_UPDATE);
			} else {
				job.add(k, jsonObject.get(k));
			}
		}
		jsonObject = job.build();

		response = given().contentType(APPLICATION_JSON)
				.body(jsonObject.toString()).auth().basic(username, password)
				.put(KUNDEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
	}

	@Ignore
	@Test
	public void testeUpdateInvlidKunde() {

	}

	// TODO 500 Fehler bei Kunde ohne Bestellung, aber kein (bzw richtiger)
	// Fehler bei Kunde mit Bestellung. Wieso?
	@Ignore
	@Test
	public void testeDeleteKunde() {
		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;

		// When
		final Response response = given()
				.pathParameter(KUNDEN_ID_PATH_PARAM,
						ID_DELETE_WITHOUT_BESTELLUNGEN_EXIST).auth()
				.basic(username, password).delete(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));

		LOGGER.finer("ENDE");

	}

	@Test
	public void testeDeleteKundeWithBestellung() {

		LOGGER.finer("BEGINN");

		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;

		// When
		final Response response = given()
				.pathParameter(KUNDEN_ID_PATH_PARAM,
						ID_DELETE_WITH_BESTELLUNGEN_EXIST).auth()
				.basic(username, password).delete(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
		final String errorMsg = response.asString();
		assertThat(errorMsg, startsWith("Kunde mit ID="
				+ ID_DELETE_WITH_BESTELLUNGEN_EXIST
				+ " kann nicht geloescht werden:"));
		assertThat(errorMsg, endsWith("Bestellung(en)"));

		LOGGER.finer("ENDE");

	}

}
