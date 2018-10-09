package ru.secondchat.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.server.ClientHandler;

import java.io.IOException;

/*Абстрактный класс Юзер, содержит поля, характеризующие клиента\агента, сетеры и гетеры этих полей*/
public abstract class User {

    static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);

    //
    private String name = "";//имя пользователя
    private String status = "";//статус
    private int maxNumberOfRecipients;//максимальнодопустимое количество собеседников(для веб агента)
    private int ID;// ID каждого клиента
    private boolean isInTheQueue = false;//проверяет помещен ли данный user в какую-нибудь из очередей
    private boolean isReadyToChat = false;//флаг при тру поток находится в цикле с методом onRecieve
    private boolean isExit = false;
    private boolean isPromptForRelax = false;//флаг для агента, если он ненадолго отошел, чтобы не соединять его нискем, но и не отключать

    //Конструкторы
    public User() {
        maxNumberOfRecipients = 1;
    }//конструктор для консольных юзеров

    public User(String name, String status) {//конструктор для консольных юзеров
        this.name = name;
        this.status = status;
        maxNumberOfRecipients = 1;
    }

    public User(String name, String status, int maxNumberOfRecipients) {//конструктор для web юзеров
        this.name = name;
        this.status = status;
        this.maxNumberOfRecipients = maxNumberOfRecipients;
    }

    // гетеры
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getMaxNumberOfRecipients() {
        return maxNumberOfRecipients;
    }

    public int getID() {
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

    public boolean isPromptForRelax() {
        return isPromptForRelax;
    }

    //сетеры

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMaxNumberOfRecipients(int maxNumberOfRecipients) {
        this.maxNumberOfRecipients = maxNumberOfRecipients;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setInTheQueue(boolean inTheQueue) {
        isInTheQueue = inTheQueue;
    }

    public void setReadyToChat(boolean readyToChat) {
        isReadyToChat = readyToChat;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    public void setPromptForRelax(boolean promptForRelax) {
        isPromptForRelax = promptForRelax;
    }

    //метод определяет поведение агента\клиента после успешной регистрации и до их соединения с другим юзером
    abstract public void onHold(ClientHandler clientHandler) throws IOException;
}
