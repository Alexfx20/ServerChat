package ru.secondchat.client;


import ru.secondchat.network.Commands;
import ru.secondchat.network.Connection;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.SocketConnection;

import java.io.IOException;
import java.util.Scanner;

public class Client implements ConnectionListener {

    private static final String IP = "127.0.0.1";
    private static final int PORT = 5000;
    private static Connection connection;
    private static boolean isOnline = true;
    private Scanner sc;
    private String name;
    private String status;
    private String regParams;


    public static void main(String[] args) {
        new Client().go();

    }

    public Client() {
            sc=new Scanner(System.in);
    }

    private void go(){
        if(innerRegistration()){
            try {
                this.connection = new SocketConnection(this, IP, PORT);
                this.onConnectionReady(connection);
                this.onRegistration(connection);
                connection.startNewChat();
                transmitMessage();
            } catch (IOException e) {
                printMessage("Connection Exception: " + e);
            }
        }
    }

    @Override
    public void onConnectionReady(Connection connection) {
        try{
        if(connection!=null)
           printMessage(connection.recieveSingleMessage());
        else System.out.println("Server is unavailable, try again later");}
        catch(IOException e){
            printMessage("Server refused to provide a connection");
        }

    }

    @Override
    public void onRegistration(Connection connection) throws IOException{

                printMessage(connection.recieveSingleMessage()+" ");
                if(!regParams.isEmpty())
                connection.sendMessage(regParams);

    }

    @Override
    public void onReciveMessage(String value) {

        processCommands(value);

    }

    @Override
    public void onDisconnect(Connection connection) {
        printMessage("You've been disconnected");
        connection.disconnect();

    }

    @Override
    public void onException(Connection connection, Exception e) {
        System.out.println("Connection Exception: "+e);

    }

    @Override
    public void processCommands(String value) {
       if(value.startsWith(Commands.LEFT.getCommand())){
            connection.sendMessage(Commands.END_OF_CHAT.getCommand());

            value = value.substring(6,value.length())+" ended the conversation";
            value = filterCommandMessages(value);
        }
        else if (value.startsWith(Commands.OUT.getCommand())){
           connection.sendMessage(Commands.END_OF_CHAT.getCommand());

           value = value.substring(Commands.OUT.getCommand().length(),value.length())+" exit the program";
            value = filterCommandMessages(value);

       }
        else if(value.startsWith(Commands.TIME_OUT.getCommand())){
           printMessage("You have just exceeded latency time");
           value = "Press ENTER to exit the program";
           isOnline = false;
           onDisconnect(connection);
        }
        else if (value.equals(Commands.ACCESS_DENIED.getCommand())){
           printMessage("Access denied. Wrong registration parameters: "+status+" "+name);
           isOnline = false;
           value = "press ENTER to exit the program";
           onDisconnect(connection);
       }
       else if (value.equals(Commands.EXIT.getCommand())){
           onDisconnect(connection);
           value =" Good Bye "+ name;
       }
        printMessage(value);

    }

    public void processSelfCommand(String value){
        connection.sendMessage(value);
        if(value.equals(Commands.EXIT.getCommand())){
            isOnline = false;
        }
    }

    private void transmitMessage(){

        while(isOnline){
            processSelfCommand(sc.nextLine());
        }
    }

    private synchronized void printMessage(String message){
        if(status.equals("agent")){
           message = filterAgentsMessages(message);
        }

        System.out.println(message);

    }

    private String filterCommandMessages(String message){
        int indexOfID = message.lastIndexOf("$");
        if(indexOfID!=-1){
            int indOfExit = message.lastIndexOf("exit the program");
            if(indOfExit ==-1) indOfExit = message.lastIndexOf("ended the conversation");
            if((indOfExit!=-1)&&(indOfExit-indexOfID == 3)){
                StringBuilder str = new StringBuilder(message);
                str.deleteCharAt(indexOfID);
                str.deleteCharAt(indexOfID);
                message=str.toString();
            }
        }

        return message;
    }

    private String filterAgentsMessages(String message){
        int indexOfID = message.lastIndexOf("$");
        if(indexOfID!=-1){
                message=message.substring(0,indexOfID);
        }
        return message;
    }

    private boolean innerRegistration(){
        regParams=Commands.REGISTER.getCommand()+" ";
        System.out.println("Please enter your name: ");
        name = sc.nextLine();
        if(!name.isEmpty()){
           int forbidenSlash = name.indexOf(" ");
           int forbidenAmpersand = name.indexOf("$");
           while(forbidenAmpersand!=-1||forbidenSlash!=-1){
               System.out.println("space characters and ampersands '$' are not allowed in Name");
               System.out.println(String.format("try again or type %s to exit the program", Commands.EXIT.getCommand() ));
               name=sc.nextLine();
               forbidenSlash = name.indexOf(" ");
               forbidenAmpersand = name.indexOf("$");
           }
           if(name.equals(Commands.EXIT.getCommand())) return false;
           else regParams +=name+" ";
        }
        System.out.println("Please enter your status (client or agent): ");
        status=sc.nextLine();
        regParams +=status;

        return true;
    }
}
