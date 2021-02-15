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
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;

import com.chat.testclient.config.UsersConfig;

public class Driver {
	private static final String CONFIGURATION_USERS_PROPERTIES = "configuration/users.properties";
	private static ExecutorService executorService = Executors.newFixedThreadPool(2);

	public static void main(String[] args) throws Exception {
		initLogging();
		UsersConfig usersConfig = parseUsersConfig();

		List<Future<Integer>> resultFutures = new ArrayList<>();
		for (int i = 0; i < usersConfig.getNumberOfUsers(); i++) {
			resultFutures.add(executorService.submit(new UserTask("testUserId" + i, usersConfig)));
		}

		List<Integer> results = resultFutures.stream().map(f -> {
			try {
				return f.get();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}).collect(Collectors.toList());

		System.out.println("results = " + results);

		System.out.println("resultSum = " + results.stream().mapToInt(i -> i).sum());
	}

	private static UsersConfig parseUsersConfig() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(CONFIGURATION_USERS_PROPERTIES)));
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
