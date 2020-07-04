package org.unctad.docker.java.server.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.unctad.docker.java.model.Count;
import org.unctad.docker.java.model.DecisionDefinition;
import org.unctad.docker.java.model.Deployment;
import org.unctad.docker.java.model.ProcessDefinition;
import org.unctad.docker.java.model.ProcessTask;
import org.unctad.docker.java.server.DefaultApi;
import org.unctad.docker.java.server.utils.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import java.util.logging.Level;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;

public class MiddleWareRestImpl implements DefaultApi {
	
	private static final Logger LOGGER = Logger.getLogger(MiddleWareRestImpl.class.getName());

	@Override
	public Response getsTaskSubmission(String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response saveTaskSubmission(String taskId, String formData) {
		LOGGER.log(Level.INFO, "Submission data received!");
		LOGGER.log(Level.INFO, formData);
		try {
			String taskKey = getCamundaTaskKey(taskId);
			String uri = "http://haproxy:6001/formio/" + taskKey + "/submission?dryrun=1";
			List<Object> providers = new ArrayList<Object>();
			providers.add(new JacksonJsonProvider());
			WebClient client = WebClient.create(uri, providers).type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON);
			String validation = client.post(formData).readEntity(String.class);
			LOGGER.log(Level.INFO, "formio validation: " + validation);
			boolean isValid = Boolean.parseBoolean(validation);
			if (isValid && completeTask(taskId, formData)) {
				Response response = Response.status(Status.OK).entity(true).build();
				return response;
			} else {
				Response response = Response.status(Status.OK).entity(false).build();
				return response;
			}
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.toString());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}
	
	private boolean completeTask(String taskId, String formData) {
		String uri = "http://camunda:8080/engine-rest/engine/default/task/" + taskId + "/complete";
		List<Object> providers = new ArrayList<Object>();
		providers.add(new JacksonJsonProvider());
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		String body = Utils.convertToCamundaJson(formData);
		if (body == null) {
			return false;
		}
		Response response = client.post(body);
		if (response.getStatus() == 204) {
			LOGGER.log(Level.WARNING, "Task sumbitted, taskId: " + taskId);
			return true;
		}
		LOGGER.log(Level.WARNING, "Task not sumbitted, taskId: " + taskId);
		return false;
	}

	@Override
	public Response getTaskForm(String taskId) {
		try {
			String taskKey = getCamundaTaskKey(taskId);
			if (taskKey != null) {
				String uri = "http://haproxy:6001/formio/" + taskKey;
				List<Object> providers = new ArrayList<Object>();
				providers.add( new JacksonJsonProvider() );
				WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
				String formIoResponse = client.get().readEntity(String.class);	
				String formVariables = getCamundaTaskFormVariables(taskId);
				JSONObject obj = new JSONObject(formIoResponse);
				JSONObject variables = new JSONObject();
				variables.put("data", new JSONObject(Utils.convertFromCamundaToJson(formVariables)));
				obj.accumulate("variables", variables);
				Response response = Response.status(Status.OK).entity(obj.toString()).build();
				return response;
			} else {
				LOGGER.log(Level.WARNING, "Task form not found, taskId: " + taskId);
				Response response = Response.status(Status.NOT_FOUND).entity("Task form not found!").build();
				return response;
			}
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.toString());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}
	
	private String getCamundaTaskKey(String taskId) throws JSONException {
		String uri = "http://camunda:8080/engine-rest/engine/default/task/" + taskId + "/form";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		String json = client.get().readEntity(String.class);
		JSONObject resultObject = new JSONObject(json);
		return resultObject.getString("key");
	}
	
	private String getCamundaTaskFormVariables(String taskId) throws JSONException {
		String uri = "http://camunda:8080/engine-rest/engine/default/task/" + taskId + "/form-variables";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		String json = client.get().readEntity(String.class);
		return json;
	}

	@Override
	public Response getProcessDefinitionList() {
		String uri = "http://camunda:8080/engine-rest/engine/default/process-definition?sortBy=name&sortOrder=asc";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		List<ProcessDefinition> processDefinitions = client.get().readEntity(List.class);
		Response response = Response.status(Status.OK).entity(processDefinitions).build();
		return response;
	}

	@Override
	public Response getProcessDefinitionCount() {
		String uri = "http://camunda:8080/engine-rest/engine/default/process-definition/count";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		Count count = client.get().readEntity(Count.class);
		Response response = Response.status(Status.OK).entity(count).build();
		return response;
	}

	@Override
	public Response getTaskList() {
		String uri = "http://camunda:8080/engine-rest/engine/default/task?sortBy=created&sortOrder=desc";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		List<ProcessTask> processDefinitions = client.get().readEntity(List.class);
		Response response = Response.status(Status.OK).entity(processDefinitions).build();
		return response;
	}

	@Override
	public Response startProcess(String processDefinitionId) {
		try {
			String uri = "http://camunda:8080/engine-rest/engine/default/process-definition/" + processDefinitionId + "/start";
			List<Object> providers = new ArrayList<Object>();
			providers.add(new JacksonJsonProvider());
			WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			String process = client.post("{}").readEntity(String.class);
			JSONObject resultObject = new JSONObject(process);
			String processId = resultObject.getString("id");
			String taskId = getProcessTaskId(processId);
			System.out.println("taskID: " + taskId);
			JSONObject task = new JSONObject();
			task.put("id", taskId);
			Response response = Response.status(Status.OK).entity(task.toString()).build();
			return response;
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}
	
	private String getProcessTaskId(String processId) throws JSONException {
		String uri = "http://camunda:8080/engine-rest/engine/default/task?processInstanceId=" + processId;
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		String taskListJson = client.get().readEntity(String.class);
		LOGGER.log(Level.FINE, "getProcessTaskId = " + taskListJson);
		if (taskListJson != null) {
			JSONArray jsonarray = new JSONArray(taskListJson);
			JSONObject jsonObject = jsonarray.getJSONObject(0);
			return jsonObject.getString("id");
		}
		return null;
	}

	@Override
	public Response getProcessDefinitionXml(String key) {
		try {
			String uri = "http://camunda:8080/engine-rest/engine/default/process-definition/key/" + key + "/xml";
			List<Object> providers = new ArrayList<Object>();
			providers.add( new JacksonJsonProvider() );
			WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			String data = client.get().readEntity(String.class);
			LOGGER.log(Level.FINE, "getProcessDefinitionXml = " + data);
			if (data != null) {
				JSONObject jsonObject = new JSONObject(data);
				String xml = jsonObject.getString("bpmn20Xml");
				Response response = Response.status(Status.OK).entity(xml).build();
				return response;
			} else {
				return null;
			}
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}

	@Override
	public Response deployProcessDefinition(String deploymentName, String content) {
		//LOGGER.log(Level.WARNING, "deployProcessDefinition input data = " + content);
		try {
			System.out.println("*******************");
			System.out.println("deploymentName = " + deploymentName);
			System.out.println("content = " + content);
			System.out.println("deployment-source = " + Utils.getNameFromProcessXml(content));
			System.out.println("*******************");
			
			String uri = "http://camunda:8080/engine-rest/engine/default/deployment/create";
			//String uri = "http://localhost:6009/engine-rest/engine/default/deployment/create";
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(uri);

			MultipartEntity reqEntity = new MultipartEntity();
			ByteArrayInputStream contentStream = new ByteArrayInputStream(content.getBytes());
			InputStreamBody streamBody = new InputStreamBody(contentStream, MediaType.APPLICATION_OCTET_STREAM, deploymentName);
			reqEntity.addPart("project", streamBody);
			
			reqEntity.addPart("deployment-name", new StringBody(deploymentName));	
			reqEntity.addPart("enable-duplicate-filtering", new StringBody("true"));	
			reqEntity.addPart("deployment-source", new StringBody(Utils.getNameFromProcessXml(content)));	
			httppost.setEntity(reqEntity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httppost, responseHandler);
			LOGGER.log(Level.WARNING, "deployProcessDefinition Camunda result = " + responseBody);
			Response response = Response.status(Status.OK).entity(responseBody).build();
			return response;
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}
	
	public static void main(String[] args) throws Exception {
		FileBody body = new FileBody(new File(MiddleWareRestImpl.class.getResource("/test.bpmn").getFile()));
		String content = IOUtils.toString(body.getInputStream(), "UTF-8");
		Response response = new MiddleWareRestImpl().deployProcessDefinition("test.bpmn", content);
		System.out.println("deployProcessDefinition Camunda result = " + response.getEntity());
	}
	
	public static void main2(String[] args) throws Exception {
		String uri = "http://localhost:6001/java/v2016/06/engine-rest/engine/default/deployment/create";
		FileBody body = new FileBody(new File(MiddleWareRestImpl.class.getResource("/test.bpmn").getFile()));
		String content = IOUtils.toString(body.getInputStream(), "UTF-8");
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(uri);
		MultipartEntity reqEntity = new MultipartEntity();
		
		reqEntity.addPart("content", new StringBody(content));
		httppost.addHeader("deployment-name", "test3.bpmn");
        httppost.addHeader("enable-duplicate-filtering", "true");
        httppost.addHeader("deploy-changed-only-name", "hjello");
        httppost.setEntity(reqEntity);
        HttpResponse camResponse = httpclient.execute(httppost);
        HttpEntity resEntity = camResponse.getEntity();
        String result = IOUtils.toString(resEntity.getContent(), "UTF-8");
		System.out.println("deployProcessDefinition Camunda result = " + result);
	}

	@Override
	public Response evaluateDecision(String decisionDefinitionId, String jsonContent) {
		String uri = "http://camunda:8080/engine-rest/engine/default/decision-definition/" + decisionDefinitionId + "/evaluate";
		List<Object> providers = new ArrayList<Object>();
		providers.add(new JacksonJsonProvider());
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		String jsonString = client.post(jsonContent).readEntity(String.class);
		Response response = Response.status(Status.OK).entity(jsonString).build();
		return response;
	}

	@Override
	public Response getDecisionDefinitionCount() {
		String uri = "http://camunda:8080/engine-rest/engine/default/decision-definition/count";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		Count count = client.get().readEntity(Count.class);
		Response response = Response.status(Status.OK).entity(count).build();
		return response;
	}

	@Override
	public Response getDecisionDefinitionList() {
		String uri = "http://camunda:8080/engine-rest/engine/default/decision-definition/";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		List<DecisionDefinition> decisionDefinitions = client.get().readEntity(List.class);
		Response response = Response.status(Status.OK).entity(decisionDefinitions).build();
		return response;
	}

	@Override
	public Response getDecisionDefinitionXml(String key) {
		try {
			String uri = "http://camunda:8080/engine-rest/engine/default/decision-definition/key/" + key + "/xml";
			List<Object> providers = new ArrayList<Object>();
			providers.add( new JacksonJsonProvider() );
			WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			String data = client.get().readEntity(String.class);
			LOGGER.log(Level.FINE, "getDecisionDefinitionXml = " + data);
			if (data != null) {
				JSONObject jsonObject = new JSONObject(data);
				String xml = jsonObject.getString("dmnXml");
				Response response = Response.status(Status.OK).entity(xml).build();
				return response;
			} else {
				return null;
			}
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}

	@Override
	public Response getDeploymentList() {
		String uri = "http://camunda:8080/engine-rest/engine/default/deployment/";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		List<Deployment> deployments = client.get().readEntity(List.class);
		Response response = Response.status(Status.OK).entity(deployments).build();
		return response;
	}

	@Override
	public Response getDeploymentXml(String deploymentId) {
		try {
			String resourceId = getDeploymentResourceId(deploymentId);
			String uri = "http://camunda:8080/engine-rest/engine/default/deployment/" + deploymentId + "/resources/" + resourceId + "/data";
			List<Object> providers = new ArrayList<Object>();
			providers.add( new JacksonJsonProvider() );
			WebClient client = WebClient.create(uri, providers);
			String xml = client.get().readEntity(String.class);
			JSONObject json = new JSONObject();
			json.put("xml", xml);
			Response response = Response.status(Status.OK).entity(json.toString()).build();
			return response;
		} catch (JSONException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error!").build();
			return response;
		}
	}
	
	private String getDeploymentResourceId(String deploymentId) {
		String uri = "http://camunda:8080/engine-rest/engine/default/deployment/" + deploymentId + "/resources";
		List<Object> providers = new ArrayList<Object>();
		providers.add( new JacksonJsonProvider() );
		WebClient client = WebClient.create(uri, providers).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
		List<LinkedHashMap> resources = client.get().readEntity(List.class);
		if (resources != null && resources.size() > 0) {
			for (LinkedHashMap map : resources) {
				if (((String) map.get("name")).endsWith(".bpmn")) {
					return (String) map.get("id");
				}
			}
		}
		return null;
		
	}

}
