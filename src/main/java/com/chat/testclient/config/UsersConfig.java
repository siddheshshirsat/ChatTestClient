package com.chat.testclient.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsersConfig {
	private int numberOfUsers;
	private int messagesSentPerUser;
	private long maxWaitTimeBetweenMessagesMillis;
}
