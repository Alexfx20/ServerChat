package ru.secondchat.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.secondchat.network.RestConnection;
import ru.secondchat.server.Server;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.Assert.*;

public class RestClientsHandlerTest {

    RestClientsHandler client1;
    String id;
    String name;
    String status;
    int maxConnections;



    @Before
    public void setUp() throws Exception {
        client1 = new RestClientsHandler();
        id = "ABCD";
        name = "Smith";
        status = "agent";
        maxConnections = 2;
    }

    @After
    public void tearDown() throws Exception {
        client1 = null;
        id = null;
        name = null;
        status = null;

    }

    @Test
    public void getRegistrationMessage(){
        String tmp = "/register "+name+" "+status+"$";
        System.out.println(tmp);
        String regParams = client1.getRegistrationMessage(name, status, maxConnections);
        String regParams2 = client1.getRegistrationMessage(name, status, 0);
        assertEquals("Error in re message formating",regParams, tmp+maxConnections);
        assertEquals("Error in re message formating",regParams2, tmp+1);
    }

    @Test
    public void register() throws IOException {
        int tempConnectionsSize = Server.getConnectionsSize();//вычисляем начальный размер очереди на случай если она не пуста
        String tmp = client1.register(id, name, status, maxConnections);

        System.out.println(tmp);
        System.out.println(tempConnectionsSize);
        assertEquals("doesn't add new RestConnections to Server queue", (Server.getConnectionsSize()-tempConnectionsSize),1);
        assertEquals("Error while adding RestClient to the map", client1, RestClientsHandler.getInstance(id));
        RestConnection restConnection = client1.getConnection();
        String regTemplate = "/register "+name+" "+status+"$"+maxConnections; //то что должно поместиться в очередь connection
        String actual = restConnection.recieveSingleMessage();//извлекаем сообщение находящееся в очереди соединения
        assertEquals("RestConnection receiveMessage Error", regTemplate, actual);
        client1.onDisconnect(restConnection);
    }

    @Test(expected = SocketTimeoutException.class)
    public void sendMessage() throws IOException {
        client1.register(id, name, status, maxConnections);
        String messageTemplate = "Hello message";
        client1.sendMessage(messageTemplate);
        RestConnection restConnection = client1.getConnection();
        restConnection.recieveSingleMessage();//убираем регистарационное сообщение
        String expectedMessage = restConnection.recieveSingleMessage();
        assertEquals("Error in sendMessage mehod", expectedMessage, messageTemplate);
        client1.onDisconnect(restConnection);//удаляет клиента из карты, т.к. в конце кинется исключение
        client1.sendMessage(null);
        restConnection.setSoTimeout(1000);//устанавливаем время ожидания 1 секунду, если за это время не будет получено сообщения то кинется TimeOut Exception
        restConnection.recieveSingleMessage();

    }

    @Test
    public void getInstance() {
        client1.register(id, name, status, maxConnections);
        RestClientsHandler client2 = RestClientsHandler.getInstance(id);
        assertEquals("couldn't get RectHandlers instance", client1, client2);
        client1.onDisconnect(client1.getConnection());
    }

    @Test
    public void onReciveMessage() {
        client1.onReciveMessage("Hello");
        client1.onReciveMessage("second message");
        client1.onReciveMessage("third message");
        client1.onReciveMessage(null);
        String[] messages = client1.seeAllReceivedMessages();
        System.out.println(messages[0]+" "+messages[1]+" "+messages[2]);
        assertEquals("problems with message storage", messages[0], "third message");
        assertEquals("problems with message storage", messages[1], "second message");
        assertEquals("problems with message storage", messages[2], "Hello");
    }

    @Test
    public void processCommands() {
    }
}