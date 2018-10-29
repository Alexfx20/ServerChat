package ru.secondchat.network;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RestConnection implements Connection {

    private ConnectionListener eventListener;
    private ConnectionListener restClient;
    private LinkedBlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>(100);
    private int timeOut = 300;


    public RestConnection(ConnectionListener restClient) {
        this.restClient = restClient;
    }

    @Override
    public void startNewChat() {

    }

    @Override
    public synchronized void sendMessage(String value) {
        restClient.onReciveMessage(value);

    }

    @Override
    public String recieveSingleMessage() throws IOException {
        String msg="";

        try {
            msg = receivedMessages.poll(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
           eventListener.onException(this, e);
        }
        if(msg==null){
            throw new SocketTimeoutException();}


        return msg;
    }

    public String addMessage(String message){   //добавляем сообщения в очередь
        try {
            receivedMessages.put(message);
            return "success";
        } catch (InterruptedException e) {
            eventListener.onException(this, e);
            return "failure";//логирование в дочернем методе
        }
    }

    @Override
    public synchronized void disconnect() {
        restClient.onDisconnect(this);
        restClient=null;
        eventListener=null;
        receivedMessages.clear();
        receivedMessages=null;

    }

    @Override
    public void setEventListener(ConnectionListener eventListener) {

        this.eventListener = eventListener;

    }

    @Override
    public synchronized void setSoTimeout(int idleTime) {
        timeOut = idleTime/500;

    }
}
