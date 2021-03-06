package ru.secondchat.web;

import ru.secondchat.network.Commands;
import ru.secondchat.network.Connection;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.WebSocketConnection;
import ru.secondchat.server.Server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


@ServerEndpoint(value = "/ru/secondchat/web")
public class WebSocketListener implements ConnectionListener {
    private Session session;
    private WebSocketConnection connection;



    @OnOpen
    public void onOpen(Session session){
        this.session = session;         //сохранили сессию
        this.connection = new WebSocketConnection(this, session); //создали webconnection
        try {
            Server.addConnections(this.connection);     //попробовали добавить в очередь
        } catch (InterruptedException e) {
            onException(connection, e);     // есил не получилось залогировали исключение
        }


    }

    @OnClose
    public void onClose() {
        System.out.println("Closing connection");

        try {
            connection = null;
            session.close();
        } catch (IOException e) {

           onException(connection, e);
        }

    }

    @OnError
    public void onError(Throwable error){

        error.printStackTrace();

        connection.addMessage(Commands.EXIT.getCommand());


    }

    @OnMessage
    public void onMessage(String msg){
        connection.addMessage(msg);
    }




    @Override
    public void onDisconnect(Connection connection) {
        onClose();

    }

    @Override
    public void onException(Connection connection, Exception e) {
        this.connection.addMessage(Commands.EXIT.getCommand());
        e.printStackTrace();


    }

    @Override
    public void processCommands(String value) {


            if (value.startsWith(Commands.LEFT.getCommand())) {
                connection.addMessage(Commands.END_OF_CHAT.getCommand());
                value = "User ended the conversation: "+value.substring(Commands.LEFT.getCommand().length()+1, value.length());
            }
            else if (value.startsWith(Commands.OUT.getCommand())) {
                connection.addMessage(Commands.END_OF_CHAT.getCommand());
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
        try {
            session.getBasicRemote().sendText(value);
        } catch (IOException e) {
                onException(this.connection, e);
            e.printStackTrace();
        }
    }


    // наследуемые но не используемые методы от ConnectionListener
    @Override
    public void onConnectionReady(Connection connection) {

    }

    @Override
    public void onRegistration(Connection connection) {

    }

    @Override
    public void onReciveMessage(String value) {

    }

}
