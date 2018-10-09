package ru.secondchat.user;

import ru.secondchat.network.Connection;
import ru.secondchat.server.ClientHandler;
import ru.secondchat.server.Server;
import ru.secondchat.server.Switcher;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class Agent extends User{

    public Agent(String name, String status) {
        super(name, status);
    }

    public Agent(String name, String status, int maxNumberOfRecipients) {
        super(name, status, maxNumberOfRecipients);
    }

    @Override
    public void onHold(ClientHandler clientHandler) throws IOException {
        clientHandler.setRecipient(null, null);//устанавливаем у управляющего клиентом потока значения полей для собеседника null и null
        Connection handlingUser = clientHandler.getHandlingUser();
        if(isPromptForRelax()){     //проверяем вышел ли агент самостоятельно, если да, то ждем от него сообщения
            handlingUser.sendMessage("Write a line whenever You are ready");//ждем первого сообщения от пользователя
            String firstMessage = handlingUser.recieveSingleMessage();//обработка аналогично клиенту
            if(!(firstMessage.equals("/exit")||firstMessage.equals("/leave"))) {

                this.setPromptForRelax(false);//если все ок, переводим флаги в режим готовности к чату
            }
            else
            {
                clientHandler.onReciveMessage(handlingUser,firstMessage);//если опять была команда на выход обрабатываем ее

                return;
            }// если сообщение не является командой помещаем пользоватея в очередь
        }
        this.setReadyToChat(true);
        handlingUser.sendMessage("looking for new customers, wait please, you are the "+(Server.getAgentsSize()+1)+" among agents");
        try {
            clientHandler.setFirstMessage(String.format("Agent %s on-line", this.getName()));
            synchronized (clientHandler) {
                if (!this.isInTheQueue()) {
                    Server.addAgents(clientHandler);
                    this.setInTheQueue(true);
                    try {
                        Switcher.BARRIER.await();
                    } catch (BrokenBarrierException e) {
                           clientLogger.error("Exception in await() method. status: agent; name: "+getName());
                        e.printStackTrace();
                    }
                }
            }

            } catch(InterruptedException e){
                System.out.println("Exception while waiting a turn");
                clientHandler.onException(handlingUser, e);
            }
    }
}
