package com.chat.testclient;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;

@ClientEndpoint
public class UserWebsocketClientEndpoint {
	@OnMessage
	public void onMessage(String message) {
		System.out.println("Received msg: " + message);
	}
}
