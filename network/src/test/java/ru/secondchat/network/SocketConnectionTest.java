package ru.secondchat.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketConnectionTest {

    ServerSocket serverSocket;
    Socket server;
    volatile String received = "";
    String separatorMessage= "";
    Connection serverConnection;
    Connection clientConnection;

    @Before
    public void setUp() throws Exception {

        serverSocket = new ServerSocket(5001);
        testThread.start();
        server = serverSocket.accept();

        testThread.join();

        serverConnection = new SocketConnection(new TestEventListener(), server);

    }

    @After
    public void tearDown() throws Exception {
        serverConnection.disconnect();
        clientConnection.disconnect();
        server.close();
        serverSocket.close();
        received = null;
        testThread = null;

    }

    @Test
    public void startNewChat() {
        clientConnection.startNewChat();


        serverConnection.sendMessage("hello");
        try {
            Thread.sleep(100);//задержка выставлена т.к. прием сообщения осуществляется в другом потоке
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals("Parralel thread doesn'r receive message on the client side", "hello", received);
        assertTrue("line separator missed at the end of each string", separatorMessage.equals(""));
        clientConnection.disconnect();
    }

    @Test
    public void sendMessage() {

        clientConnection.sendMessage("hello");
        try {
            received = serverConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("server doesn't receive client messages", "hello",received);

    }

    @Test
    public void recieveSingleMessage() {

        serverConnection.sendMessage("test");
        try {
            received = clientConnection.recieveSingleMessage();
        } catch (IOException e) {}

        assertEquals("client doesn't receive agent messages", "test",received);
    }

    @Test
    public void disconnect() {
        clientConnection.disconnect();
        clientConnection.sendMessage("hello");
        try {
         received = serverConnection.recieveSingleMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("couldn't close the socket", received, null);


    }

    private class TestEventListener implements ConnectionListener {
        @Override
        public void onConnectionReady(Connection connection) {

        }

        @Override
        public void onRegistration(Connection connection) {

        }

        @Override
        public void onReciveMessage(String value) {

            //value!=null&&!value.equals(""))//sendMessage отсылает вслед за value еще пустую строку, что может приводить к затеранию value в тестах
                received = value;//кроме того т.к. присвоение значения received осуществляется в другом потоке, проверка assert может выполняться раньше чем
                                    // произойдет присвоение, поэтому здесь выставлно ожидание в 100 мс в основном тестовом потоке.

            /*else {separatorMessage = value;
                System.out.println("separatorMessage = " +value);}*/

        }

        @Override
        public void onDisconnect(Connection connection) {

        }

        @Override
        public void onException(Connection connection, Exception e) {
            //e.printStackTrace();

        }

        @Override
        public void processCommands(String value) {

        }
    }
    Thread testThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
                clientConnection = new SocketConnection(new TestEventListener(),"127.0.0.1", 5001 );
            } catch (IOException e) {}
            catch (InterruptedException e){}

        }
    });
}