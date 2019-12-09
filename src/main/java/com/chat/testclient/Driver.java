package com.chat.testclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.PropertyConfigurator;

public class Driver {
	private static final int NUMBER_OF_USERS = 2; 
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
    	PropertyConfigurator.configure("log4j.properties");

    	ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.apache.http");
        root.setLevel(ch.qos.logback.classic.Level.INFO);
        
        List<Future<Integer>> resultFutures = new ArrayList<>();
        for(int i=0; i<NUMBER_OF_USERS; i++) {
        	resultFutures.add(executorService.submit(new UserTask("testUserId" + i, NUMBER_OF_USERS)));
        }
        
        int resultSum = resultFutures.stream().mapToInt(f -> {
			try {
				return f.get();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}).sum();
        
        System.out.println("resultSum = " + resultSum);
    }
}
