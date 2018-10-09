package ru.secondchat.network;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketConnection implements Connection {

    private ConnectionListener eventListener;       //обычный clientHandler
    private ConnectionListener websocketListener;   //WebSocekListener обрабатывает отправляемые текущему клиенту сообщения
    private LinkedBlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();// очередь из сообщений поступающих от клиента
    Session session;

    public WebSocketConnection(ConnectionListener websocketListener, Session session) {
        this.websocketListener = websocketListener;
        this.session = session;
    }

    public void addMessage(String message){   //добавляем сообщения в очередь
        try {
            receivedMessages.put(message);
        } catch (InterruptedException e) {
            eventListener.onException(this, e);//логирование в дочернем методе
        }
    }

    @Override
    public void startNewChat() {

    }//это наследство от обычного Socket соединения

    @Override
    public synchronized void sendMessage(String value) {
       if(value.startsWith("/"))            //отсылаем сообщение webSocketListener на обработку
        websocketListener.processCommands(value);
       else {
           try {
               session.getBasicRemote().sendText(value); //либо напрямую клиенту если сообщение не команда
           } catch (IOException e) {
               eventListener.onException(this, e);
           }
       }

    }

    @Override
    public String recieveSingleMessage() throws IOException {   //ждем нового сообщения
        String msg="";
        try {
           msg = receivedMessages.take();
        } catch (InterruptedException e) {
            eventListener.onException(this, e);
        }
        return msg;
    }

    @Override
    public void disconnect() {              //делегируем disconnect классу EndPoint и обнуляем ссылки
        websocketListener.onDisconnect(this);
        eventListener = null;
        websocketListener = null;
        session = null;


    }

    @Override
    public void setEventListener(ConnectionListener eventListener) {    //устанавливаем обычный ClientHandler, служит для обработки исключений
        this.eventListener = eventListener;

    }

    @Override
    public void setSoTimeout(int idleTime) {    //устанавливаем время ожидания
        session.setMaxIdleTimeout(idleTime);
    }//мак допустимое время бездействия

}
