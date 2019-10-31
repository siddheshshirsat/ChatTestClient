package com.chat.testclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
import com.chat.testclient.stomp.Frame;
import com.chat.testclient.stomp.StompClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserTask implements Callable<Integer> {
	private static final String CONNECTION_MANAGER_ENDPOINT = "http://localhost:9000";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String CONNECTION_API = "/webSocketService";
	private static final String SUBSCRIBE_API = "/user/queue/messages";
	private static final String REQUEST_CONNECTION_ENDPOINT = "/requestConnection";
	private static final String SEND_MESSAGE_API = "/sendMessage";

	private String userId;
	private int numberOfMessages;
	private int receivedMessageCount;
	HttpClient httpClient;

	public UserTask(String userId, int numberOfMessages) {
		this.userId = userId;
		this.numberOfMessages = numberOfMessages;
		this. receivedMessageCount = 0;
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
		StompClient stompClient = setupConnection(connectionResponse.getUrl());

		// random async. message sending
		for (int i = 0; i < numberOfMessages; i++) {
			int waitTime = (int) (Math.random() * 5) + 1;
			Thread.sleep(waitTime * 1000);
			sendMessage();
		}
		
		Thread.sleep(10000);
		stompClient.disconnect();

		// -1 for an emoty message recevied on connection setup.
		return receivedMessageCount -1;
	}

	private StompClient setupConnection(String url) throws IOException, URISyntaxException {
		return new StompClient(url + CONNECTION_API, userId) {
			@Override
			protected void onStompError(String errorMessage) {
			}

			@Override
			protected void onConnection(boolean connected) {
				System.out.println("Reached....connected");
				this.subscribe(SUBSCRIBE_API, userId);
			}

			@Override
			protected void onDisconnection(String reason) {
			}

			@Override
			protected void onStompMessage(final Frame frame) {
				System.out.println("Reached....message = " + frame.getBody());
				receivedMessageCount++;
			}
		};
	}

	private void sendMessage() throws IOException, URISyntaxException {
		List<String> possibleRecipients = new ArrayList<>(Driver.USERS);
		possibleRecipients.remove(userId);
		int randomIndex = (int)(Math.random() * possibleRecipients.size());
		String recipientId = possibleRecipients.get(randomIndex);
		Message message = new Message(userId, recipientId, "Random message", System.currentTimeMillis());
		SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_ENDPOINT + SEND_MESSAGE_API);

		post.setEntity(
				new StringEntity(OBJECT_MAPPER.writeValueAsString(sendMessageRequest), ContentType.APPLICATION_JSON));

		HttpResponse response = httpClient.execute(post);
		SendMessageResponse sendMessageResponse = OBJECT_MAPPER.readValue(EntityUtils.toString(response.getEntity()), SendMessageResponse.class);
//		System.out.println("Message sent successfully " + sendMessageResponse.isDelivered());
	}
}
