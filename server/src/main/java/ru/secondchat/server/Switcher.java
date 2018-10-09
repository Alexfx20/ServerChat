package ru.secondchat.server;

//import org.apache.logging.log4j.*;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Switcher extends Thread{
    static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);
    static private ClientHandler client;
    static private ClientHandler agent;
    public static final CyclicBarrier BARRIER = new CyclicBarrier(2);

    @Override
    public void run() {
        System.out.println("Switcher has started...");
        while(!Thread.currentThread().isInterrupted()){

            if(Server.customers.isEmpty() || Server.agents.isEmpty()) {
                try {

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

//если очерди не пустые, то вызывается этот метод
    private void tryToSetUpConnection() {

            try {

                agent = Server.agents.take();       //берем агента

                synchronized (agent) {
                    agent.getUser().setInTheQueue(false);//Теперь синхронизируем его, чтобы он случайно не вышел  в другом потоке
                    if (agent.getHandlingUser() != null&&agent.getUser().isReadyToChat()) { //т.к. не проиcходит удаление клиентов\агентов из очереди при их выходе, то проверяем не вышел ли юзер в процессе ожидания
                        client = Server.customers.take();// если клинет вышел, то ссылка на его соединение обнуляется, достаем клиента из очереди
                        synchronized (client) {
                            client.getUser().setInTheQueue(false);// теперь берем клинета и делаем с ним тоже самое
                            if (client.getHandlingUser() != null&&client.getUser().isReadyToChat()) {
                                agent.setRecipient(client, client.getHandlingUser());//устанавливаем получателя агенту
                                client.setRecipient(agent, agent.getHandlingUser());//устанавливаем получателя клиенту
                                agent.getHandlingUser().sendMessage(client.getFirstMessage());//оповещаем клинета об установленном соединении
                                client.getHandlingUser().sendMessage(agent.getFirstMessage());//оповещаем агетна об установленном соединении
                                // System.out.println("Клиент "+client.getName()+" connected to "+agent.getName()+" at "+new Date());
                                clientLogger.info("Client " + client.getUser().getName() + " connected to Agent " + agent.getUser().getName());

                            } else
                                Server.agents.put(agent);//если вдруг оказалось, что клиент вышел, то возвращаем агента обратно в очередь, и ожидаем нового клиента
                        }
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            clientLogger.error("Switcher class InterruptedException ");
            }
    }
}
