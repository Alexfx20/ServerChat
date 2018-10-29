package ru.secondchat.network;

import java.io.IOException;

public interface Connection {

    void startNewChat();
    void sendMessage(String value);
    String recieveSingleMessage()throws IOException;
    void disconnect();
    void setEventListener(ConnectionListener eventListener);
    void setSoTimeout(int idleTime);

}
