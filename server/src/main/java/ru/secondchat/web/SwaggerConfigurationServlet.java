package ru.secondchat.web;

import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


public class SwaggerConfigurationServlet extends HttpServlet {

    private static  final long serialVersionUID = 1L;

    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath("/server-1.0-SNAPSHOT/rest");
        beanConfig.setHost("localhost:8080");
        beanConfig.setTitle("Chat RestServices Docs");
        beanConfig.setResourcePackage("ru.secondchat.web");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setVersion("1.0");
    }

}
