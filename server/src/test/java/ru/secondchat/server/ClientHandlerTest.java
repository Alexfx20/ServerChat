package ru.secondchat.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.secondchat.network.Commands;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.WebSocketConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ClientHandlerTest {

    private static List<String> storegeOfClientsSentMessages = new ArrayList<>();
    private static List<String> agentsSentMessages = new ArrayList<>();
    private ClientHandler client;
    private ClientHandler agent_supportsOnlyOneChat;
   private ClientHandler agent_supportsMultipalChats;
   private String agentName = "Smith";
   private String clientsName = "Dusia";


    private TestSocketConnection clientsConnection;
    private TestSocketConnection agentsConnection;
    private TestSocketConnection multipalAgent;


    @Before
    public void setUp() throws Exception {
        clientsConnection = new TestSocketConnection();
        agentsConnection = new TestSocketConnection();
        multipalAgent = new TestSocketConnection();
        client = new ClientHandler(clientsConnection, 0);
        agent_supportsOnlyOneChat = new ClientHandler(agentsConnection, 1);
        agent_supportsMultipalChats = new ClientHandler(multipalAgent, 2);
        clientsConnection.addMessage(Commands.REGISTER+" "+clientsName+" client");
        agentsConnection.addMessage(Commands.REGISTER+" "+agentName+" agent$1");
        multipalAgent.addMessage(Commands.REGISTER.getCommand()+" "+agentName+" agent$2");
        client.onRegistration(clientsConnection);
        agent_supportsOnlyOneChat.onRegistration(agentsConnection);
    }

    @After
    public void tearDown() {
        clientsConnection = null;
        agentsConnection = null;
        multipalAgent = null;
        clearTheMessagesStorege();
        client = null;
        agent_supportsOnlyOneChat = null;
        agent_supportsMultipalChats = null;

    }

    @Test
    public void setRecipient() throws IOException {
        assertNull("wrong initial parameters, recipient field must be empty", client.getRecipient());
        assertNull("wrong initial parameters, recipient field must be empty", agent_supportsOnlyOneChat.getRecipient());
        initializeClientAndAgent();
        assertEquals("wrong set of recipient", client.getRecipient(), agentsConnection);
        assertEquals("wrong set of recipient", agent_supportsOnlyOneChat.getRecipient(), clientsConnection);
        assertEquals("method getNewId (Agent.class) returns wrong parameters, or wrong set of chat's ID",client.getUser().getID(),"$0");
        assertEquals("method getNewId (Agent.class) returns wrong parameters, or wrong set of chat's ID", agent_supportsOnlyOneChat.getUser().getID(),"$0");
        client.setRecipient(null, null);
        agent_supportsOnlyOneChat.setRecipient(null, null);
        assertNull("wrong set of recipient for null parameter",client.getRecipient());
        assertNull("wrong set of recipient for null parameter", agent_supportsOnlyOneChat.getRecipient());

        initializeMultipuleAgent();
        assertNull("doesn't initialize recipientsMap",agent_supportsMultipalChats.getRecipient());
        assertTrue("doesn't initialize recipientsMap",agent_supportsMultipalChats.getUser().isFluded());
        agent_supportsMultipalChats.setRecipient(null, null);
        assertFalse("doesn't clean the map of recipients in Agent class", agent_supportsMultipalChats.getUser().isFluded());

    }

    @Test
    public void setHandlingUser() {
        assertEquals("wrong initialization of handling user during ClientHandler constraction",client.getHandlingUser(), clientsConnection);
        assertEquals("wrong initialization of handling user during ClientHandler constraction",agent_supportsOnlyOneChat.getHandlingUser(), agentsConnection);
        client.setHandlingUser(agentsConnection);
        agent_supportsOnlyOneChat.setHandlingUser(clientsConnection);
        assertEquals("doesn't set field \' Connection handling user\'", client.getHandlingUser(), agentsConnection);
        assertEquals("doesn't set field \' Connection handling user\'", agent_supportsOnlyOneChat.getHandlingUser(), clientsConnection);
    }

    @Test
    public void getUser() {
        assertEquals("doesn't return the User's field", client.getUser().getClass().getSimpleName(),"Customer" );
        assertEquals("doesn't return the User's field", agent_supportsOnlyOneChat.getUser().getClass().getSimpleName(),"Agent" );
    }

    @Test
    public void setFirstMessage() {
        client.setFirstMessage("Wrong bee's produce the wrong honney");
        agent_supportsOnlyOneChat.setFirstMessage("MATRIX");
        assertEquals("wrong set of FirstMessage field", client.getFirstMessage(), "Wrong bee's produce the wrong honney");
        assertEquals("wrong set of FirstMessage field", agent_supportsOnlyOneChat.getFirstMessage(), "MATRIX");
    }

    @Test
    public void processCommands() throws IOException {
        String ID_OF_Chat = initializeClientAndAgent();

        agent_supportsOnlyOneChat.processCommands(Commands.LEAVE.getCommand());
        assertFalse(client.getUser().isReadyToChat());
        assertFalse(agent_supportsOnlyOneChat.getUser().isReadyToChat());
        assertEquals(storegeOfClientsSentMessages.get(0), String.format("%s agent %s %s", Commands.LEFT.getCommand(), agentName, ID_OF_Chat));
        initializeMultipuleAgent();
        assertTrue("wrong initial test parameters",agent_supportsMultipalChats.getUser().isFluded());

        clearTheMessagesStorege();
        agent_supportsMultipalChats.processCommands(Commands.LEAVE_ALL.getCommand());
        assertTrue("must set PromptForRelax true", agent_supportsMultipalChats.getUser().isPromptForRelax());
        assertFalse("must set isReadyToChat false", client.getUser().isReadyToChat());
        assertFalse("must set isReadyToChat false", agent_supportsOnlyOneChat.getUser().isReadyToChat());

        assertEquals("doesn't notify about end of chat",storegeOfClientsSentMessages.get(0), String.format("%s agent %s",Commands.OUT.getCommand(),agentName));
        assertEquals("doesn't notify about end of chat",agentsSentMessages.get(0), String.format("%s agent %s",Commands.OUT.getCommand(),agentName));
        clearTheMessagesStorege();
        ID_OF_Chat = initializeClientAndAgent();
        client.processCommands(Commands.EXIT.getCommand());
        assertEquals("doesn't notify about exit of chat", agentsSentMessages.get(0),String.format("%s client %s %s",Commands.OUT.getCommand(), clientsName, ID_OF_Chat));
        assertFalse("must set isReadyToChat false", client.getUser().isReadyToChat());
        assertFalse("must set isReadyToChat false", agent_supportsOnlyOneChat.getUser().isReadyToChat());
        assertTrue("must set isReadyToChat false", client.getUser().isExit());

    }

    @Test
    public void onConnectionReady() {
        clearTheMessagesStorege();
        client.onConnectionReady(clientsConnection);
        assertEquals("doesn't notify about established connection","Client connected: "+clientsConnection, storegeOfClientsSentMessages.get(0) );
        agent_supportsOnlyOneChat.onConnectionReady(agentsConnection);
        assertEquals("doesn't notify about established connection","Client connected: "+agentsConnection, agentsSentMessages.get(0) );
    }

    @Test
    public void onRegistration() throws IOException {
        assertEquals("doesn't notifyabout registration process","REGISTRATION... ", storegeOfClientsSentMessages.get(0) );
        assertEquals("Registration completed. Good day "+clientsName, storegeOfClientsSentMessages.get(1));
        assertEquals("doesn't notify about established connection","REGISTRATION... ", storegeOfClientsSentMessages.get(2) );
        assertEquals("Registration completed. Good day "+agentName, agentsSentMessages.get(0));
        assertEquals("fail to initialize Client", client.getUser().getStatus(), "client");
        assertEquals("fail to initialize Agent", agent_supportsOnlyOneChat.getUser().getStatus(), "agent");
        assertEquals("wrong ID", client.getId(), 0);
        assertEquals("wrong ID", agent_supportsOnlyOneChat.getId(), 1);
        clientsConnection.addMessage("WRONG REGISTRATION PARAMETERS");
        client.onRegistration(clientsConnection);
        assertEquals("Wrong processing of false registraion parameters",Commands.ACCESS_DENIED.getCommand(), storegeOfClientsSentMessages.get(4) );
    }

    @Test
    public void onReciveMessage() throws IOException {
       String ID_OF_Chat = initializeClientAndAgent();
        client.onReciveMessage("Client message");
        agent_supportsOnlyOneChat.onReciveMessage("Agent message");
        String clientMessage = client.getUser().getStatus()+" "+client.getUser().getName()+" : Client message";
        String agentMessage = agent_supportsOnlyOneChat.getUser().getStatus()+" "+agent_supportsOnlyOneChat.getUser().getName()+" : Agent message";

        assertEquals(clientMessage, storegeOfClientsSentMessages.get(0));
        assertEquals(clientMessage+ID_OF_Chat, agentsSentMessages.get(0));
        assertEquals(agentMessage, storegeOfClientsSentMessages.get(1));
        assertEquals(agentMessage, agentsSentMessages.get(1));

        String[] IDs = initializeMultipuleAgent();
        clearTheMessagesStorege();
        agent_supportsMultipalChats.onReciveMessage("Hello to entolocutor # 1"+IDs[0]);
        agent_supportsMultipalChats.onReciveMessage("Hello to entolocutor # 2"+IDs[1]);
        System.out.println(agentsSentMessages.get(0)+" "+agentsSentMessages.get(1));
        System.out.println(storegeOfClientsSentMessages.size());
        assertEquals("Message redirection in multipalAgent mode acts wrong ","agent Smith : Hello to entolocutor # 1"+IDs[0], agentsSentMessages.get(0));
        assertEquals("Message redirection in multipalAgent mode acts wrong ","agent Smith : Hello to entolocutor # 1", agentsSentMessages.get(1));
        assertEquals("Message redirection in multipalAgent mode acts wrong ","agent Smith : Hello to entolocutor # 2", storegeOfClientsSentMessages.get(0));


    }

    @Test
    public void onDisconnect() {
        initializeClientAndAgent();
        agent_supportsOnlyOneChat.onDisconnect(agentsConnection);
        client.onDisconnect(clientsConnection);
        assertTrue("isExit must be set as true in disconnect method", agent_supportsOnlyOneChat.getUser().isExit());
        assertTrue("isExit must be set as true in disconnect method", client.getUser().isExit());
        assertNull(agent_supportsOnlyOneChat.getHandlingUser());
        assertNull(client.getHandlingUser());

    }

    @Test
    public void onException() {
        initializeClientAndAgent();
        assertEquals(agent_supportsOnlyOneChat.getUser().getRecipient(), clientsConnection);
        agent_supportsOnlyOneChat.onException(agentsConnection, new IOException());
        assertFalse(client.getUser().isReadyToChat());
        assertTrue(agent_supportsOnlyOneChat.getUser().isExit());

    }

    @Test
    public void stopClientHandlersThread() {
        agent_supportsOnlyOneChat.stopClientHandlersThread();
        assertFalse("isReadyToChat must be set as false", agent_supportsOnlyOneChat.getUser().isReadyToChat());
        assertTrue("isExit must be set as false", agent_supportsOnlyOneChat.getUser().isExit());
    }

    private static class TestSocketConnection extends WebSocketConnection {
        private ClientHandler eventListener;// mock connection for tests

        @Override
        public synchronized void sendMessage(String value) {
            if(eventListener!=null&&eventListener.getUser()!=null&&eventListener.getUser().getStatus().equals("agent"))agentsSentMessages.add(value);
            else storegeOfClientsSentMessages.add(value);
        }

        @Override
        public void setEventListener(ConnectionListener eventListener) {
            super.setEventListener(eventListener);
            this.eventListener = (ClientHandler)eventListener;
        }

        @Override
        public synchronized void disconnect() {
            storegeOfClientsSentMessages.add(this+" disconnected");
            agentsSentMessages.add(this+" disconnected");
        }
    }

    private String[] initializeMultipuleAgent() throws IOException {
        agent_supportsMultipalChats.onRegistration(multipalAgent);
        String ID1 = agent_supportsMultipalChats.getUser().getNewID();
        agent_supportsMultipalChats.setRecipient(agent_supportsOnlyOneChat, ID1);
        agent_supportsOnlyOneChat.setRecipient(agent_supportsMultipalChats,ID1);
        String ID2 = agent_supportsMultipalChats.getUser().getNewID();
        agent_supportsMultipalChats.setRecipient(client, ID2);
        client.setRecipient(agent_supportsMultipalChats, ID2);
        client.getUser().setReadyToChat(true);
        agent_supportsOnlyOneChat.getUser().setReadyToChat(true);
        String[] IDs = {ID1, ID2};
        return IDs;
    }

    private String initializeClientAndAgent(){

        String ID_OF_Chat = agent_supportsOnlyOneChat.getUser().getNewID();
        client.setRecipient(agent_supportsOnlyOneChat,ID_OF_Chat);
        agent_supportsOnlyOneChat.setRecipient(client,ID_OF_Chat);
        client.getUser().setReadyToChat(true);
        agent_supportsOnlyOneChat.getUser().setReadyToChat(true);
        clearTheMessagesStorege();

        return ID_OF_Chat;
    }

    private void clearTheMessagesStorege(){
        agentsSentMessages.clear();
        storegeOfClientsSentMessages.clear();
    }
}