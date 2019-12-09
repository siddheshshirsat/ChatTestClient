package com.chat.testclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.chat.connectionmanager.model.Message;
import com.chat.connectionmanager.model.RequestConnectionRequest;
import com.chat.connectionmanager.model.RequestConnectionResponse;
import com.chat.connectionmanager.model.SendMessageRequest;
import com.chat.connectionmanager.model.SendMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@ClientEndpoint
public class UserTask implements Callable<Integer> {
	private static final int MESSAGES_TO_SEND = 10;
	private static final long MAX_WAIT_TIME_MILLIS = 5000;

	private static final String CONNECTION_MANAGER_ENDPOINT = "http://localhost:9000";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String CONNECTION_API = "/user";
	private static final String REQUEST_CONNECTION_ENDPOINT = "/requestConnection";
	private static final String SEND_MESSAGE_API = "/sendMessage";

	@Getter
	private String userId;

	@Getter
	private int totalNumberOfUsers;

	HttpClient httpClient;

	public UserTask(String userId, int totalNumberOfUsers) {
		this.userId = userId;
		this.totalNumberOfUsers = totalNumberOfUsers;
		httpClient = HttpClients.createDefault();
	}

	public Integer call() throws Exception {
		RequestConnectionRequest connectionRequest = new RequestConnectionRequest();
		connectionRequest.setUserId(userId);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_ENDPOINT + REQUEST_CONNECTION_ENDPOINT);

		post.setEntity(
				new StringEntity(OBJECT_MAPPER.writeValueAsString(connectionRequest), ContentType.APPLICATION_JSON));

		HttpResponse response = httpClient.execute(post);
		RequestConnectionResponse connectionResponse = OBJECT_MAPPER
				.readValue(EntityUtils.toString(response.getEntity()), RequestConnectionResponse.class);
		setupConnection(connectionResponse);

		// random async. message sending
		int count = 0;
		for (int i = 0; i < MESSAGES_TO_SEND; i++) {
			int waitTime = (int) (Math.random() * MAX_WAIT_TIME_MILLIS) + 1;
			Thread.sleep(waitTime);
			count += sendMessage();
		}
		return count;
	}

	private void setupConnection(RequestConnectionResponse connectionResponse) throws IOException, URISyntaxException {
		WebSocketContainer container = null;//
		Session session = null;
		try {
			container = ContainerProvider.getWebSocketContainer();
			System.out.println("Reached..." + connectionResponse);
			session = container.connectToServer(UserWebsocketClientEndpoint.class, URI.create(
					connectionResponse.getScheme() + "://" + connectionResponse.getEndpoint() + CONNECTION_API));
			session.getAsyncRemote().sendText("Connect:" + userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(String message) {
		System.out.println("Received msg: " + message);
	}

	private int sendMessage() throws IOException, URISyntaxException {
		int recipientIndex = (int) (Math.random() * totalNumberOfUsers);

		String recipientId = "testUserId" + recipientIndex;
		Message message = new Message(userId, recipientId, "Random message", System.currentTimeMillis());
		SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_ENDPOINT + SEND_MESSAGE_API);

		post.setEntity(
				new StringEntity(OBJECT_MAPPER.writeValueAsString(sendMessageRequest), ContentType.APPLICATION_JSON));

		HttpResponse response = httpClient.execute(post);
		SendMessageResponse sendMessageResponse = OBJECT_MAPPER.readValue(EntityUtils.toString(response.getEntity()),
				SendMessageResponse.class);

		if (sendMessageResponse.isDelivered()) {
			return 1;
		} else {
			return 0;
		}
//		System.out.println("Message sent successfully for user = " + userId);
	}
}
