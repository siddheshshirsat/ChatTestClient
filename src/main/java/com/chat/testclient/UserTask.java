package com.chat.testclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

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
import com.chat.testclient.config.UsersConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@ClientEndpoint
public class UserTask implements Callable<Integer> {
	private static final String CONNECTION_MANAGER_ENDPOINT = "http://localhost:9000";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String CONNECTION_API = "/user";
	private static final String REQUEST_CONNECTION_ENDPOINT = "/requestConnection";
	private static final String SEND_MESSAGE_API = "/sendMessage";

	private String userId;
	private UsersConfig usersConfig;
	private HttpClient httpClient;

	@Getter
	private Queue<String> queue;

	public UserTask(String userId, UsersConfig usersConfig) {
		this.userId = userId;
		this.usersConfig = usersConfig;
		httpClient = HttpClients.createDefault();
		this.queue = new LinkedBlockingQueue<>();
	}

	public Integer call() throws Exception {
		System.out.println("Reached..." + ", user id " + userId + ", current thread = " + Thread.currentThread().getId());
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
		for (int i = 0; i < usersConfig.getMessagesSentPerUser(); i++) {
			int waitTime = (int) (Math.random() * usersConfig.getMaxWaitTimeBetweenMessagesMillis()) + 1;
			Thread.sleep(waitTime);
			sendMessage();
		}
		return 0;
	}

	private void setupConnection(RequestConnectionResponse connectionResponse) throws IOException, URISyntaxException {
		Session session = null;
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			System.out.println("Reached..." + connectionResponse + ", user id = " + userId);
			session = container.connectToServer(this, URI.create(
					connectionResponse.getScheme() + "://" + connectionResponse.getEndpoint() + CONNECTION_API));
			session.getAsyncRemote().sendText("Connect:" + userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public synchronized void onMessage(String message) {
		queue.add(message);
//		System.out.println("Received: " + ", user id " + userId + ", queue = " + queue);
	}

	private int sendMessage() throws IOException, URISyntaxException {
		int recipientIndex = (int) (Math.random() * (usersConfig.getNumberOfUsers()-1));
		if(recipientIndex >= Integer.parseInt(userId.substring("testUserId".length()))) {
			recipientIndex++;
		}

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
