package ru.secondchat.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;
import ru.secondchat.network.WebSocketConnection;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

public class Server {

    static final Logger rootLogger = LogManager.getRootLogger();

    private static Configurator configurator;
    private static volatile boolean isShutDown;
    private static Server server;

    private static LinkedBlockingQueue<Connection>  connections;
    private static LinkedBlockingQueue<ClientHandler> customers;
    private static LinkedBlockingQueue<ClientHandler> agents;

    private static long k = 0;


    static{

        configurator = new Configurator("Settings.prop");
        customers =new LinkedBlockingQueue<>(configurator.getMAX_CLIENTS());
        agents = new LinkedBlockingQueue<>(configurator.getMAX_AGENTS());
        connections = new LinkedBlockingQueue<>();
        server = new Server();
    }

    public static void main(String [] args) {

        server.go();

    }

    private Server() {

        isShutDown = false;

        System.out.println("Server running... " + new Date());

    }

     public void go(){
        ExecutorService pool = Executors.newFixedThreadPool(configurator.getMAX_ALLOWED_CONNECTIONS());//максимальные соединения
        ((ThreadPoolExecutor)pool).setKeepAliveTime(configurator.getUSERS_THREAD_LIFETIME(),TimeUnit.SECONDS);
        ((ThreadPoolExecutor)pool).allowCoreThreadTimeOut(true);
         pool.execute( new Switcher());
         pool.execute(new SocketListener(configurator.getPORT()));
        try {
            while (!isShutDown) {
                try {
                    pool.execute(new ClientHandler(connections.take(), k ));
                      rootLogger.info("Connection request "+k);
                    k += 1;
                } catch (InterruptedException e) {//rootLogger.error("InteruptedException")}
                }
            }
        }

        finally {
            pool.shutdownNow();
            rootLogger.info("Server stopped");
        }

    }

    public static void shutDown(){
        rootLogger.info("Shutting down the server...");

        isShutDown = true;
        SocketListener.closeServerSocket();
        Switcher.disturbSwitcher();
        connections.add(new WebSocketConnection());
        closeAllConnections();

    }
    static void runServer(){
        isShutDown = false;
    }


    static boolean IsshutDown() {
        return isShutDown;
    }

    public static Server getServer() {
        return server;
    }
    public static int getCustomersSize(){//возвращает длину очереди клиентов

        return customers.size();
    }
    public static int getAgentsSize(){//возвращает длину очереди агентов

        return agents.size();
    }
    public static void addCustomers(ClientHandler customer)throws InterruptedException{//добавить клиента
        customers.put(customer);
    }
    public static void addAgents(ClientHandler agent)throws InterruptedException{//добавить агента
        agents.put(agent);
    }
    public static void addConnections(Connection connection)throws InterruptedException{//добавить агента
        connections.put(connection);
    }
    public static int getConnectionsSize(){//добавить агента
        return connections.size();
    }

    public static LinkedBlockingQueue<ClientHandler> getCustomers() {
        return customers;
    }

    public static LinkedBlockingQueue<ClientHandler> getAgents() {
        return agents;
    }

    public static Configurator getConfigurator() {
        return configurator;
    }

    private static void closeAllConnections(){
        for (Map.Entry<Long, ClientHandler> agent: StatisticsHolder.allAgents.entrySet()) {
            agent.getValue().stopClientHandlersThread();
        }
        for (Map.Entry<Long, ClientHandler> client: StatisticsHolder.allClients.entrySet()) {
            client.getValue().stopClientHandlersThread();
        }
    }
}
