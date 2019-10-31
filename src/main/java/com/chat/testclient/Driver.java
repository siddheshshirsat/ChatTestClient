package com.chat.testclient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Driver {

	private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	public static final Set<String> USERS = new HashSet<>(Arrays.asList("testUser1",
																		"testUser2"));
//																		"testUser3",
//																		"testUser4",
//																		"testUser5"));

	public static void main(String[] args) throws Exception {
		initLogging();

		int numberOfMessages = 10;

		Set<Future<Integer>> userTaskFutures = USERS.stream()
				.map(u -> executorService.submit(new UserTask(u, numberOfMessages))).collect(Collectors.toSet());
		
		int result = userTaskFutures.stream().mapToInt(f -> {
			try {
				return f.get();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}).sum();
		
		System.out.println("Reached...result = " + result);
	}

	private static void initLogging() {
		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));

		for (String log : loggers) {
			Logger logger = (Logger) LoggerFactory.getLogger(log);
			logger.setLevel(Level.INFO);
			logger.setAdditive(false);
		}
	}
}
