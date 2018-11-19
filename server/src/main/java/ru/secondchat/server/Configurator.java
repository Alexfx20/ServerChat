package ru.secondchat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configurator {

    /*
    *   Server configuration parameters
     */
    private Properties properties;
    private int MAX_ALLOWED_CONNECTIONS;
    private int MAX_MINUTES_ONLINE;
    private int MAX_AGENTS;
    private int MAX_CLIENTS;
    private int PORT;
    private int USERS_THREAD_LIFETIME;

    private static final Logger configuratorLogger = LogManager.getLogger(Configurator.class);

    /*
    * SWAGGERServlet PARAMETERS
     */
       private String beanConfigSetBasePath;
        private String beanConfigSetHost;
        private String beanConfigSetTitle;
        private String beanConfigSetSchemes;
        private String beanConfigSetVersion;


    public Configurator(String propertyFileName) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        properties = new Properties();
        try {
            InputStream input = classLoader.getResourceAsStream(propertyFileName);
            properties.load(input);
        }
        catch(NullPointerException e){
            configuratorLogger.error(propertyFileName+" not found.\nLoading default parameters...\nPORT = 5000");
        }
        catch (IOException e) {
            configuratorLogger.error(propertyFileName+" not found");
        }
        initializeSetting();
    }



    private void initializeSetting(){
        MAX_ALLOWED_CONNECTIONS = Integer.parseInt(properties.getProperty("MAX_ALLOWED_CONNECTIONS", "100"));
        MAX_MINUTES_ONLINE = Integer.parseInt(properties.getProperty("MAX_MINUTES_ONLINE", "10"));
        MAX_AGENTS = Integer.parseInt(properties.getProperty("MAX_AGENTS", "20"));
        MAX_CLIENTS = MAX_ALLOWED_CONNECTIONS-MAX_AGENTS;
        USERS_THREAD_LIFETIME = Integer.parseInt(properties.getProperty("USERS_THREAD_LIFETIME", "2"));
        PORT = Integer.parseInt(properties.getProperty("PORT", "5000"));
        beanConfigSetBasePath = properties.getProperty("beanConfigSetBasePath", "/server-1.0-SNAPSHOT/rest");
        beanConfigSetHost = properties.getProperty("beanConfigSetHost", "localhost:8080");
        beanConfigSetTitle = properties.getProperty("beanConfigSetTitle", "Chat RestServices Docs");
        beanConfigSetSchemes = properties.getProperty("beanConfigSetSchemes", "http");
        beanConfigSetVersion = properties.getProperty("beanConfigSetVersion", "1.0");
        /*System.out.println(beanConfigSetBasePath);
        System.out.println(beanConfigSetHost);
        System.out.println(beanConfigSetTitle);
        System.out.println(beanConfigSetSchemes);
        System.out.println(beanConfigSetVersion);*/
    }

    public int getMAX_ALLOWED_CONNECTIONS() {
        return MAX_ALLOWED_CONNECTIONS;
    }

    public int getMAX_MINUTES_ONLINE() {
        return MAX_MINUTES_ONLINE;
    }

    public int getMAX_AGENTS() {
        return MAX_AGENTS;
    }

    public int getPORT() {
        return PORT;
    }

    public int getMAX_CLIENTS() {
        return MAX_CLIENTS;
    }

    public int getUSERS_THREAD_LIFETIME() {
        return USERS_THREAD_LIFETIME;
    }

    public String getBeanConfigSetBasePath() {
        return beanConfigSetBasePath;
    }

    public String getBeanConfigSetHost() {
        return beanConfigSetHost;
    }

    public String getBeanConfigSetTitle() {
        return beanConfigSetTitle;
    }

    public String getBeanConfigSetSchemes() {
        return beanConfigSetSchemes;
    }

    public String getBeanConfigSetVersion() {
        return beanConfigSetVersion;
    }
}
