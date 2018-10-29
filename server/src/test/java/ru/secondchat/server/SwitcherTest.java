package ru.secondchat.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.secondchat.network.WebSocketConnection;

import static org.junit.Assert.assertEquals;

public class SwitcherTest {

    Switcher switcher;
    //Server server;
    ClientHandler agent;
    ClientHandler client;

    @Before
    public void setUp() throws Exception {
//       //server = new Server();
        switcher = new Switcher();
        TestSocketConnection user = new TestSocketConnection();
        user.addMessage("/register Smith agent");
        user.addMessage("/register Dusia client");
        agent = new ClientHandler(user, 1);
        agent.onRegistration(user);

        client = new ClientHandler(user, 2);
        client.onRegistration(user);
        Server.runServer();// организовать корректное завершение потока switcher
            switcher.start();

    }

    @After
    public void tearDown() throws Exception {
     //   server = null;
        Server.shutDown();//теперь все реализовано в одном методе
        //switcher.BARRIER.await(1, TimeUnit.SECONDS);//вызвать await чтобы switcher увидел изменившийся флаг

        switcher = null;
        Server.customers.remove(client);
        Server.agents.remove(agent);
        agent = null;
        client = null;

        }

    @Test
    public void run() throws Exception{
        agent.getUser().setReadyToChat(true);
        client.getUser().setReadyToChat(true);// рассмотреть случаи с несколькими юзерами и при флаге readytochatfalse false

        Server.agents.put(agent);
        Server.customers.put(client);
        switcher.BARRIER.await();
            Thread.sleep(1000);

        assertEquals("wrong set of recipient", agent.getHandlingUser(),client.getRecipient());
        assertEquals("wrong set of recipient", client.getHandlingUser(),agent.getRecipient());
    }

    @Test
    public void runSecondParameters() throws Exception {
        agent.getUser().setReadyToChat(true);
        client.getUser().setReadyToChat(true);// рассмотреть случаи с несколькими юзерами и при флаге readytochatfalse false
        System.out.println("заходим в метод");
        agent.getUser().setReadyToChat(true);
        System.out.println("клиент устанавливаем флаг");
        client.getUser().setReadyToChat(false);
        System.out.println("кладем агента");
        Server.agents.put(agent);
        System.out.println("кладем клиента");
        Server.customers.put(client);
        switcher.BARRIER.await();
        Thread.sleep(1000);
        assertEquals("if client isn't ready to chat agent must be returned to the queue", agent.getUser().isInTheQueue(),true);
        assertEquals("assighment of recipient when client flag isReadyTochat false", client.getRecipient(),null);
        assertEquals("assighment of recipient when client flag isReadyTochat false", agent.getRecipient(),null);
        agent.getUser().setReadyToChat(false);
        client.getUser().setReadyToChat(true);
        Server.customers.put(client);
        client.getUser().setInTheQueue(true);
        switcher.BARRIER.await();
        Thread.sleep(1000);
        assertEquals("if agent isn't ready to chat he mustn't be in the queue", agent.getUser().isInTheQueue(),false);
        assertEquals("if agent isn't ready to chat client must stay to the queue", client.getUser().isInTheQueue(),true);
        assertEquals("assighment of recipient when client flag isReadyTochat false", client.getRecipient(),null);

    }

    private static class TestSocketConnection extends WebSocketConnection{// mock connection for tests

        @Override
        public synchronized void sendMessage(String value) {
            System.out.println(value);
        }
    }
}