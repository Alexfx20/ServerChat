package ru.secondchat.network;

import java.io.IOException;

public interface Connection {//определяет методы соединения.

    void startNewChat();//метод используюется только в консольном клиенте для запуска второго потока-слушателя.
    void sendMessage(String value);//отправка единичного сообщения
    String recieveSingleMessage()throws IOException;//получение единичного сообщения
    void disconnect();
    void setEventListener(ConnectionListener eventListener);//устанавливает ClientHandlera обслуживающего данное соединение
    void setSoTimeout(int idleTime);//максимальное время простаивания соединения

}
