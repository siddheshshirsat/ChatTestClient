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
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@ClientEndpoint
public class UserTask implements Callable<Boolean> {
	private static final String CONNECTION_MANAGER_ENDPOINT = "http://localhost:9000";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String CONNECTION_API = "/user";
	private static final String REQUEST_CONNECTION_ENDPOINT = "/requestConnection";
	private static final String SEND_MESSAGE_API = "/sendMessage";

	@Getter
	private String userId;

	HttpClient httpClient;

	public UserTask(String userId) {
		this.userId = userId;
		httpClient = HttpClients.createDefault();
	}

	public Boolean call() throws Exception {
		RequestConnectionRequest connectionRequest = new RequestConnectionRequest();
		connectionRequest.setUserId(userId);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_ENDPOINT + REQUEST_CONNECTION_ENDPOINT);

		post.setEntity(
				new StringEntity(OBJECT_MAPPER.writeValueAsString(connectionRequest), ContentType.APPLICATION_JSON));

		HttpResponse response = httpClient.execute(post);
		RequestConnectionResponse connectionResponse = OBJECT_MAPPER
				.readValue(EntityUtils.toString(response.getEntity()), RequestConnectionResponse.class);
		setupConnection(connectionResponse.getUrl());
		
		// random async. message sending
		for(int i=0; i<2;i++) {
			int waitTime = (int)(Math.random() * 5) + 1;
			Thread.sleep(waitTime* 1000);
			sendMessage();
		}
		return true;
	}

	private void setupConnection(String url) throws IOException, URISyntaxException {
		WebSocketContainer container = null;//
		Session session = null;
		try {
			// Tyrus is plugged via ServiceLoader API. See notes above
			container = ContainerProvider.getWebSocketContainer();
			// WS1 is the context-root of my web.app
			// ratesrv is the path given in the ServerEndPoint annotation on server
			// implementation
			System.out.println("Reached..." + url);
			session = container.connectToServer(UserWebsocketClientEndpoint.class, URI.create(url + CONNECTION_API));
	        session.getAsyncRemote().sendText("Connect:" + userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			if (session != null) {
//				try {
//					session.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
		}
	}

	@OnMessage
	public void onMessage(String message) {
		// the new USD rate arrives from the websocket server side.
		System.out.println("Received msg: " + message);
	}

	private void sendMessage() throws IOException, URISyntaxException {
		String recipientId = "testUserId1".equals(userId) ? "testUserId2" : "testUserId1";
		Message message = new Message(userId, recipientId, "Random message", System.currentTimeMillis());
		SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
		HttpPost post = new HttpPost(CONNECTION_MANAGER_ENDPOINT + SEND_MESSAGE_API);

		post.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(sendMessageRequest), ContentType.APPLICATION_JSON));

		httpClient.execute(post);
//		SendMessageResponse sendMessageResponse = OBJECT_MAPPER.readValue(EntityUtils.toString(response.getEntity()), SendMessageResponse.class);
//		System.out.println("Message sent successfully for user = " + userId);
	}
}
