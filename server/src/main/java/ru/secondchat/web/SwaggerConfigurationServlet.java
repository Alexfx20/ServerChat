package ru.secondchat.web;

import io.swagger.jaxrs.config.BeanConfig;
import ru.secondchat.server.Configurator;
import ru.secondchat.server.Server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


public class SwaggerConfigurationServlet extends HttpServlet {

    private static  final long serialVersionUID = 1L;

    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        Configurator configurator = Server.getConfigurator();
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath(configurator.getBeanConfigSetBasePath());
        beanConfig.setHost(configurator.getBeanConfigSetHost());
        beanConfig.setTitle(configurator.getBeanConfigSetTitle());
        beanConfig.setResourcePackage("ru.secondchat.web");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
        beanConfig.setSchemes(new String[]{configurator.getBeanConfigSetSchemes()});
        beanConfig.setVersion(configurator.getBeanConfigSetVersion());
    }

}
