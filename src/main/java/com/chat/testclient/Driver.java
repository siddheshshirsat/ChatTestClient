package com.chat.testclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.PropertyConfigurator;

import com.chat.testclient.config.UsersConfig;

public class Driver {
	private static final String CONFIGURATION_USERS_PROPERTIES = "configuration/users.properties";

	public static void main(String[] args) throws Exception {
		initLogging();

		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(CONFIGURATION_USERS_PROPERTIES)));
		UsersConfig usersConfig = parseUsersConfig(properties);

		ExecutorService executorService = Executors
				.newFixedThreadPool(Integer.parseInt(properties.getProperty("threadPoolSize")));

		List<Future<Integer>> resultFutures = new ArrayList<>();
		List<UserTask> userTasks = new ArrayList<>();
		for (int i = 0; i < usersConfig.getNumberOfUsers(); i++) {
			userTasks.add(new UserTask("testUserId" + i, usersConfig));
		}

		for (UserTask userTask: userTasks) {
			resultFutures.add(executorService.submit(userTask));
		}

		int sum = 0;
		while(sum < usersConfig.getNumberOfUsers()* usersConfig.getMessagesSentPerUser()) {
			sum = 0;
			for (UserTask userTask: userTasks) {
				sum += userTask.getQueue().size();
			}
			System.out.println("Reached...recieved message count = " + sum);
			Thread.sleep(5000);
		}
		System.out.println("Reached...final total recieved message count = " + sum);
	}

	private static UsersConfig parseUsersConfig(Properties properties) throws FileNotFoundException, IOException {
		return UsersConfig.builder().numberOfUsers(Integer.parseInt(properties.getProperty("numberOfUsers")))
				.messagesSentPerUser(Integer.parseInt(properties.getProperty("messagesSentPerUser")))
				.maxWaitTimeBetweenMessagesMillis(
						Long.parseLong(properties.getProperty("maxWaitTimeBetweenMessagesMillis")))
				.build();
	}

	private static void initLogging() {
		PropertyConfigurator.configure("log4j.properties");

		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.apache.http");
		root.setLevel(ch.qos.logback.classic.Level.INFO);
	}
}
