package com.chat.testclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;

public class Driver {
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
    	PropertyConfigurator.configure("log4j.properties");

    	ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.apache.http");
        root.setLevel(ch.qos.logback.classic.Level.INFO);

    	executorService.submit(new UserTask("testUserId1"));
    	executorService.submit(new UserTask("testUserId2"));
    }
}
