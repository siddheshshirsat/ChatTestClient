package com.chat.testclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
    	System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    	System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    	System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "INFO");
    	System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "ERROR");

    	executorService.submit(new UserTask("testUserId1"));
    	executorService.submit(new UserTask("testUserId2"));
    }
}
