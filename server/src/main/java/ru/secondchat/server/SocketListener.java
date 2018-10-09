package ru.secondchat.server;

import ru.secondchat.network.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//слушает сокетный порт создает SocketConnection и отдает его в очердь Server.connections
public class SocketListener implements Runnable {

    private int PORT;

    public SocketListener(int PORT) {
        this.PORT = PORT;
    }

    @Override
    public void run() {
        System.out.println("SocketListener has Started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {//слушаем соединение
            while(!Server.isIsshutDown()) {                     //пока сервер работает получаем сокет, создаем Connection и отправляем его в очердь в Server.connections

                Socket socket = serverSocket.accept();

               try{
                Server.connections.put(new SocketConnection(socket));}
                catch(InterruptedException e){
                   Server.rootLogger.error("InterruptedException in SocketListener");
                }
            }


        } catch (IOException e) {
            Server.rootLogger.error("InterruptedException in SocketListener");
        }

    }
}

