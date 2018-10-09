package ru.secondchat.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class SwitcherTest {

    Switcher switcher;
    //Server server;
    ClientHandler agent;
    ClientHandler client;

    @Before
    public void setUp() throws Exception {
//       server = new Server();
        switcher = new Switcher();
        agent = new ClientHandler(new Socket(), 1);
         client = new ClientHandler(new Socket(), 2);


        Server.agents.put(agent);
        Server.customers.put(client);


    }

    @After
    public void tearDown() throws Exception {
     //   server = null;
        switcher = null;
        agent = null;
        client = null;

        }

    @Test
    public void run() {

        try {
            switcher.start();
            Thread.sleep(1000);
            switcher.interrupt();
        } catch (InterruptedException e) {

        }


        assertEquals("wrong set of recipient", agent.getHandlingUser(),client.getRecipient());
        assertEquals("wrong set of recipient", client.getHandlingUser(),agent.getRecipient());
    }
}