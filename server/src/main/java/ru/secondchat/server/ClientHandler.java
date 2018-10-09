package ru.secondchat.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.SocketConnection;
import ru.secondchat.user.Agent;
import ru.secondchat.user.Customer;
import ru.secondchat.user.User;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable, ConnectionListener {
      //тут начинается
      static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);
        private ClientHandler recipientClient;//ссылка на собеседника

        private Connection recipient;//ссылка на соединение с собесдеником
        private Connection handlingUser;//ссылка на свое соединение
        private String firstMessage="";//сообщение от данного клиента
        private User user;// пользователь которым управляет поток
        int i;//это для статистики

        public ClientHandler(Socket socket, int i){

            this.i = i;
            try {
                handlingUser = new SocketConnection(this, socket);// создаем соединение - ин аут потоки связанные с сокетом
            } catch (IOException e) {
                e.printStackTrace();//залогировать
            }
        }

    public ClientHandler(Connection handlingUser, int i) {//Accomplishing Connection instance
            handlingUser.setEventListener(this); //в соединение передается ссылка на ClientHandler
        this.handlingUser = handlingUser;
        this.i = i;

    }

    @Override
        public void run() {
            //
            try{

                this.onConnectionReady(handlingUser);//вызываем метод подтверждающий что соединение с сервером установлено
                this.onRegistration(handlingUser);// регистрируемся на сервере
                while(!user.isExit()) {//До тех пор пока пользователь не ввел команду /exit повторяем цикл
                    handlingUser.setSoTimeout(120000);//в режиме ожидания когда пользователь обдумывает свое первое сообщение устанавливаем тайм аут 2 мин для соединения
                    user.onHold(this);//вызываем метод, который ожидает пользовательского ввода первого сообщения, после чего помещаем пользователя в очередь
                    handlingUser.setSoTimeout(600000);// увеличиваем тайм аут до 10 минут на время коммуникации с агентом
                    while(user.isReadyToChat()) {
                        try{
                        this.onReciveMessage(handlingUser, handlingUser.recieveSingleMessage());}//ожидаем сообщения от пользователя в случае получения пересылаем его агенту
                        catch(SocketTimeoutException e){
                            if(recipient!=null){handlingUser.sendMessage("/Timeout" );//если в процессе общения у одного из собеседников превышено время бездействия
                                recipientClient.getUser().setReadyToChat(false);//то второму собеседнику отсылается об этом уведомление и устанавливаетя флаг для возвращения к методу onHold
                                recipient.sendMessage(String.format("/left %s", user.getName()));
                            onException(handlingUser, e);
                            }
                        }
                    }
                }
            }
            catch(SocketTimeoutException e){
                System.out.println("Socket Exception");
                handlingUser.sendMessage("/Timeout" );
                this.onException(handlingUser, e);
            }
            catch (IOException e){
                System.out.println("incoming connection has just failed. Congratulations with: "+e);
                this.onException(handlingUser, e);
            }
            finally{this.onDisconnect(handlingUser);}//

        }

        //блок из различных сетеров и гетеров для данного класса
        public void setRecipient (ClientHandler recipientClient, Connection recipient){
            this.recipient = recipient;
            this.recipientClient = recipientClient;
        }

     public void setHandlingUser(Connection handlingUser) {
         this.handlingUser = handlingUser;
     }

     public Connection getHandlingUser() {
         return handlingUser;
     }
     public Connection getRecipient() {
         return recipient;
     }

    public User getUser() {
        return user;
    }

    public String getFirstMessage() {
         return firstMessage;
     }

    public void setFirstMessage(String firstMessage) {
        this.firstMessage = firstMessage;
    }

    //реализация интерфейса ConnectionListener

     public void processCommands(String value){
            if(value.equals("/leave")){
                user.setReadyToChat(false);
                user.setPromptForRelax(true);
                if(recipientClient!=null){
                recipientClient.getUser().setReadyToChat(false);
                recipient.sendMessage(String.format("/left %s", user.getName()));}
                clientLogger.info(user.getStatus()+" "+user.getName()+" has ended the chat");
            }
            else if(value.equals("/exit")){
                user.setReadyToChat(false);
                if(recipientClient!=null){
                    recipientClient.getUser().setReadyToChat(false);
                recipient.sendMessage(String.format("/out %s", user.getName()));}
               handlingUser.sendMessage("/exit");
                user.setExit(true);
                }
     }

     @Override
     public synchronized void onConnectionReady(Connection connection) {
         connection.sendMessage("Client connected: "+connection);
         clientLogger.info("Client connected: "+connection);

     }

     @Override
     public synchronized void onRegistration(Connection handlingUser) {
         try{

             handlingUser.sendMessage("Please enter your name: ");
            String name = handlingUser.recieveSingleMessage();
             System.out.println(name);
             handlingUser.sendMessage("Please enter your status (client or agent): ");
            String status = handlingUser.recieveSingleMessage();
            switch (status){
                case "agent":
                    user = new Agent(name, status);
                    handlingUser.sendMessage("Registration completed. Good day "+name);
                       clientLogger.info("Status of the user: "+status+" name: "+name+" Registration completed");
                    break;
                case "client":
                    user = new Customer(name,status);
                    handlingUser.sendMessage("Registration completed. Good day "+name);
                       clientLogger.info("Status of the user: "+status+" name: "+name+" Registration completed");
                    break;
                default:
                    clientLogger.info("access denied. Wrong registration parameters "+ name+" "+ status);
                    handlingUser.sendMessage("/access denied");
                    user = new Customer(name, status);
                    user.setExit(true);
                    break;
            }
         }
         catch (SocketTimeoutException e){

             handlingUser.sendMessage("/Timeout");
             onException(handlingUser, e);
         }
         catch (IOException e){

             System.out.println("Registration failed");
             handlingUser.sendMessage("Registration failed. Bye Bye ");
             onException(handlingUser, e);
         }
     }

     @Override
     public synchronized void onReciveMessage(Connection connection, String value) {
         firstMessage = value;
         if(value.startsWith("/")){
             processCommands(value);//данный метод обрабатывает команды с консоли
         }
            else
         sendToAllConnections(firstMessage);

     }

     @Override
     public synchronized void onDisconnect(Connection connection) {


         clientLogger.info(user.getStatus()+" "+user.getName()+" disconnected "+handlingUser);
         connection.disconnect();
         handlingUser = null;
         user.setExit(true);
        }

     @Override
     public synchronized void onException(Connection connection, Exception e) {
            if(e.getClass().getSimpleName().equals("SocketTimeoutException")){
                clientLogger.info("TimeOut: waiting time has been exceeded "+handlingUser);
            }
            else {
         clientLogger.error(user.getStatus()+" "+user.getName()+" Exception occurred "+e);
         e.printStackTrace();}
         user.setExit(true);//при возникновении Exception соединение разрывается
     }

     private void sendToAllConnections(String message){


         if(handlingUser!=null)
             handlingUser.sendMessage(user.getStatus()+" "+user.getName()+" : "+message);
         if(recipient!=null){
             recipient.sendMessage(user.getStatus()+" "+user.getName()+" : "+message);}
     }
}
