package com.chat.testclient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class SpringBasedConnectionListener {
	public void initialize() throws IOException, URISyntaxException {
		WebSocketClient client = new StandardWebSocketClient();

		WebSocketStompClient stompClient = new WebSocketStompClient(client);
		stompClient.setMessageConverter(new StringMessageConverter());

		StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
			@Override
			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
				session.subscribe("/user/queue/messages", this);
				System.out.println("New session:" + session.getSessionId());
			}

			@Override
			public void handleException(StompSession session, StompCommand command, StompHeaders headers,
					byte[] payload, Throwable exception) {
				exception.printStackTrace();
			}

			@Override
			public Type getPayloadType(StompHeaders headers) {
				return String.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				System.out.println("Received: " + payload);
			}
		};

		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("userId", "testUser");
		stompClient.connect("ws://localhost:9000/webSocketService", headers, sessionHandler);
	}
}
