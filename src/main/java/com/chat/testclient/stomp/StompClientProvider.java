package com.chat.testclient.stomp;

public interface StompClientProvider {

    void disconnect();

    void subscribe(final String destination, final String id);

    void subscribe(final String destination);

    void unSubscribe(final String destination, final String id);

    void unSubscribe(final String destination);

    void sendMessage(final String destination, final String body);

    void sendFrame(final Frame frame);

}
