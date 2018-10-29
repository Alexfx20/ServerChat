package ru.secondchat.server;





import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;
import ru.secondchat.network.WebSocketConnection;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;






public class Server {

    static final Logger rootLogger = LogManager.getRootLogger();
    //static final Logger rootLogger = LogManager.getLogger(Server.class);

    private int PORT;
    private static int MAX_ALLOWED_CONNECTIONS;
    private int MAX_MINUTES_ONLINE;
    private static int MAX_AGENTS;
    private static Properties properties;
    private static volatile boolean isShutDown;
    private static Server server;


    static LinkedBlockingQueue<ClientHandler> customers;
    static LinkedBlockingQueue<ClientHandler> agents;
    static LinkedBlockingQueue<Connection>  connections;
    static long k = 0;
    static public volatile String hello = "Hello message";

    static{
       // try {
           // properties = new Properties();
           // properties.load(new FileInputStream("Settings.prop"));
            MAX_ALLOWED_CONNECTIONS = 100;//Integer.parseInt(properties.getProperty("MAX_ALLOWED_CONNECTIONS"));
            MAX_AGENTS = 20;// Integer.parseInt(properties.getProperty("MAX_AGENTS"));
            customers = new LinkedBlockingQueue<>(MAX_ALLOWED_CONNECTIONS-MAX_AGENTS);
            agents = new LinkedBlockingQueue<>(MAX_AGENTS);
            connections = new LinkedBlockingQueue<>();
            server = new Server();

       /* } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){e.printStackTrace();}*/

    }

    public static void main(String [] args) {


        server.go();

    }

    public Server() {
        PORT = 5000;//Integer.parseInt(properties.getProperty("PORT"));//здесь ошибки задаем порт вручную
        MAX_MINUTES_ONLINE = 2;//Integer.parseInt(properties.getProperty("MAX_MINUTES_ONLINE"));
        isShutDown = false;

        System.out.println("Server running... " + new Date());

    }

     public void go(){
        ExecutorService pool = Executors.newFixedThreadPool(100);//максимальные соединения
        ((ThreadPoolExecutor)pool).setKeepAliveTime(120,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)pool).allowCoreThreadTimeOut(true);
         pool.execute( new Switcher());
         pool.execute(new SocketListener(PORT));
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
            rootLogger.info("Server stoped");
        }

    }

    public static void shutDown(){
        rootLogger.info("Shuting down the server...");

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

    private static void closeAllConnections(){
        for (Map.Entry<Long, ClientHandler> agent: StatisticsHolder.allAgents.entrySet()) {
            agent.getValue().stopClientHandlersThread();
        }
        for (Map.Entry<Long, ClientHandler> client: StatisticsHolder.allClients.entrySet()) {
            client.getValue().stopClientHandlersThread();
        }
    }
}
