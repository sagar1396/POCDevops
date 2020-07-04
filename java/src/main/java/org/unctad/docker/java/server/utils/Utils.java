package org.unctad.docker.java.server.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;

public class Utils {
	
	private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
	
	public static String convertToCamundaJson(String json) {
		try {
			JSONObject root = new JSONObject();
			JSONObject input = new JSONObject(json).getJSONObject("data");
			JSONObject variables = new JSONObject();
			convertToCamundaJsonObject(variables, "", input);
			root.put("variables", variables);
			return root.toString();
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			return null;
		}
	}
	
	private static void convertToCamundaJsonObject(JSONObject root, String varName, JSONObject data) throws JSONException {
		for (int i = 0; i < data.length(); i++) {
			String key = (String) data.names().get(i);
			Object value = data.get(key);
			if (value instanceof JSONObject) {
				convertToCamundaJsonObject(root, concatPrefix(varName) + key, (JSONObject) value);
			} else {
				JSONObject subVarValue = new JSONObject();
				subVarValue.put("value", value);
				subVarValue.put("type", "string");
				root.accumulate(concatPrefix(varName) + key, subVarValue);
			}
		}
	}
	
	private static String concatPrefix(String name) {
		if ("".equals(name)) {
			return "";
		} else {
			return name + "_";
		}
	}
	
	public static String convertFromCamundaToJson(String camundaJson) {
		JSONObject root = new JSONObject();
		try {
			JSONObject json = new JSONObject(camundaJson);
			for (int i = 0; i < json.length(); i++) {
				String key = (String) json.names().get(i);
				JSONObject valueObject = (JSONObject) json.get(key);
				Object value = valueObject.get("value");
				String[] names = extractNames(key);
				JSONObject current = root;
				int j = 0;
				for (String name : names) {
					j++;
					if (j == names.length) {
						current.accumulate(name, value);
					} else if (!current.has(name)) {
						current.put(name, new JSONObject());
					}
					if (j < names.length) {
						current = current.getJSONObject(name);
					}
				}
			}
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
		}
		return root.toString();
	}
	
	private static String[] extractNames(String name) {
		return name.split("_");
	}
	
	public static String getNameFromProcessXml(String xml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document document = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	        XPathFactory xpf = XPathFactory.newInstance();
	        XPath xp = xpf.newXPath();
	        XPathExpression expr = xp.compile("//definitions/process/@name");
	        return (String) expr.evaluate(document, XPathConstants.STRING);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			return null;
		}
	}
	
	public static void mains(String[] args) throws JSONException {
		// multilevel
		String formData = convertToCamundaJson("{\"data\":{\"address2\":{\"country\":\"Eesti\",\"city\":\"Kiili\",\"street\":\"Allika 26\"}}}");
		//String formData = convertToCamundaJson("{\"data\"={\"eesnimi\":\"wjjjcccjj\", \"perenimi\":\"wjeeeecjj\",\"accept\":{\"\":true}}}");
		System.out.println(formData);
		String json = convertFromCamundaToJson("{\"data_name\":{\"type\":\"String\",\"value\":\"Kalamees\",\"valueInfo\":{}},\"data_address_street\":{\"type\":\"String\",\"value\":\"Kalda\",\"valueInfo\":{}},\"data_address_city\":{\"type\":\"String\",\"value\":\"Kiili\",\"valueInfo\":{}},\"data_address_country\":{\"type\":\"String\",\"value\":\"Eesti\",\"valueInfo\":{}},\"requestor\":{\"type\":\"Null\",\"value\":null,\"valueInfo\":{}}}");
		//{"data":{"name":"Restoran Gloria","address":{"country":"Eesti","city":"Kiili","street":"Allika 26"}}}
		System.out.println(json);
	}
	
	public static void main(String[] args) throws JSONException {
		StringBuilder builder = new StringBuilder();
		builder.append("<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">");
		builder.append("<bpmn2:process id=\"Process_1\" name=\"Ingmars\" isExecutable=\"false\">");
		builder.append("</bpmn2:process>");
		builder.append("</bpmn2:definitions>");
		System.out.println(getNameFromProcessXml(builder.toString()));
	}

}
