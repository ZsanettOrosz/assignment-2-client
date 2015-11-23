import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.jayway.jsonpath.JsonPath;

public class ClientSide {
	private WebTarget service;
	private ClientConfig clientConfig;
	private Client client;
	private XPathClient x;

	private int first_person_id;
	private int last_person_id;

	private int first_person_id_json;
	private int last_person_id_json;

	List<String> measure_types;

	String measure_id = "";
	String measure_type = "";

	String postedPersonId;
	int postedPersonIdJson;

	PrintWriter writerXml;
	PrintWriter writerJson;

	public ClientSide() {
		clientConfig = new ClientConfig();
		client = ClientBuilder.newClient(clientConfig);
		service = client.target(getBaseURI());
		x = new XPathClient();
	}

	public String prettyJson(String input) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		Object json = mapper.readValue(input, Object.class);
		String indented = mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(json);
		return indented;
	}

	public void json3_1() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#1 GET /person Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";
		Response response = service.path("person").request()
				.accept(MediaType.APPLICATION_JSON).get();

		String result = response.readEntity(String.class);

		first_person_id_json = JsonPath.read(result, "$.[0].idPerson");
		last_person_id_json = JsonPath
				.read(result, "$.[(@.length-1)].idPerson");

		JSONArray array = new JSONArray(result);

		String resStr = "";
		if (array.length() >= 2 && response.getStatus() == 200) {
			resStr = "OK";
		} else if (array.length() < 3) {
			resStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.1 ********");
		System.out.println(messageHeader(req, response.getStatus(), resStr));
		System.out.println(prettyJson(result));

		this.writerJson.write("\n\t\t******** 3.1 ********");
		this.writerJson.write(messageHeader(req, response.getStatus(), resStr));
		this.writerJson.write(prettyJson(result));

	}

	public void json3_2() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#2 GET /person/" + first_person_id_json
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response response = service.path("person/" + first_person_id_json)
				.request().accept(MediaType.APPLICATION_JSON).get();

		String result = response.readEntity(String.class);

		int resultCode = response.getStatus();
		String resStr = "";
		if (resultCode == 200 || resultCode == 202) {
			resStr = "OK";
		} else {
			resStr = "ERROR";
		}
		System.out.println("\n\t\t******** 3.2 ********");
		System.out.println(messageHeader(req, resultCode, resStr));
		System.out.println(prettyJson(result));

		this.writerJson.write("\n\t\t******** 3.2 ********");
		this.writerJson.write(messageHeader(req, resultCode, resStr));
		this.writerJson.write(prettyJson(result));

	}

	public void json3_3() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#3 PUT /person/" + first_person_id_json
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response responseGet = service.path("person/" + first_person_id_json)
				.request().accept(MediaType.APPLICATION_JSON).get();
		String resultGet = responseGet.readEntity(String.class);

		String oldName = JsonPath.read(resultGet, "$.firstname");

		String jsonRequest = "{" + "\"firstname\" : \"Chuck" + "_JSON" + "\","
				+ "\"lastname\" : \"Norris\","
				+ "\"birthdate\" : \"1945-01-01\"," + "\"lifeStatus\": []}";

		Response responsePut = service.path("person/" + first_person_id_json)
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.json(jsonRequest));

		int resultPut = responsePut.getStatus();

		Response responseGetUpdated = service
				.path("person/" + first_person_id_json).request()
				.accept(MediaType.APPLICATION_JSON).get();
		String resultGetUpdated = responseGetUpdated.readEntity(String.class);

		String newName = JsonPath.read(resultGetUpdated, "$.firstname");

		String resStr = "";
		if (!oldName.equals(newName) && resultPut == 201) {
			resStr = "OK";
		} else {
			resStr = "ERROR";
		}
		System.out.println("\n\t\t******** 3.3 ********");
		System.out.println(messageHeader(req, resultPut, resStr));
		System.out.println(prettyJson(resultGet));
		System.out.println(prettyJson(resultGetUpdated));

		this.writerJson.write("\n\t\t******** 3.3 ********");
		this.writerJson.write(messageHeader(req, resultPut, resStr));
		this.writerJson.write(prettyJson(resultGet));
		this.writerJson.write(prettyJson(resultGetUpdated));
	}

	public void json3_4() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#4 POST /person Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		String toPostJson = "{" + "\"firstname\" : \"Chuck\","
				+ "\"lastname\" : \"Norris\","
				+ "\"birthdate\" : \"1945-01-01\"," + "\"lifeStatus\": [{"
				+ "\"value\": \"78.9\"," + "\"measureType\" : {"
				+ "\"idMeasureDef\" : \"1\"," + "\"name\" : \"weight\"}}"
				+ ",{" + "\"value\" : \"172\"," + "\"measureType\" : {"
				+ "\"idMeasureDef\" : \"2\"," + "\"name\" : \"height\"}}]}";

		Response response = service.path("person").request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(toPostJson));

		int resultPost = response.getStatus();
		String resultStr = response.readEntity(String.class);

		postedPersonIdJson = JsonPath.read(resultStr, "$.idPerson");

		String resStr = "";
		if (resultPost == 200 || resultPost == 201 || resultPost == 202) {
			resStr = "OK";
		} else {
			resStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.4 ********");
		System.out.println(messageHeader(req, resultPost, resStr));
		System.out.println(prettyJson(resultStr));

		this.writerJson.write("\n\t\t******** 3.4 ********");
		this.writerJson.write(messageHeader(req, resultPost, resStr));
		this.writerJson.write(prettyJson(resultStr));
	}

	public void json3_5() {
		String reqDel = "#5 DELETE /person/" + postedPersonIdJson
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response response = service.path("person/" + postedPersonIdJson)
				.request().accept(MediaType.APPLICATION_JSON).delete();

		int resultCodeDelete = response.getStatus();

		String reqGet = "#2 GET /person/" + postedPersonIdJson
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response responseGet = service.path("person/" + postedPersonIdJson)
				.request().accept(MediaType.APPLICATION_JSON).get();

		int resultCodeGet = responseGet.getStatus();

		String resStr = "";
		if (resultCodeGet == 404 && resultCodeDelete == 204) {
			resStr = "OK";
		} else {
			resStr = "ERROR";
		}
		System.out.println("\n\t\t******** 3.5 ********");
		System.out.println(messageHeader(reqDel, resultCodeDelete, resStr));
		System.out.println(messageHeader(reqGet, resultCodeGet, resStr));

		this.writerJson.write("\n\t\t******** 3.5 ********");
		this.writerJson.write(messageHeader(reqDel, resultCodeDelete, resStr));
		this.writerJson.write(messageHeader(reqGet, resultCodeGet, resStr));
	}

	public void json3_6() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#9 GET /measureTypes Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response response = service.path("measureTypes").request()
				.accept(MediaType.APPLICATION_JSON).get();

		String result = response.readEntity(String.class);
		JSONObject array = new JSONObject(result); // will have the string array
													// from xml

		String resStr = "";
		if (array.length() >= 2 && response.getStatus() == 200) {

			resStr = "OK";
		} else if (array.length() < 3) {
			resStr = "ERROR";
		}
		System.out.println("\n\t\t******** 3.6 ********");
		System.out.println(messageHeader(req, response.getStatus(), resStr));
		System.out.println(prettyJson(result));

		this.writerJson.write("\n\t\t******** 3.6 ********");
		this.writerJson.write(messageHeader(req, response.getStatus(), resStr));
		this.writerJson.write(prettyJson(result));
	}

	public void json3_7() throws JsonParseException, JsonMappingException,
			IOException {

		boolean hasHistory = false;
		for (int i = 0; i < measure_types.size(); i++) {
			Response responseFirst = service
					.path("person/" + first_person_id_json + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_JSON).get();
			String resultFirst = responseFirst.readEntity(String.class);
			int responseCode = responseFirst.getStatus();
			if (responseCode == 200) {
				JSONArray array = new JSONArray(resultFirst);
				if (array.length() > 0)
					hasHistory = true;
			}
			Response responseLast = service
					.path("person/" + last_person_id_json + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_JSON).get();
			responseCode = responseLast.getStatus();
			if (responseCode == 200) {
				String resultLast = responseLast.readEntity(String.class);
				JSONArray arrayL = new JSONArray(resultLast);
				if (arrayL.length() > 0)
					hasHistory = true;
			}
		}

		System.out.println("\n\t\t******** 3.7 ********");
		this.writerJson.write("\n\t\t******** 3.7 ********");

		for (int i = 0; i < measure_types.size(); i++) {
			String req = "#6 GET /person/"
					+ first_person_id_json
					+ "/"
					+ measure_types.get(i)
					+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

			Response response = service
					.path("person/" + first_person_id_json + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_JSON).get();

			String result = response.readEntity(String.class);
			if (hasHistory == true && response.getStatus() == 200) {
				System.out.println(messageHeader(req, response.getStatus(),
						"OK"));
				System.out.println(prettyJson(result));

				this.writerJson.write(messageHeader(req, response.getStatus(),
						"OK"));
				this.writerJson.write(prettyJson(result));
			} else {
				System.out.println(messageHeader(req, response.getStatus(),
						"NO CONTENT"));
				this.writerJson.write(messageHeader(req, response.getStatus(),
						"NO CONTENT"));
			}
		}

		for (int i = 0; i < measure_types.size(); i++) {
			String req = "#6 GET /person/"
					+ last_person_id_json
					+ "/"
					+ measure_types.get(i)
					+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

			Response response = service
					.path("person/" + last_person_id_json + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_JSON).get();
			String result = response.readEntity(String.class);
			if (hasHistory == true && response.getStatus() == 200) {
				System.out.println(messageHeader(req, response.getStatus(),
						"OK"));
				System.out.println(prettyJson(result));
				this.writerJson.write(messageHeader(req, response.getStatus(),
						"OK"));
				this.writerJson.write(prettyJson(result));
			} else {
				System.out.println(messageHeader(req, response.getStatus(),
						"NO CONTENT"));
				this.writerJson.write(messageHeader(req, response.getStatus(),
						"NO CONTENT"));
			}

		}
		// measure id and measure type stored from xml

	}

	public void json3_8() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#7 GET /person/" + first_person_id_json + "/"
				+ measure_type + "/" + measure_id
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response response = service
				.path("person/" + first_person_id_json + "/" + measure_type
						+ "/" + measure_id).request()
				.accept(MediaType.APPLICATION_JSON).get();
		int resultCodeGet = response.getStatus();
		String result = response.readEntity(String.class);

		String resStr = "";
		if (resultCodeGet == 200) {
			resStr = "OK";
		} else {
			resStr = "ERROR";

		}
		System.out.println("\n\t\t******** 3.8 ********");
		System.out.println(messageHeader(req, resultCodeGet, resStr));
		System.out.println(prettyJson(result));

		this.writerJson.write("\n\t\t******** 3.8 ********");
		this.writerJson.write(messageHeader(req, resultCodeGet, resStr));
		this.writerJson.write(prettyJson(result));
	}

	public void json3_9() throws JsonParseException, JsonMappingException,
			IOException {

		String reqGetFirst = "#6 GET /person/" + first_person_id_json + "/"
				+ measure_type
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response responseGetFirst = service
				.path("person/" + first_person_id_json + "/" + measure_type)
				.request().accept(MediaType.APPLICATION_JSON).get();

		String resultGetFirst = responseGetFirst.readEntity(String.class);
		int numberOfHistories = 0;
		if (responseGetFirst.getStatus() == 200) {
			JSONArray array = new JSONArray(resultGetFirst);
			numberOfHistories = array.length();
		}

		String reqPost = "#8 POST /person/" + first_person_id_json + "/"
				+ measure_type
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		String historyToPostJson = "{" + "\"created\" : \"2011-12-09\","
				+ "\"value\" : \"72\"" + "}";
		Response responsePost = service
				.path("person/" + first_person_id + "/" + measure_type)
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.json(historyToPostJson));

		int resultCodePost = responsePost.getStatus();
		String resultStrPost = responsePost.readEntity(String.class);

		String reqGetSec = "#6 GET /person/" + first_person_id_json + "/"
				+ measure_type
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response responseGetSec = service
				.path("person/" + first_person_id_json + "/" + measure_type)
				.request().accept(MediaType.APPLICATION_JSON).get();

		String resultGetSec = responseGetSec.readEntity(String.class);
		int numberOfHistoriesSec = 0;
		if (responseGetSec.getStatus() == 200) {
			JSONArray array = new JSONArray(resultGetFirst);
			numberOfHistoriesSec = array.length();
		}

		String resStr = "";
		if (numberOfHistoriesSec > numberOfHistories && resultCodePost == 200) {
			resStr = "OK";
		} else {
			resStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.9 ********");
		this.writerJson.write("\n\t\t******** 3.9 ********");

		System.out.println(messageHeader(reqGetFirst,
				responseGetFirst.getStatus(), "OK"));
		System.out.println(prettyJson(resultGetFirst));

		this.writerJson.write(messageHeader(reqGetFirst,
				responseGetFirst.getStatus(), "OK"));
		this.writerJson.write(prettyJson(resultGetFirst));

		System.out.println(messageHeader(reqPost, resultCodePost, resStr));
		System.out.println(prettyJson(resultStrPost));

		this.writerJson.write(messageHeader(reqPost, resultCodePost, resStr));
		this.writerJson.write(prettyJson(resultStrPost));

		System.out.println(messageHeader(reqGetSec, responseGetSec.getStatus(),
				"OK"));
		System.out.println(prettyJson(resultGetSec));

		this.writerJson.write(messageHeader(reqGetSec,
				responseGetSec.getStatus(), "OK"));
		this.writerJson.write(prettyJson(resultGetSec));

	}

	public void json3_10() throws JsonParseException, JsonMappingException,
			IOException {
		String req = "#10 PUT /person/" + first_person_id_json + "/"
				+ measure_type + "/" + measure_id
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		String toUpdate = "80";
		String historyToPutJson = "{" + "\"created\" : \"2011-12-09\","
				+ "\"value\" : \"" + toUpdate + "\"" + "}";
		Response responsePut = service
				.path("person/" + first_person_id_json + "/" + measure_type
						+ "/" + measure_id).request(MediaType.APPLICATION_JSON)
				.put(Entity.json(historyToPutJson));

		int resultPut = responsePut.getStatus();
		System.out.println("Put result: " + resultPut);

		String reqGet = "#6 GET /person/" + first_person_id_json + "/"
				+ measure_type
				+ " Accept: APPLICATION/JSON Content-Type: APPLICATION/JSON";

		Response responseGet = service
				.path("person/" + first_person_id_json + "/" + measure_type
						+ "/" + measure_id).request()
				.accept(MediaType.APPLICATION_JSON).get();

		String resultGet = responseGet.readEntity(String.class);

		String newValue = JsonPath.read(resultGet, "$.[0].value");

		String resStr = "";
		if (newValue.equals(toUpdate) && resultPut == 201) {
			resStr = "OK";
		} else {

			resStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.10 ********");
		System.out.println(messageHeader(req, resultPut, resStr));
		System.out.println(prettyJson(resultGet));

		this.writerJson.write("\n\t\t******** 3.10 ********");
		this.writerJson.write(messageHeader(req, resultPut, resStr));
		this.writerJson.write(prettyJson(resultGet));
		// this.writerJson.write(prettyJson(resultStr));

	}

	public void task_3_1() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		String req = "#1 GET /person Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response response = service.path("person").request()
				.accept(MediaType.APPLICATION_XML).get();

		String result = response.readEntity(String.class);

		x.loadXML(result);

		NodeList peopleIDs = x.getPersonIDs(result);

		Node first = peopleIDs.item(0);
		Node last = peopleIDs.item(peopleIDs.getLength() - 1);

		first_person_id = Integer.parseInt(first.getTextContent());
		last_person_id = Integer.parseInt(last.getTextContent());

		// System.out.println("First person ID: " + first_person_id);
		// System.out.println("Last person ID: " + last_person_id);

		// System.out.println("Number of people: " + peopleIDs.getLength());

		String resultStr = " ";
		if (peopleIDs.getLength() >= 2) {
			resultStr = "OK";
		} else if (peopleIDs.getLength() < 3) {
			resultStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.1 ********");
		System.out.println(messageHeader(req, response.getStatus(), resultStr));
		System.out.println(prettyFormat(result));

		this.writerXml.write("\n\t\t******** 3.1 ********");
		this.writerXml
				.write(messageHeader(req, response.getStatus(), resultStr));
		this.writerXml.write(prettyFormat(result));

	}

	public void task_3_2() {
		String req = "#2 GET /person/" + first_person_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response response = service.path("person/" + first_person_id).request()
				.accept(MediaType.APPLICATION_XML).get();

		String result = response.readEntity(String.class);

		int resultCode = response.getStatus();

		String resultString = "";
		if (resultCode == 200 || resultCode == 201) {
			resultString = "OK";
		} else {
			resultString = "ERROR";
		}

		System.out.println("\n\t\t******** 3.2 ********");
		System.out.println(messageHeader(req, resultCode, resultString));
		System.out.println(prettyFormat(result));

		this.writerXml.write("\n\t\t******** 3.2 ********");
		this.writerXml.write(messageHeader(req, resultCode, resultString));
		this.writerXml.write(prettyFormat(result));
	}

	public void task_3_3() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		String reqGet = "#2 GET /person/" + first_person_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response responseGet = service.path("person/" + first_person_id)
				.request().accept(MediaType.APPLICATION_XML).get();

		String resultGetStr = responseGet.readEntity(String.class);

		x.loadXML(resultGetStr);
		Node node = x.getNodeResult("person/firstname");
		String oldFirstname = node.getTextContent();
		System.out.println("Old first name: " + oldFirstname);

		String reqPut = "#3 PUT /person/" + first_person_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		String xmlRequest = "<person><firstname>" + oldFirstname
				+ "_XML</firstname></person>";

		Response responsePut = service.path("person/" + first_person_id)
				.request(MediaType.APPLICATION_XML).put(Entity.xml(xmlRequest));

		int resultPut = responsePut.getStatus();
		System.out.println("Put result: " + resultPut);

		Response responseGetUpdated = service.path("person/" + first_person_id)
				.request().accept(MediaType.APPLICATION_XML).get();

		String resultNew = responseGetUpdated.readEntity(String.class);

		x.loadXML(resultNew);
		Node nodeNew = x.getNodeResult("person/firstname");
		String newFirstname = nodeNew.getTextContent();
		System.out.println("New first name: " + newFirstname);

		String resultOfRequest;
		if (!oldFirstname.equals(newFirstname) && resultPut == 201) {
			resultOfRequest = "OK";
		} else {
			resultOfRequest = "ERROR";
		}

		System.out.println("\n\t\t******** 3.3 ********");
		System.out
				.println(messageHeader(reqGet, responseGet.getStatus(), "OK"));
		System.out.println(prettyFormat(resultGetStr));

		System.out.println(messageHeader(reqPut, resultPut, resultOfRequest));
		System.out.println(prettyFormat(resultNew));

		this.writerXml.write("\n\t\t******** 3.3 ********");
		this.writerXml.write(messageHeader(reqGet, responseGet.getStatus(),
				"OK"));
		this.writerXml.write(prettyFormat(resultGetStr));

		this.writerXml.write(messageHeader(reqPut, resultPut, resultOfRequest));
		this.writerXml.write(prettyFormat(resultNew));

	}

	public void task_3_4() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		String req = "#4 POST /person Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		String toPost = "<person>" + "<firstname>whatnow</firstname>"
				+ "<lastname>Chan</lastname>"
				+ "<birthdate>1978-09-01T23:00:00+01:00</birthdate>"
				+ "<email>chuck.norris@gmail.com</email>"
				+ "<username>chuck.norris</username>" + "<healthProfile>"
				+ "<lifeStatus>" + "<measureType>"
				+ "<idMeasureDef>1</idMeasureDef>" + "<name>weight</name>"
				+ "</measureType>" + "<value>72.3</value>" + "</lifeStatus>"
				+ "<lifeStatus>" + "<measureType>"
				+ "<idMeasureDef>1</idMeasureDef>" + "<name>weight</name>"
				+ "</measureType>" + "<value>86</value>" + "</lifeStatus>"
				+ "</healthProfile>" + "</person>";

		Response response = service.path("person").request()
				.accept(MediaType.APPLICATION_XML).post(Entity.xml(toPost));

		int resultPost = response.getStatus();
		String resultStr = response.readEntity(String.class);
		// System.out.println("Post result: " + resultPost);

		x.loadXML(resultStr);
		Node node = x.getNodeResult("person/idPerson");
		postedPersonId = "";
		postedPersonId = node.getTextContent();
		System.out.println("ID of the new person: " + postedPersonId);

		String resultString = "";
		if ((resultPost == 200 || resultPost == 201 || resultPost == 202)
				&& postedPersonId != "") {
			resultString = "OK";
		} else {
			resultString = "ERROR";
		}
		System.out.println("\n\t\t******** 3.4 ********");
		System.out.println(messageHeader(req, resultPost, resultString));
		System.out.println(prettyFormat(resultStr));

		this.writerXml.write("\n\t\t******** 3.4 ********");
		this.writerXml.write(messageHeader(req, resultPost, resultString));
		this.writerXml.write(prettyFormat(resultStr));

	}

	public void task_3_5() {
		String reqDel = "#5 DELETE /person/" + postedPersonId
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response response = service.path("person/" + postedPersonId).request()
				.accept(MediaType.APPLICATION_XML).delete();

		int resultCodeDelete = response.getStatus();
		System.out.println("Result delete: " + resultCodeDelete);

		String reqGet = "#2 GET /person/" + first_person_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response responseGet = service.path("person/" + postedPersonId)
				.request().accept(MediaType.APPLICATION_XML).get();

		int resultCodeGet = responseGet.getStatus();
		System.out.println("Result get: " + resultCodeGet);

		String resultStr = "";
		if (resultCodeGet == 404) {
			resultStr = "OK";
		} else {
			resultStr = "OK";
		}
		System.out.println("\n\t\t******** 3.5 ********");
		System.out.println(messageHeader(reqDel, resultCodeDelete, resultStr));
		System.out.println(messageHeader(reqGet, resultCodeGet, resultStr));

		this.writerXml.write("\n\t\t******** 3.5 ********");
		this.writerXml
				.write(messageHeader(reqDel, resultCodeDelete, resultStr));
		this.writerXml.write(messageHeader(reqGet, resultCodeGet, resultStr));
		// this.writerXml.write(prettyFormat(resultNew));

	}

	public void task_3_6() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		String req = "#9 GET /measureTypes Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response response = service.path("measureTypes").request()
				.accept(MediaType.APPLICATION_XML).get();

		String result = response.readEntity(String.class);

		x.loadXML(result);

		NodeList measureTypes = x.getNodeListResult("measureTypes/measureType");
		measure_types = new ArrayList<>();
		for (int i = 0; i < measureTypes.getLength(); i++) {
			measure_types.add(measureTypes.item(i).getTextContent());
		}
		String resultStr = " ";
		if (measureTypes.getLength() >= 2) {
			resultStr = "OK";
		} else if (measureTypes.getLength() < 3) {
			resultStr = "ERROR";
		}

		System.out.println("\n\t\t******** 3.6 ********");
		System.out.println(messageHeader(req, response.getStatus(), resultStr));
		System.out.println(prettyFormat(result));

		this.writerXml.write("\n\t\t******** 3.6 ********");
		this.writerXml
				.write(messageHeader(req, response.getStatus(), resultStr));
		this.writerXml.write(prettyFormat(result));
	}

	public void task_3_7() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		int count = 0;
		for (int i = 0; i < measure_types.size(); i++) {
			Response response = service
					.path("person/" + first_person_id + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_XML).get();

			String result = response.readEntity(String.class);

			if (response.getStatus() == 200) {
				x.loadXML(result);
				System.out.println(result);
				NodeList nodes = x
						.getNodeListResult("healthMeasureHistories/measure/mid");
				if (nodes.getLength() != 0) {

					measure_id = nodes.item(0).getTextContent();
					measure_type = measure_types.get(i);
					count++;
				}
			}
		}

		for (int i = 0; i < measure_types.size(); i++) {
			Response response = service
					.path("person/" + last_person_id + "/"
							+ measure_types.get(i)).request()
					.accept(MediaType.APPLICATION_XML).get();

			String result = response.readEntity(String.class);
			if (response.getStatus() == 200) {
				x.loadXML(result);
				NodeList nodes = x
						.getNodeListResult("healthMeasureHistories/measure/mid");
				if (nodes.getLength() != 0) {
					
					measure_id = nodes.item(0).getTextContent();
					//System.out.println(measure_id);
					measure_type = measure_types.get(i);
					count++;
				}
			}
		}

		System.out.println("\n\t\t******** 3.7 ********");
		this.writerXml.write("\n\t\t******** 3.7 ********");

		String req = "#6 GET /person/{id}/{measureType} Accept: APPLICATION/XML Content-Type: APPLICATION/XML";
		if (count == 0) {
			messageHeader(req, 404, "ERROR");
		} else {

			for (int i = 0; i < measure_types.size(); i++) {
				String requ = "#6 GET /person/"
						+ first_person_id
						+ "/"
						+ measure_types.get(i)
						+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

				Response response = service
						.path("person/" + first_person_id + "/"
								+ measure_types.get(i)).request()
						.accept(MediaType.APPLICATION_XML).get();

				String result = response.readEntity(String.class);
				if (response.getStatus() == 200) {
					messageHeader(requ, response.getStatus(), "OK");
					System.out.println(prettyFormat(result));
					this.writerXml.write(messageHeader(requ, 200, "OK"));
					this.writerXml.write(prettyFormat(result));
				} else {
					messageHeader(requ, response.getStatus(), "NO CONTENT");
					this.writerXml
							.write(messageHeader(requ, 404, "NO CONTENT"));
				}

			}

			for (int i = 0; i < measure_types.size(); i++) {
				String requ = "#6 GET /person/"
						+ last_person_id
						+ "/"
						+ measure_types.get(i)
						+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

				Response response = service
						.path("person/" + last_person_id + "/"
								+ measure_types.get(i)).request()
						.accept(MediaType.APPLICATION_XML).get();

				String result = response.readEntity(String.class);
				if (response.getStatus() == 200) {
					messageHeader(requ, response.getStatus(), "OK");
					System.out.println(prettyFormat(result));

					this.writerXml.write(messageHeader(requ, 200, "OK"));
					this.writerXml.write(prettyFormat(result));
				} else {
					messageHeader(requ, response.getStatus(), "NO CONTENT");
					this.writerXml
							.write(messageHeader(requ, 404, "NO CONTENT"));
				}
			}
		}

	}

	public void task_3_8() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		String req = "#7 GET /person/" + first_person_id + "/" + measure_type
				+ "/" + measure_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response response = service
				.path("person/" + first_person_id + "/" + measure_type + "/"
						+ measure_id).request()
				.accept(MediaType.APPLICATION_XML).get();
		int resultCodeGet = response.getStatus();
		String result = response.readEntity(String.class);

		String resStr = "";
		if (resultCodeGet == 200) {
			resStr = "OK";
		} else {
			resStr = "ERROR";

		}
		System.out.println("\n\t\t******** 3.8 ********");
		System.out.println(messageHeader(req, resultCodeGet, resStr));
		System.out.println(prettyFormat(result));

		this.writerXml.write("\n\t\t******** 3.8 ********");
		this.writerXml.write(messageHeader(req, resultCodeGet, resStr));
		this.writerXml.write(prettyFormat(result));

	}

	public void task_3_9() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		String req = "#8 POST /person/" + first_person_id + "/" + measure_type
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		String reqGet = "#6 GET /person/" + first_person_id + "/" + measure_type
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";

		Response responseGet = service
				.path("person/" + first_person_id + "/" + measure_type)
				.request().get();

		int resultCodeGet = responseGet.getStatus();
		String result = responseGet.readEntity(String.class);
		// System.out.println(prettyFormat(result));

		int numberOfHistoriesBefore = 0;
		if (resultCodeGet == 200) {
			x.loadXML(result);
			NodeList nodes = x
					.getNodeListResult("healthMeasureHistories/measure/mid");
			numberOfHistoriesBefore = nodes.getLength();
			System.out.println("Length of hsitory before: "
					+ numberOfHistoriesBefore);

			String toPost = "<measure>" + "<value>72</value>"
					+ "<created>2011-12-09</created>" + "</measure>";

			Response responsePost = service
					.path("person/" + first_person_id + "/" + measure_type)
					.request().accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(toPost));

			int resultPost = responsePost.getStatus();
			String responseStrPost = responsePost.readEntity(String.class);
			System.out.println("Result of the post: " + resultPost);
			// String resultStr = response.readEntity(String.class);

			Response response2 = service
					.path("person/" + first_person_id + "/" + measure_type)
					.request().get();

			int resultCodeGet2 = response2.getStatus();
			System.out.println("Result of get:" + resultCodeGet2);
			String result2 = response2.readEntity(String.class);
			x.loadXML(result2);
			NodeList nodes2 = x
					.getNodeListResult("healthMeasureHistories/measure/mid");
			int numberOfHistoriesAfter = nodes2.getLength();
			System.out.println("Length of hsitory after: "
					+ numberOfHistoriesAfter);
			// System.out.println(prettyFormat(result2));

			String resStr = "";
			if (numberOfHistoriesBefore < numberOfHistoriesAfter) {
				resStr = "OK";
			} else {
				resStr = "ERROR";
			}

			System.out.println("\n\t\t******** 3.9 ********");
			System.out.println(messageHeader(reqGet, resultCodeGet, "OK"));
			System.out.println(prettyFormat(result));

			System.out.println(messageHeader(req, resultPost, resStr));
			System.out.println(prettyFormat(responseStrPost));

			System.out.println(messageHeader(reqGet, resultCodeGet2, "OK"));
			System.out.println(prettyFormat(result2));

			this.writerXml.write("\n\t\t******** 3.9 ********");
			this.writerXml.write(messageHeader(reqGet, resultCodeGet, "OK"));
			this.writerXml.write(prettyFormat(result));

			this.writerXml.write(messageHeader(req, resultPost, resStr));
			this.writerXml.write(prettyFormat(responseStrPost));

			this.writerXml.write(messageHeader(reqGet, resultCodeGet2, "OK"));
			this.writerXml.write(prettyFormat(result2));

			// this.writerXml.write(messageHeader(req, resultPost, resStr));

			// this.writerXml.write(prettyFormat(result));
		}

	}

	public void task_3_10() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		String req = "#10 PUT /person/" + first_person_id + "/" + measure_type
				+ "/" + measure_id
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";
		System.err.println(first_person_id + " " + measure_type + " " + measure_id);
		Response responseGet = service
				.path("person/" + first_person_id + "/" + measure_type + "/"
						+ measure_id).request()
				.accept(MediaType.APPLICATION_XML).get();

		String result = responseGet.readEntity(String.class);
		System.out.println(result);
		
		x.loadXML(result);
		Node node = x.getNodeResult("healthMeasureHistories/"
				+ "measure/value");
		String oldValue = node.getTextContent();
		System.out.println("Old value: " + oldValue);

		String toPut = "<measure>" + "<value>100</value>"
				+ "<created>2011-12-09</created>" + "</measure>";

		Response responsePut = service
				.path("person/" + first_person_id + "/" + measure_type + "/"
						+ measure_id).request(MediaType.APPLICATION_XML)
				.put(Entity.xml(toPut));

		int resultPut = responsePut.getStatus();
		System.out.println("Put result: " + resultPut);

		String requGet = "#6 GET /person/" + first_person_id + "/"
				+ measure_type
				+ " Accept: APPLICATION/XML Content-Type: APPLICATION/XML";
		Response responseGetUpdated = service
				.path("person/" + first_person_id + "/" + measure_type + "/"
						+ measure_id).request()
				.accept(MediaType.APPLICATION_XML).get();

		String resultNew = responseGetUpdated.readEntity(String.class);

		x.loadXML(resultNew);
		Node nodeNew = x.getNodeResult("healthMeasureHistories/measure/value");
		String newValue = nodeNew.getTextContent();
		System.out.println("New value: " + newValue);

		String resultOfRequest;
		if (!oldValue.equals(newValue) && resultPut == 201) {
			resultOfRequest = "OK";
		} else {
			resultOfRequest = "ERROR";
		}

		System.out.println("\n\t\t******** 3.10 ********");
		System.out.println(messageHeader(req, resultPut, resultOfRequest));
		System.out.println(messageHeader(requGet,
				responseGetUpdated.getStatus(), "OK"));
		System.out.println(prettyFormat(resultNew));

		this.writerXml.write("\n\t\t******** 3.10 ********");
		this.writerXml.write(messageHeader(req, resultPut, resultOfRequest));
		this.writerXml.write(messageHeader(requGet,
				responseGetUpdated.getStatus(), "OK"));
		this.writerXml.write(prettyFormat(resultNew));
		// this.writerXml.write(prettyFormat(result));
	}

	public static void main(String[] args) {
		ClientSide c = new ClientSide();

		try {
			c.writerXml = new PrintWriter("client-server-xml.log", "UTF-8");
			c.writerJson = new PrintWriter("client-server-json.log", "UTF-8");

			// Server's url
			System.out.println("URL of the server: " + getBaseURI());

			try {
				// xml
				c.task_3_1();
				c.task_3_2();
				c.task_3_3();
				c.task_3_4();
				c.task_3_5();
				c.task_3_6();
				c.task_3_7();
				c.task_3_8();
				c.task_3_9();
				c.task_3_10();

				// json-only printings
				c.json3_1();
				c.json3_2();
				c.json3_3();
				c.json3_4();
				c.json3_5();
				c.json3_6();
				c.json3_7();
				c.json3_8();
				c.json3_9();
				c.json3_10();
			} catch (XPathExpressionException | ParserConfigurationException
					| SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			c.writerXml.close();
			c.writerJson.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public String messageHeader(String query, int status, String resultStr) {
		return "\n" + query + "\n" + "=>Result: " + resultStr + "\n"
				+ "=>HTTP Status: " + status + "\n";
	}

	private static URI getBaseURI() {
		// return
		// UriBuilder.fromUri("http://10.218.221.148:5700/sdelab/").build();
		return UriBuilder.fromUri(
				"https://warm-crag-1462.herokuapp.com/sdelab").build();

	}

	public static String prettyFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please
											// review it
		}
	}

	public static String prettyFormat(String input) {
		return prettyFormat(input, 2);
	}
}
