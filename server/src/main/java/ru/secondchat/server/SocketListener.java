package ru.secondchat.server;

import ru.secondchat.network.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//слушает сокетный порт создает SocketConnection и отдает его в очердь
public class SocketListener implements Runnable {

    static ServerSocket serverSocket;
    private int PORT;


    public SocketListener(int PORT) {
        this.PORT = PORT;
    }

    @Override
    public void run() {
        System.out.println("SocketListener has Started");
        Server.rootLogger.info("SocketListener thread running...");

        try {//слушаем соединение
            serverSocket = new ServerSocket(PORT);
            while(!Server.IsshutDown()) {                     //пока сервер работает получаем сокет, создаем Connection и отправляем его в очердь в Server.connections

                Socket socket = serverSocket.accept();
                System.out.println("new Connection request");
               try{
                Server.connections.put(new SocketConnection(socket));}//создает SocketConnection и ложит в очердь
                catch(InterruptedException e){
                   Server.rootLogger.error("InterruptedException in SocketListener "+e);
                }
            }


        } catch (IOException e) {
            Server.rootLogger.error("Exception in SocketListener "+e);
        }

    }

    static void closeServerSocket(){
        try {
            if(serverSocket!=null)
            serverSocket.close();
        } catch (IOException e) {
            Server.rootLogger.info("Shutting down SocketListener Thread...");
        }

    }
}

