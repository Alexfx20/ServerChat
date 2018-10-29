package ru.secondchat.network;

import java.io.IOException;

public interface ConnectionListener {

    void onConnectionReady(Connection connection);
    void onRegistration(Connection connection) throws IOException;
    void onReciveMessage(String value);
    void onDisconnect(Connection connection);
    void onException(Connection connection, Exception e);
    void processCommands(String value);
}
