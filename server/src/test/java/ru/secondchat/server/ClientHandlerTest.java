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
    String AgentName = "Vasia";
    String AgentStatus = "agent";

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

    @Test
    public void setRecipient() {
        agent.setRecipient(agent, clientConnection);
        assertEquals("ошибка при установке сокета получателя", clientConnection, agent.getRecipient());
    }

    @Test
    public void getRecipient() {

        agent.setRecipient(agent, clientConnection);
        assertEquals("ошибка при получении сокета получателя", clientConnection, agent.getRecipient());
    }

    @Test
    public void getFirstMessage() throws IOException {
        register(AgentStatus, AgentName);
        agent.onReciveMessage(agent.getHandlingUser(), "hello");
        assertEquals("Неправильно возвращает первое сообщение", "hello",agent.getFirstMessage());
    }


    @Test
    public void processCommands() throws IOException {
        register(AgentStatus, AgentName);
        agent.setHandlingUser(serverConnection);
        agent.setRecipient(agent, agent.getHandlingUser());
        garbageMessages();
        agent.processCommands("/leave");
        received = clientConnection.recieveSingleMessage();
        assertEquals("неправильно обрабатывает команду /leave","/left",received.substring(0,5));
        agent.processCommands("/exit");
        received = clientConnection.recieveSingleMessage();
        assertEquals("неправильно обрабатывает команду /exit","/out",received.substring(0,4));
    }

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

    @Test
    public void onRegistration() {
        register(AgentName, AgentStatus);

        try {
            garbageMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("проблемы с регистрацией", "Registration completed. Good day Vasia", received);
    }

    @Test
    public void onReciveMessage() {
        agent.setHandlingUser(serverConnection);
        register(AgentName, AgentStatus);

        agent.onReciveMessage(serverConnection, "hello");
        try {

               garbageMessages();
               received = clientConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("проблемы с отправкой сообщений", AgentStatus+" "+AgentName+" : hello", received);
    }

    @Test
    public void onDisconnect() {
        register(AgentStatus, AgentName);
        agent.setHandlingUser(serverConnection);
        agent.onDisconnect(serverConnection);
        assertEquals("не обнуляет собственное соединение", agent.getHandlingUser(), null);
    }


    Thread testThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                clientConnection = new SocketConnection(new Socket("127.0.0.1", 5001) );
            } catch (IOException e) {}
            catch (InterruptedException e){}

        }
    });

    private void register(String Name, String Status) { //т.к. поле User используется в методах класса ClientHandler, оно должно быть инициализировано
        clientConnection.sendMessage(Name);//инициализация данного поля происходит только в методе onRegistration
        clientConnection.sendMessage(Status);
        agent.onRegistration(serverConnection);

    }
        private void garbageMessages() throws IOException{//данный метод считывает сообщения, присылаемые сервером во время регистрации

                clientConnection.recieveSingleMessage();
                clientConnection.recieveSingleMessage();
                received = clientConnection.recieveSingleMessage();
            }
}