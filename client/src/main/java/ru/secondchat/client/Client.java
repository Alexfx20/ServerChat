package ru.secondchat.client;


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

    @Override
    public void onConnectionReady(Connection connection) {
        try{
        if(connection!=null)
            System.out.println(connection.recieveSingleMessage());
        else System.out.println("Server is unavailable, try again later");}
        catch(IOException e){
            System.out.println("Server refused to provide a connection");
        }

    }

    @Override
    public void onRegistration(Connection connection) throws IOException{
        //try{

                System.out.print(connection.recieveSingleMessage()+" ");
                if(!regParams.isEmpty())
                connection.sendMessage(regParams);


        //}
        /*catch(IOException e){
            System.out.println("Something bad happening...");
        }*/

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
       if(value.startsWith("/left")){
            connection.sendMessage("/endOfChat");
           System.out.println(value);
            value = value.substring(6,value.length())+" ended the conversation";
           System.out.println(value);
        }
        else if (value.startsWith("/out")){
           connection.sendMessage("/endOfChat");
           System.out.println(value);
           value = value.substring(5,value.length())+" exit the program";
           System.out.println(value);

       }
        else if(value.startsWith("/Timeout")){
           printMessage("You have just exceeded latency time");
           value = "Press ENTER to exit the program";
           isOnline = false;
           onDisconnect(connection);
        }
        else if (value.equals("/access denied")){
           printMessage("Access denied. Wrong registration parameters: "+status+" "+name);
           isOnline = false;
           value = "press ENTER to exit the program";

           onDisconnect(connection);


       }
       else if (value.equals("/exit")){
           onDisconnect(connection);
           value =" Good Bye "+ name;
       }
        printMessage(value);

    }

    public void processSelfCommand(String value){
        connection.sendMessage(value);
        if(value.equals("/exit")){
            isOnline = false;
        }


    }



    private synchronized void printMessage(String message){
        if(status.equals("agent")){
            int indexOfID = message.lastIndexOf("$");
            if(indexOfID!=-1)
            message=message.substring(0,indexOfID);
        }
        System.out.println(message);

    }

    private void transmitMessage(){

        while(isOnline){
            processSelfCommand(sc.nextLine());
        }
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
                System.out.println("Connection Exception: " + e);
            }
        }
    }


    private boolean innerRegistration(){
        regParams="/register ";
        System.out.println("Please enter your name: ");
        name = sc.nextLine();
        if(!name.isEmpty()){
           int forbidenSlash = name.indexOf(" ");
           int forbidenAmpersand = name.indexOf("$");
           while(forbidenAmpersand!=-1||forbidenSlash!=-1){
               System.out.println("space characters and ampersands '$' are not allowed in Name");
               System.out.println("try again or type /exit to exit the program");
               name=sc.nextLine();
               forbidenSlash = name.indexOf(" ");
               forbidenAmpersand = name.indexOf("$");
           }
           if(name.equals("/exit")) return false;
           else regParams +=name+" ";
        }
        System.out.println("Please enter your status (client or agent): ");
        status=sc.nextLine();
        regParams +=status;

        return true;
    }


}
