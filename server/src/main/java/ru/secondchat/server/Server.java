package ru.secondchat.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.*;




public class Server {

    static final Logger rootLogger = LogManager.getRootLogger();


    private int PORT;
    private static int MAX_ALLOWED_CONNECTIONS;
    private int MAX_MINUTES_ONLINE;
    private static int MAX_AGENTS;
    private static Properties properties;
    private static boolean isshutDown;
    private static Server server;


    static LinkedBlockingQueue<ClientHandler> customers;
    static LinkedBlockingQueue<ClientHandler> agents;
    static LinkedBlockingQueue<Connection>  connections;
    static int k = 0;


    static{
        try {
            properties = new Properties();
            properties.load(new FileInputStream("Settings.prop"));
            MAX_ALLOWED_CONNECTIONS = Integer.parseInt(properties.getProperty("MAX_ALLOWED_CONNECTIONS"));
            MAX_AGENTS = Integer.parseInt(properties.getProperty("MAX_AGENTS"));
            customers = new LinkedBlockingQueue<>(MAX_ALLOWED_CONNECTIONS-MAX_AGENTS);
            agents = new LinkedBlockingQueue<>(MAX_AGENTS);
            connections = new LinkedBlockingQueue<>();
            server = new Server();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){e.printStackTrace();}

    }

    public static void main(String [] args) {
        server.go();
    }

    public Server() {
        PORT = Integer.parseInt(properties.getProperty("PORT"));
        MAX_MINUTES_ONLINE = Integer.parseInt(properties.getProperty("MAX_MINUTES_ONLINE"));
        isshutDown = false;

        System.out.println("Server running... " + new Date());

    }

     public void go(){
        ExecutorService pool = Executors.newFixedThreadPool(MAX_ALLOWED_CONNECTIONS);//максимальное количество соединений
        ((ThreadPoolExecutor)pool).setKeepAliveTime(MAX_MINUTES_ONLINE,TimeUnit.SECONDS);//время жизни бездействующих в пуле потоков
        ((ThreadPoolExecutor)pool).allowCoreThreadTimeOut(true);//возможность прерывания бездействующих потоков, не превышающих максимальное количество в 100 штук
         pool.execute( new Switcher()); //запуск служебных потоков
         pool.execute(new SocketListener(PORT));
        try {
            while (!isshutDown) {
                try {
                    pool.execute(new ClientHandler(connections.take(), k + 1));//запускается обработчик клиента при появлении нового соединения в очереди
                    k += 1;
                      rootLogger.info("Connection request "+k);
                } catch (InterruptedException e) {rootLogger.error("InterruptedException");}

            }
        }

        finally {
            pool.shutdownNow();
            rootLogger.info("Server stopped");
        }

    }

    public void shutDown(){
        rootLogger.info("Shutting down the server...");
        isshutDown = true;

    }

    static boolean isIsshutDown() {
        return isshutDown;
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
    public static void addConnections(Connection connection)throws InterruptedException{//добавить соединение
        connections.put(connection);
    }
}
