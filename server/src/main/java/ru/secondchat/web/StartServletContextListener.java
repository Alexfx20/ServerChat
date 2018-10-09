package ru.secondchat.web;

import ru.secondchat.server.Server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartServletContextListener implements ServletContextListener {

    Server server;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext sc = servletContextEvent.getServletContext();
        server = Server.getServer();
        sc.setAttribute("server", server);

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                String [] args= new String[2];
                server.main(args);

               }

        });
        thread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        server.shutDown();
    }
}
