package ru.secondchat.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class SocketConnection implements Connection {

    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final PrintWriter out;
    private ConnectionListener eventListener;

    public SocketConnection(ConnectionListener eventListener, String ip, int port) throws IOException {//конструктор для консольного клиента
        this(eventListener, new Socket(ip, port));
    }

    public SocketConnection(ConnectionListener event, Socket socket) throws IOException {//конструктор для сервера
        this(socket);
        this.eventListener = event;
    }

    public SocketConnection(Socket socket) throws IOException{ //Basic constructor.
        this.socket=socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);

        rxThread = new Thread(new Runnable() {//данный поток позволяет параллельно прослушивать входящие сообщения в консольном клиенте
            @Override
            public void run() {
                try{
                    while(!rxThread.isInterrupted()){
                        try{

                        eventListener.onReciveMessage(SocketConnection.this, SocketConnection.this.recieveSingleMessage());}
                        catch(SocketTimeoutException e){
                            System.out.println("TimeOut Exception in rxThread");
                        }
                    }

                } catch(IOException e){
                    eventListener.onException(SocketConnection.this, e);
                    System.out.println("IOException in rxThread");

                }

            }
        });
    }

    public void startNewChat(){
        rxThread.start();
    }

    public synchronized void sendMessage(String value){
        if(value!=null){
        out.print(value);
        out.println();}
        else {eventListener.onException(SocketConnection.this, new IOException());
            System.out.println("Exception in sendMessage method");
        }
    }

    public String recieveSingleMessage()throws IOException{

        return in.readLine();
    }

    public synchronized void disconnect(){
        if(!rxThread.isInterrupted())
        rxThread.interrupt();
        try {
            if (socket.isConnected())
            socket.close();
        } catch (IOException e) {
            System.out.println("Exception in disconnect method");
            eventListener.onException(SocketConnection.this, e);
        }

    }

    @Override
    public String toString() {
        return "TCP Connection: "+socket.getInetAddress()+": "+ socket.getPort();
    }

    public void setEventListener(ConnectionListener eventListener) {//settingEventListener
        this.eventListener = eventListener;
    }

    @Override
    public void setSoTimeout(int idleTime) {
        try {
            socket.setSoTimeout(idleTime);
        } catch (SocketException e) {
            eventListener.onException(this, e);
        }

    }
}
