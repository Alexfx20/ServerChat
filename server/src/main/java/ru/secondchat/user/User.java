package ru.secondchat.user;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;
import ru.secondchat.server.ClientHandler;

import java.io.IOException;
//@XmlRootElement(name = "user")
//@XmlType(propOrder = {"name", "status", "id", "number_of_recipients"})
/*Абстрактный класс Юзер, содержит пля, характеризующие клиента\агента, сетеры и гетеры этих полей*/
public abstract class User {

    static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);

    //
    private String name = "";//имя пользователя
    private String status = "";//статус
    private String maxNumberOfRecipients;//максимальнодопустимое количество собеседников(для веб агента)

    private boolean isInTheQueue = false;//проверяет помещен ли данный user в какую-нибудь из очередей
    private boolean isReadyToChat = false;//флаг при тру поток находится в цикле с методом onrecive
    private boolean isExit = false;
    private boolean isPromptForRelax = false;
    String ID;// ID каждого клиента
    ClientHandler recipientHandler;
    Connection recipient;

    //Конструкторы
    public User() {
        maxNumberOfRecipients = "1";
    }

    public User(String name, String status) {
        this.name = name;
        this.status = status;
        maxNumberOfRecipients = "1";
    }

    public User(String name, String status, String maxNumberOfRecipients) {
        this.name = name;
        this.status = status;
        this.maxNumberOfRecipients = maxNumberOfRecipients;
        System.out.println("MaxRecipient = "+maxNumberOfRecipients);
    }

    // гетеры
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getMaxNumberOfRecipients() {
        return maxNumberOfRecipients;
    }
    //@XmlElement(name="id")
    public String getID() {
        return ID;
    }


    public boolean isInTheQueue() {
        return isInTheQueue;
    }

    public boolean isReadyToChat() {
        return isReadyToChat;
    }

    public boolean isExit() {
        return isExit;
    }
   // @XmlTransient
    public boolean isPromptForRelax() {
        return isPromptForRelax;
    }
   // @XmlTransient
    public Connection getRecipient(){return recipient;}

    public boolean isOnChat(){return isFluded();}

    //сетеры
 //   @XmlElement(name="name")
    public void setName(String name) {
        this.name = name;
    }
   // @XmlElement(name="status")
    public void setStatus(String status) {
        this.status = status;
    }

    public void setMaxNumberOfRecipients(String maxNumberOfRecipients) {
        this.maxNumberOfRecipients = maxNumberOfRecipients;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    //@XmlElement
    public void setInTheQueue(boolean inTheQueue) {
        isInTheQueue = inTheQueue;
    }
   // @XmlElement
    public void setReadyToChat(boolean readyToChat) {
        isReadyToChat = readyToChat;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    public void setPromptForRelax(boolean promptForRelax) {
        isPromptForRelax = promptForRelax;
    }

    public void setRecipientHandler(ClientHandler recipientHandler, String ID) {
        this.recipientHandler = recipientHandler;
        this.ID = ID;
        this.recipient =  recipientHandler!=null ? recipientHandler.getHandlingUser() : null;
    }

    //методы жизненного цикла соединения
    abstract public void onHold(ClientHandler clientHandler) throws IOException;
    abstract public void sendMessageByID(String message);

    String extractID(String value){         //выделяет ID из строки
        int indexOfLiteral = value.lastIndexOf("$");
        if (indexOfLiteral!=-1&&indexOfLiteral!=(value.length()-1))
            return value.substring(indexOfLiteral);

        return "$0";//если явно не указано кому сообщение, то оно будет пересылаться первому человеку
    }

   public void leave(String value){
       setReadyToChat(false);
       setPromptForRelax(true);
       if(recipientHandler!=null){
           recipientHandler.getUser().endChatWith(ID, recipientHandler);//т.к. у user нет ссылки на clientHandler то ее нужно положить в этот метод
           recipient.sendMessage(String.format("/left %s %s %s", getStatus(), getName(), ID));}//String.format("%s has just left the chat./", name)
       clientLogger.info(getStatus()+" "+getName()+" has ended the chat. ID: "+ID);
   }

        public void endChatWith(String ID, ClientHandler handlingClient) {
            setReadyToChat(false);
        }

        public void exit(String value){
            setReadyToChat(false);
            if(recipientHandler!=null){
            recipientHandler.getUser().endChatWith(ID, recipientHandler);
                System.out.println("Между endChat и сообщением recipient");
                recipientHandler.getHandlingUser().sendMessage(String.format("/out %s %s %s", getStatus(), getName(), ID));}

        }
       // @XmlTransient
        public String getNewID(){return "$0";}
        public boolean isFluded(){
            return recipientHandler!=null ? true : false;}

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", maxNumberOfRecipients='" + maxNumberOfRecipients + '\'' +
                ", isInTheQueue=" + isInTheQueue +
                ", isReadyToChat=" + isReadyToChat +
                ", isExit=" + isExit +
                ", ID='" + ID + '\'' +
                '}';
    }


}
