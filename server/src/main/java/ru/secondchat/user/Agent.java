package ru.secondchat.user;

import ru.secondchat.network.Commands;
import ru.secondchat.network.Connection;
import ru.secondchat.server.ClientHandler;
import ru.secondchat.server.Server;
import ru.secondchat.server.Switcher;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Agent extends User{

    ConcurrentMap <String, ClientHandler> recipientsMap;

    public Agent(String name, String status) {
        super(name, status);
    }

    public Agent(String name, String status, String maxNumberOfRecipients) {
        super(name, status, maxNumberOfRecipients);
        recipientsMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onHold(ClientHandler clientHandler) throws IOException {
        setRecipientHandler(null, null);//устанавливаем у управляющего клиентом потока значения полей для собеседника null и null
        Connection handlingUser = clientHandler.getHandlingUser();
        if(isPromptForRelax()){     //проверяем вышел ли агент самостоятельно, если да, то ждем от него сообщения
            handlingUser.setSoTimeout(600000);//устанавливаем время отдыха для агента 10 мин
            handlingUser.sendMessage("Write a line whenever You are ready");//ждем первого сообщения от пользователя
            String firstMessage = handlingUser.recieveSingleMessage();//обработка аналогично клиенту
            if(!(firstMessage.startsWith(Commands.EXIT.getCommand())||firstMessage.startsWith(Commands.LEAVE.getCommand()))) {

                this.setPromptForRelax(false);//если все ок, переводим флаги в режим готовности к чату
            }
            else
            {
                clientHandler.onReciveMessage(firstMessage);//если опять была команда на выход обрабатываем ее

                return;
            }// если сообщение не является командой помещаем пользователя в очередь
        }
       //setReadyToChat
       //sendFirstMessage
        try {
            clientHandler.setFirstMessage(String.format("Agent %s on-line", this.getName()));
            synchronized (clientHandler) {
                this.setReadyToChat(true);
                if (!this.isInTheQueue()) {
                    Server.addAgents(clientHandler);
                    this.setInTheQueue(true);
                    try {
                        Switcher.BARRIER.await();
                    } catch (BrokenBarrierException e) {
                        //   clientLogger.error("Exception in await() method. status: agent; name: "+name);
                        e.printStackTrace();
                    }
                }
                handlingUser.sendMessage("looking for new customers, wait please, you are the "+(Server.getAgentsSize())+" among agents");
            }

            } catch(InterruptedException e){
                System.out.println("Exception while waiting a turn");
                clientHandler.onException(handlingUser, e);
            }
    }

    @Override
    public void setRecipientHandler(ClientHandler recipientHandler, String ID) {
        if (recipientsMap == null)  //если карта не инициализирована, т.е. у агента поддерживается только одна беседа, то вызываем родительский метод
            super.setRecipientHandler(recipientHandler, ID);
        else if (recipientHandler != null) recipientsMap.put(ID, recipientHandler);//если ссылка на получателя валидна, то добавляем его в мапу
        else recipientsMap.clear();//если ссылка ноль, то удаляем все из карты.


    }

    @Override//если юзер один, то просто отправляем сообщение через поле recipient родительского класса
    public void sendMessageByID(String message) {
                if(isOnChat()){
            switch (getMaxNumberOfRecipients()) {
                case "1":
                    recipient.sendMessage(getStatus() + " " + getName() + " : " + message);
                    break;
                default:
                    String ID = extractID(message);//каждое сообщение от юзера отсылается с прикрепленным к нему ID
                    System.out.println("ID: " + ID);
                    ClientHandler recipientClient = recipientsMap.get(ID);
                    if (recipientClient == null) {//это для отладки с консоли
                        System.out.println("NO USER FOUND ID: " + ID);
                        break;
                    }
                    Connection con = recipientClient.getHandlingUser();//из мапы берем поток получателя и вытаскиваем из него connection
                    int idLength=message.lastIndexOf(ID);
                    if (idLength!=-1)message=message.substring(0,idLength);
                    con.sendMessage(getStatus() + " " + getName() + " : " + message);
                    break;
            }
        }
    }

    @Override
    public void leave(String value) {
        if(getMaxNumberOfRecipients().equals("1"))
            super.leave(value);
       else if(recipientsMap.size()<2||value.startsWith(Commands.LEAVE_ALL.getCommand())){setReadyToChat(false);
            setPromptForRelax(true);
            endChatWithAllRecipients();
       }
        else{

            String ID=extractID(value);
            ClientHandler recipId = recipientsMap.get(ID);
            if (recipId!=null){
            recipientsMap.remove(ID);
            recipId.getUser().endChatWith(ID, recipId);
            Connection recipientConnection = recipId.getHandlingUser();
            if(recipientConnection!=null)
            recipientConnection.sendMessage(String.format("%s %s %s", Commands.LEFT.getCommand(), getStatus(), getName()));//здесь в конце был ID
            clientLogger.info(getStatus()+" "+getName()+" has ended the chat");}
        }
    }

    @Override//данный метод вызывается только из другого потока и обязательно клиентом, агент в свою очердь вызывает точно такой же метод у клиента
    public void endChatWith(String ID, ClientHandler handlingClient) {

        if(getMaxNumberOfRecipients().equals("1")||recipientsMap.size()<2)
            setReadyToChat(false);
        else {recipientsMap.remove(ID);
            try {
                Server.addAgents(handlingClient);
                setInTheQueue(true);
                Switcher.BARRIER.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (BrokenBarrierException e){
                e.printStackTrace();
            }
        }
    }

    public void exit(String value){
        if(getMaxNumberOfRecipients().equals("1")){
            super.exit(value);
            return; }
        String ID = extractID(value);
        setReadyToChat(false);
        endChatWithAllRecipients();

    }

   public String getNewID(){//генерирует новый ID текущего диалога в диапазоне от 0 до максимального числа коннектов для агента
        int maxNumOfConnections = Integer.parseInt(getMaxNumberOfRecipients());
        if (maxNumOfConnections == 1) return "$0";//какой ID в случае одного клиента?
        else {
            for (int i = 0; i <= maxNumOfConnections; i++) {
                String ID = "$" + Integer.toString(i);
                System.out.println("ID " + ID);
                if (!recipientsMap.containsKey(ID)) return ID;
            }
        }
        return "-1";
    }
   public boolean isFluded(){
        System.out.println(getMaxNumberOfRecipients());
        if (getMaxNumberOfRecipients().equals("1")) return recipient!= null ? true : false;
        return (getMaxNumberOfRecipients()).compareTo(Integer.toString(recipientsMap.size()))>0 ? false : true;
    }

    @Override
    public boolean isOnChat() {
        if(getMaxNumberOfRecipients().equals("1"))
        return recipient!= null ? true : false;
        else return recipientsMap.isEmpty() ? false : true;
    }

    private void endChatWithAllRecipients(){//данный метод вызывается в этом потоке у агента и пробегается по всей карте обращаясь к методу endchat у юзера.
        if(!recipientsMap.isEmpty()){
            for (ConcurrentMap.Entry<String, ClientHandler> pair:recipientsMap.entrySet()) {
                ClientHandler recip = pair.getValue();
                recip.getUser().endChatWith(ID, recip);
                Connection recipientConnection = recip.getHandlingUser();
                if(recipientConnection!=null){

                    recipientConnection.sendMessage(String.format("%s %s %s",Commands.OUT.getCommand(), getStatus(), getName()));}//здесь в конце бы ID
            }
        }
    }

}
