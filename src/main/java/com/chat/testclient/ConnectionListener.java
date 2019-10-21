package com.chat.testclient;

import java.io.IOException;
import java.net.URISyntaxException;

import com.chat.testclient.stomp.Frame;
import com.chat.testclient.stomp.StompClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConnectionListener {
	private static final String CONNECTION_ENDPOINT = "/webSocketService";
	private static final String SUBSCRIBE_ENDPOINT = "/user/queue/messages";
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private String url;
	private User user;

	public void initialize() throws IOException, URISyntaxException {
		new StompClient(url + CONNECTION_ENDPOINT) {
            @Override
            protected void onStompError(String errorMessage) {
            }

            @Override
            protected void onConnection(boolean connected) {
            	System.out.println("Reached....connected");
            	this.subscribe(SUBSCRIBE_ENDPOINT, user.getUserId());
            }

            @Override
            protected void onDisconnection(String reason) {
            }

            @Override
            protected void onStompMessage(final Frame frame) {
            	System.out.println("Reached....message = " + frame.getBody());
            }
        };
	}
}
