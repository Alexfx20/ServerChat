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
    Scanner sc;
    String name;
    String status;


    public static void main(String[] args) {
        new Client();

    }

    public Client() {
        try {
            sc=new Scanner(System.in);

            this.connection = new SocketConnection(this, IP,PORT);
            this.onConnectionReady(connection);
            this.onRegistration(connection);
            connection.startNewChat();//слушаем входящие
            transmitMessage();//передаем исходящие
        } catch (IOException e) {
            System.out.println("Connection Exception: "+e);
        }

    }
//реализация методов интерфейса ConnectionListener
    @Override
    public void onConnectionReady(Connection connection) {
        try{
        if(connection!=null)
            System.out.println(connection.recieveSingleMessage());
        else System.out.println("Server is unavailable, try again later");}
        catch(IOException e){e.printStackTrace();}
    }

    @Override
    public void onRegistration(Connection connection) {
        try{

                System.out.print(connection.recieveSingleMessage()+" ");
                name = sc.nextLine();
                connection.sendMessage(name);
                System.out.print(connection.recieveSingleMessage()+" ");
                status = sc.nextLine();
                connection.sendMessage(status);


        }
        catch(IOException e){
            System.out.println("Something bad happening...");
        }

    }

    @Override
    public void onReciveMessage(Connection connection, String value) {

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
       if(value.startsWith("/left")){//собеседник закончил беседу
            connection.sendMessage("/endOfChat");
            value = value.substring(6,value.length())+" ended the conversation";
        }
        else if (value.startsWith("/out")){//собеседник закрыл программу
           connection.sendMessage("/endOfChat");
           value = value.substring(5,value.length())+" exit the program";
       }
        else if(value.startsWith("/Timeout")){          //произошел TimeOut
           printMessage("You have just exceeded latency time");
           value = "Press ENTER to exit the program";
           isOnline = false;
           onDisconnect(connection);
        }
        else if (value.equals("/access denied")){       //неправильные регистрационные параметры
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

    public void processSelfCommand(String value){//для прерывания цикла в методе transmitMessage и отсылки сообщений на сервер
        connection.sendMessage(value);
        if(value.equals("/exit")){
            isOnline = false;
        }
    }

    private synchronized void printMessage(String message){
        System.out.println(message);
    }

    private void transmitMessage(){
        while(isOnline){
            processSelfCommand(sc.nextLine());
        }
    }
}
