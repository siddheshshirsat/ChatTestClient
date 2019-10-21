package com.chat.testclient;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Driver {

    public static void main(String[] args) throws Exception {
        User user = new User("testUserId1");
        String url = user.requestConnectingUrl();
        
        new ConnectionListener(url, user).initialize();
        System.out.println("Reached...");
    }
}
