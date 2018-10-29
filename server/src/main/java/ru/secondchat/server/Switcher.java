package ru.secondchat.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.user.Chat;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Switcher extends Thread{
    static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);
    static private ClientHandler client;
    static private ClientHandler agent;
    public static final CyclicBarrier BARRIER = new CyclicBarrier(2);

    @Override
    public void run() {
        while(!Server.IsshutDown()){

            if(Server.customers.isEmpty() || Server.agents.isEmpty()) {
                try {

                    System.out.println("Switcher trying to get a nap");
                    BARRIER.await();

                } catch (InterruptedException e) {
                    clientLogger.error("Exception in Switcher class " + e);
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    clientLogger.error("Exception in Switcher class " + e);
                    e.printStackTrace();
                }
            }
            else
            tryToSetUpConnection();

        }
    }


    private void tryToSetUpConnection() {
        System.out.println("Switcher is awaken");
       /* try { блок для отладки
            Thread.sleep(30000);
            System.out.println("Выполняем соединение");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        try {

                agent = Server.agents.take();       //берем агента

                synchronized (agent) {
                    System.out.println("агент синхронизирован");
                    agent.getUser().setInTheQueue(false);//Теперь синхронизируем его, чтобы он случайно не вышел  в другом потоке
                    if (agent.getHandlingUser() != null&&agent.getUser().isReadyToChat()) { //т.к. не проиcходит удаление клиентов из очереди при их выходе, то проверяем не вышел ли клинет в процессе ожидания
                        client = Server.customers.take();// если клинет вышел, то ссылка на его соединение обнуляется, достаем клинета из очереди
                        synchronized (client) {
                            System.out.println("клиент синхронизирован");
                            client.getUser().setInTheQueue(false);// теперь берем клинета и делаем с ним тоже самое
                            if (client.getHandlingUser() != null&&client.getUser().isReadyToChat()) {
                                String ID = agent.getUser().getNewID();//получаем новый ID у агента и присваиваем его данному соединению
                                agent.setRecipient(client, ID);//устанавливаем получателя агенту
                                client.setRecipient(agent, ID);//устанавливаем получателя клиенту
                                agent.getHandlingUser().sendMessage("Client ON-LINE "+client.getFirstMessage()+ID);//оповещаем клинета об установленном соединении
                                client.getHandlingUser().sendMessage("Agent "+agent.getUser().getName()+" ON-LINE");//оповещаем агетна об установленном соединении
                                if(!agent.getUser().isFluded()){Server.agents.put(agent); agent.getUser().setInTheQueue(true);} //если возможно возвращаем агента в очередь и устанавливаем флаг, что он в очереди
                                // System.out.println("Клиент "+client.getName()+" connected to "+agent.getName()+" at "+new Date());
                                clientLogger.info("Client " + client.getUser().getName() + " connected to " + agent.getUser().getName());
                                StatisticsHolder.chats.put(client.getId(),new Chat(agent, client));

                            } else{
                                Server.agents.put(agent);
                                agent.getUser().setInTheQueue(true);//помечаем что агент опять в очереди
                                }//если вдруг оказалось, что клиент вышел, то возвращаем агента обратно в очередь, и ожидаем нового клиента
                        }
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            clientLogger.error("Switcher class InterruptedException "+e);
            }

    }

    static void disturbSwitcher(){
        try {
            BARRIER.await(1,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            clientLogger.error("Fail to stop the Switcher Thread "+e);
        } catch (BrokenBarrierException e) {
            clientLogger.error("Fail to stop the Switcher Thread "+e);
        } catch (TimeoutException e) {
            clientLogger.error("Fail to stop the Switcher Thread "+e);
        }
    }
}
