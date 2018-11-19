package ru.secondchat.web;

import ru.secondchat.network.Commands;
import ru.secondchat.network.Connection;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.RestConnection;
import ru.secondchat.server.Server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class RestClientsHandler implements ConnectionListener {

  private static Map<String, RestClientsHandler> restClients = new ConcurrentHashMap<>();

  private LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>(500);
  private RestConnection restConnection;
  private String id;


    public RestClientsHandler() {
    }

    String register(String id, String name, String status, int numOfMaxConnections){
        if (!restClients.isEmpty()&&restClients.containsKey(id)){
            return response("id: "+id+" has already exists, try another one", "null");
        }
        this.id = id;
        String registrParams = getRegistrationMessage(name, status, numOfMaxConnections);
        restConnection = new RestConnection(this);
        restConnection.addMessage(registrParams);
        try {
            Server.addConnections(this.restConnection);
        } catch (InterruptedException e) {
            onException(restConnection, e);
            return  response("Fail to register", id);
        }
        restClients.put(id, this);

        return response("success", id);
    }

    String response(String message, String id){
        String lineseparator = System.getProperty("line.separator");
        StringBuilder bd = new StringBuilder();
        bd.append("User:{");
        bd.append(lineseparator);
        bd.append("id: ");
        bd.append(id);
        bd.append(lineseparator);
        bd.append("message: ");
        bd.append(message);
        bd.append(lineseparator);
        bd.append("}");


        return bd.toString();
    }

    String getRegistrationMessage(String name, String status, int numOfMaxConnections){
        StringBuilder bd = new StringBuilder(Commands.REGISTER.getCommand()+" ");
        bd.append(name+" ");
        bd.append(status);
        String numOfConns = numOfMaxConnections==0 ? "$1" : ("$"+numOfMaxConnections);
        bd.append(numOfConns);
        System.out.println(bd.toString());
        return bd.toString();
    }

    String quit(){
        restConnection.addMessage(Commands.EXIT.getCommand());

        return response("Good Bye", this.id);
    }

    String sendMessage(String message){
        String  resp = "failure";
        if(message!=null){
            resp = restConnection.addMessage(message);
        }
        return response(resp, id);
    }
    //метод получения сообщений
    String[] seeAllReceivedMessages(){
        String[] allReceivedMessages = messages.toArray(new String [0]);//toArray потокобезопасный

        return allReceivedMessages;
    }

    static RestClientsHandler getInstance(String id){

        return restClients.get(id);
    }

    RestConnection getConnection(){
        return this.restConnection;
    }







    @Override
    public void onConnectionReady(Connection connection) {

    }

    @Override
    public void onRegistration(Connection connection) throws IOException {

    }

    @Override
    public void onReciveMessage(String value) {
        if(value!=null){
            if(messages.size()>=499)
                messages.pollLast();
            if(value.startsWith(Commands.COMMAND_IDENTIFIER.getCommand()))
                processCommands(value);
            else
                messages.offerFirst(value);
        }
    }

    @Override
    public void onDisconnect(Connection connection) {
        restClients.remove(id);
    }

    @Override
    public void onException(Connection connection, Exception e) {
        restConnection.addMessage(Commands.EXIT.getCommand());

    }

    @Override
    public void processCommands(String value) {
        if (value.startsWith(Commands.LEFT.getCommand())) {
            restConnection.addMessage(Commands.END_OF_CHAT.getCommand());
            value = "User ended the conversation: "+value.substring(Commands.LEFT.getCommand().length()+1, value.length());
        }
        else if (value.startsWith(Commands.OUT.getCommand())) {
            restConnection.addMessage(Commands.END_OF_CHAT.getCommand());
            value = "User exit the program: "+value.substring(Commands.OUT.getCommand().length()+1, value.length());
        }
        else if (value.startsWith(Commands.TIME_OUT.getCommand())) {
            value = "You have just exceeded latency time ";
        }
        else if (value.equals(Commands.ACCESS_DENIED.getCommand())) {
            value = "Access denied. Wrong registration parameters";
        }
        else if (value.equals(Commands.EXIT.getCommand())) {
            value = "Good Bye";
        }
        messages.offerFirst(value);
    }
}
