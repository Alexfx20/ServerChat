package ru.secondchat.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.secondchat.network.Connection;
import ru.secondchat.network.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class ClientHandlerTest {

    Connection clientConnection;
    ClientHandler agent;
    ServerSocket server;
    Socket socket;
    String received;
    Connection serverConnection;

    @Before
    public void setUp() throws Exception {
        server =new ServerSocket(5001);
        testThread.start();
        socket = server.accept();
        testThread.join();
        agent = new ClientHandler(socket,2);
        serverConnection = new SocketConnection(agent, socket);
    }

    @After
    public void tearDown() throws Exception {
        server.close();
        socket.close();
        testThread = null;
        agent = null;
        serverConnection.disconnect();
        clientConnection.disconnect();
    }

    /*@Test
    public void setRecipient() {
        agent.setRecipient(agent,null);
        assertEquals("ошибка при установке сокета получателя", clientConnection, agent.getRecipient());
    }*/

    /*@Test
    public void getRecipient() {
        agent.setRecipient(agent, null);
        assertEquals("ошибка при получении сокета получателя", clientConnection, agent.getRecipient());
    }*/

   /* @Test
    public void getFirstMessage() throws IOException {

        agent.onReciveMessage(agent.getHandlingUser(), "hello");
        assertEquals("Неправильно возвращает первое сообщение", "hello",agent.getFirstMessage());
    }*/


/*
    @Test
    public void processCommands() throws IOException {
        agent.setHandlingUser(serverConnection);
        agent.setRecipient(agent, null);
        agent.processCommands("/leave");
        received = clientConnection.recieveSingleMessage();
        assertEquals("неправильно обрабатывает команду /leave","/left",received.substring(0,5));
        agent.processCommands("/exit");
        received = clientConnection.recieveSingleMessage();
        assertEquals("неправильно обрабатывает команду /exit","/out",received.substring(0,4));
    }
*/

    @Test
    public void onConnectionReady() {
        agent.onConnectionReady(serverConnection);
        try {
            received = clientConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("не высылает сообщение о регистрации","Client connected: "+serverConnection, received);
    }

    /*@Test
    public void onRegistration() throws IOException{
        clientConnection.sendMessage("Vasia");
        clientConnection.sendMessage("agent");
        agent.onRegistration(serverConnection);

        try {
            received = clientConnection.recieveSingleMessage();
            received = clientConnection.recieveSingleMessage();
            received = clientConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("проблемы с регистрацией", "Registration completed. Good day Vasia", received);
    }*/

    /*@Test
    public void onReciveMessage() {
        agent.setHandlingUser(serverConnection);
        agent.onReciveMessage(serverConnection, "hello");
        try {
           received = clientConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("проблемы с отправкой сообщений", "  : hello", received);
    }*/

    @Test
    public void onDisconnect() {
        agent.setHandlingUser(serverConnection);
        agent.onDisconnect(serverConnection);
        assertEquals("не обнуляет собственное соединение", agent.getHandlingUser(), null);
    }


    Thread testThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                clientConnection = new SocketConnection(new ClientHandler(new Socket(), 1),"127.0.0.1", 5001 );
            } catch (IOException e) {}
            catch (InterruptedException e){}

        }
    });
}