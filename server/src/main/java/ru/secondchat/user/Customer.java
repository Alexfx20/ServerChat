package ru.secondchat.user;

import ru.secondchat.network.Connection;
import ru.secondchat.server.ClientHandler;
import ru.secondchat.server.Server;
import ru.secondchat.server.Switcher;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class Customer extends User{



    public Customer(String name, String status) {
        super(name, status);
    }


    @Override
   public void onHold(ClientHandler clientHandler) throws IOException {
        clientHandler.setRecipient(null, null);//устанавливаем у управляющего клиентом потока значения полей для собеседника null и null
        Connection handlingUser = clientHandler.getHandlingUser();//получаем локальную переменную типа Connection для передачи сообщений управляемому клиенту
        handlingUser.sendMessage("Write a question, to start the chat");//ждем первого сообщения от пользователя ждем от юзера первой строки после которой начнетсяч чат

        try {
            String firstMessage = handlingUser.recieveSingleMessage();
            if(!(firstMessage.equals("/exit")||firstMessage.equals("/leave"))) {
                this.setReadyToChat(true);                                          //если первое сообщение не является командой на выход, то
                clientHandler.setFirstMessage(this.getStatus()+" "+this.getName()+" "+firstMessage);    //устанавливаем флаг, что клиент готов к разговору и записываем его первое сообщение
            }
            else//если вдруг клиент ввел выход, то передаем это сообщение в принимающий сообщения метод к-рый содержит обработчик событий.
            {
                clientHandler.onReciveMessage(handlingUser,firstMessage);

                return; // и выходим из цикла соответственно.
            }// если сообщение не является командой помещаем пользоватея в очередь

            handlingUser.sendMessage("Hold on please, you are the "+(Server.getCustomersSize()+1)+" in the queue");//отправляем сообщение чтобы клиент подождал
            synchronized (clientHandler) {
                if (!this.isInTheQueue()) {//проверяем находится ли клиент в очереди если нет там его только тогда добавляем его в  очередь
                    Server.addCustomers(clientHandler);//синхронизируем это действие по потоку обработчику, чтобы в это же время Switcher не вытащил клиента из очереди, между текущими действиями проверки и постановки в очередь
                    this.setInTheQueue(true);//помечаем флаг что клиент в очереди
                    try {
                        Switcher.BARRIER.await();//будим Switcher чтобы он посмотрел есть ли кто-то из свободных агентов
                    } catch (BrokenBarrierException e) {
                           clientLogger.error("Exception in await() method. status: client; name: "+getName());
                        e.printStackTrace();
                    }
                }
            }
              clientLogger.info("client "+getName()+" initiated the chat");
        }
        catch (InterruptedException e){
            System.out.println("Interrupted exception in Customers onHold method");
            clientHandler.onException(handlingUser, e);
        }
    }
}
