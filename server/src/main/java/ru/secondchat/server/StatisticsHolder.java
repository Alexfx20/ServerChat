package ru.secondchat.server;

import ru.secondchat.user.Chat;
import ru.secondchat.user.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class StatisticsHolder {

    static Map<Long, ClientHandler> allAgents = new ConcurrentHashMap<>();
    static Map<Long, Chat> chats = new ConcurrentHashMap<>();
    static Map<Long, ClientHandler> allClients = new ConcurrentHashMap<>();

    private static StatisticsHolder statisticsHolder ;

    private StatisticsHolder() {}

    public static StatisticsHolder getInstance(){
        if(statisticsHolder==null){
            statisticsHolder = new StatisticsHolder();
        }
        return statisticsHolder;
    }

    public static Map<Long, ClientHandler> getAgents() {

        return allAgents;
    }


    public Map<Long, Chat> getChats() {
        return chats;
    }

    public LinkedBlockingQueue<ClientHandler> getFreeagents() {
        return Server.getAgents();
    }

    public LinkedBlockingQueue<ClientHandler> getFreeclients() {
        return Server.getCustomers();
    }

    public User getAgentByID(long id){
        ClientHandler tmp = allAgents.get(id);
        if(tmp==null)return null;
        return tmp.getUser();
    }
    public Chat getChatByID(long id){
        Chat tmp = chats.get(id);
        if(tmp==null)return null;
        return tmp;
    }
    public User getClientByID(long id){
        Object[] clients = Server.getCustomers().toArray();
        for (Object client:clients) {
            ClientHandler tmp = (ClientHandler)client;
            if(id==tmp.getId()){
                return tmp.getUser();
            }
        }
        return null;
    }

    public static void removeChat(long id){
        if (chats.containsKey(id)) System.out.println("Карта содержит нужный чат");
        else System.out.println("Чат потерялся");
        chats.remove(id);

    }




}
