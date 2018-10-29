package ru.secondchat.user;

import ru.secondchat.server.ClientHandler;


public class Chat {

    long chatID;
    ClientHandler agent;
    ClientHandler client;

    public Chat() {}

    public Chat(ClientHandler agent, ClientHandler client) {
        this.chatID = agent.getId();
        this.agent = agent;
        this.client = client;
    }

    public long getId() {
        return chatID;
    }
   // @XmlElement
    public void setId(long id) {
        this.chatID = id;
    }

    public ClientHandler getAgent() {
        return agent;
    }
   // @XmlElement
    public void setAgent(ClientHandler agent) {
        this.agent = agent;
    }

    public ClientHandler getClient() {
        return client;
    }
   // @XmlElement
    public void setClient(ClientHandler client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "agent= "+agent.toString()+" client= "+client.toString();
    }
}
