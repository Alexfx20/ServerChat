package ru.secondchat.user;

import ru.secondchat.network.Connection;
import ru.secondchat.server.ClientHandler;
import ru.secondchat.server.Server;
import ru.secondchat.server.StatisticsHolder;
import ru.secondchat.server.Switcher;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class Customer extends User{




    public Customer(String name, String status) {
        super(name, status);
    }


    @Override//метод onHold вызывается только если соединение установлено впервые либо если чат был завершен со всеми пользователями
   public void onHold(ClientHandler clientHandler) throws IOException {
        StatisticsHolder.removeChat(clientHandler.getId());//удаляем чат если он имеется в списке
        setRecipientHandler(null, null);//устанавливаем значения полей для собеседника null и null
        Connection handlingUser = clientHandler.getHandlingUser();//получаем локальную переменную типа Connection для передачи сообщений управляемому клиенту
        handlingUser.sendMessage("Write a question, to start the chat");//ждем первого сообщения от пользователя ждем от юзера первой строки после которой начнется чат

        try {
            String firstMessage = handlingUser.recieveSingleMessage();
            if(!(firstMessage.equals("/exit")||firstMessage.equals("/leave"))) {
                                                         //setКeadytoСhat было здесь!!!!!!!!!!
                clientHandler.setFirstMessage(this.getStatus()+" "+this.getName()+" "+firstMessage);    // и записываем его первое сообщение
            }
            else//если вдруг клиент ввел выход, то передаем это сообщение в принимающий метод к-рый содержит обработчик событий.
            {
                clientHandler.onReciveMessage(firstMessage);

                return; // и выходим из цикла соответственно.
            }// если сообщение не является командой помещаем пользоватея в очередь

            //отправляем сообщение чтобы клиент подождал
            synchronized (clientHandler) {
                this.setReadyToChat(true);//если первое сообщение не является командой на выход, то устанавливаем флаг, что клиент готов к разговору
                if (!this.isInTheQueue()) {//проверяем находится ли клиент в очереди если нет там его только тогда добавляем его в  очередь
                    Server.addCustomers(clientHandler);//синхронизируем это действие по потоку обработчику, чтобы в это же время Switcher не вытащил клиента из очереди, между текущими действиями проверки и постановки в очередь
                    this.setInTheQueue(true);//помечаем флаг что клиент в очереди
                    try {
                        Switcher.BARRIER.await();//будим Switcher чтобы он посмотрел есть ли кто-то из свободных агентов
                    } catch (BrokenBarrierException e) {
                        //   clientLogger.error("Exception in await() method. status: client; name: "+name);
                        e.printStackTrace();
                    }
                }
                handlingUser.sendMessage("Hold on please, you are the "+(Server.getCustomersSize())+" in the queue"); //отправляем сообщение чтобы клиент подождал
            }
            //  clientLogger.info("client "+name+" initiated the chat");
        }

        /*catch (SocketTimeoutException e) {
            handlingUser.sendMessage("/Timeout");
            onException(handlingUser, e);

        } это исключение ловится в ClientHandler*/
        catch (InterruptedException e){
            System.out.println("Поток прерван в процессе ожидания");
            clientHandler.onException(handlingUser, e);
        }
    }

    @Override
    public void sendMessageByID(String message) {
        if(recipient!=null) {
           /* try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            recipient.sendMessage(getStatus() + " " + getName() + " " + message + ID);//если соединение не ноль, то пишем сообщение здесь каждый клиент отсылает в конец свой ID
        }
    }







}
