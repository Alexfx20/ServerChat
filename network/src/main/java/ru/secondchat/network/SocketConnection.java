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

    public SocketConnection(ConnectionListener eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip, port));
    }

    public SocketConnection(ConnectionListener event, Socket socket) throws IOException {
        this(socket);
        this.eventListener = event;
    }

    public SocketConnection(Socket socket) throws IOException{ //Basic constructor.
        this.socket=socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    while(!rxThread.isInterrupted()){
                        try{

                        eventListener.onReciveMessage(SocketConnection.this.recieveSingleMessage());}
                        catch(SocketTimeoutException e){
                            System.out.println("TimeOut occured");
                        }
                    }

                } catch(IOException e){
                    eventListener.onException(SocketConnection.this, e);
                    System.out.println("Exception in eventListener connectionThread: thread was interrupted, while waiting for incoming messages");

                }

            }
        });
    }

    public void startNewChat(){
        rxThread.start();
    }

    public synchronized void sendMessage(String value){
        if(value!=null){
           /* if(socket.isClosed()) System.out.println("CLOSED");*/
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
        /*if(rxThread.isInterrupted())
        System.out.println("Thread stoped");*/
        try {
            if (socket.isConnected()){

                socket.close();}


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
    public synchronized void setSoTimeout(int idleTime) {
        try {
            socket.setSoTimeout(idleTime);
        } catch (SocketException e) {
            eventListener.onException(this, e);
        }

    }
}
