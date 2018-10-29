package ru.secondchat.web;

import ru.secondchat.server.Server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartServletContextListener implements ServletContextListener {

    Server server;
    Thread thread;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
       /* ServletContext sc = servletContextEvent.getServletContext();*/
        server = Server.getServer();
      /*  sc.setAttribute("server", server);*/

        thread = new Thread(() -> {
            System.out.println("Running the server... ");
            String [] args= new String[2];
            Server.main(args);
            System.out.println("Server started successfully");
           });
        thread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        server.shutDown();
        //thread.interrupt();
    }
}
