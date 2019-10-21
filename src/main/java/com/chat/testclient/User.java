package com.chat.testclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.chat.connectionmanager.model.ConnectionRequest;
import com.chat.connectionmanager.model.ConnectionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

public class User {
	private static final String CONNECTION_MANAGER_URL = "http://localhost:9000/requestConnection";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Getter
	private String userId;
	
	HttpClient httpClient;

	public User(String userId) {
		this.userId = userId;
		httpClient = HttpClients.createDefault();
	}

	public String requestConnectingUrl() throws Exception {
		ConnectionRequest connectionRequest = new ConnectionRequest();
		connectionRequest.setUserId(userId);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_URL);

		post.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(connectionRequest), ContentType.APPLICATION_JSON));

		HttpResponse response = httpClient.execute(post);
		ConnectionResponse connectionResponse =  OBJECT_MAPPER.readValue(EntityUtils.toString(response.getEntity()), ConnectionResponse.class);
		return connectionResponse.getUrl();
	}
}
