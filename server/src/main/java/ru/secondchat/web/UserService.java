package ru.secondchat.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import ru.secondchat.server.ClientHandler;
import ru.secondchat.user.Chat;
import ru.secondchat.user.User;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/UserService")
@Api("/UserService")
@SwaggerDefinition(tags={@Tag(name="User Service", description = "REST Endpoint for WebChat")})
public class UserService {

    UserDao userDao = new UserDao();
    private static final String SUCCESS_RESULT = "\"result\": \"success\"";
    private static final String FAILURE_RESULT = "\"result\": \"failure\"";

    @GET
    @Path("/agents")//возвращаем всех агентов
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientHandler> getAgents(
            @DefaultValue("0") @QueryParam("pageNumber") int pageNumber,
            @DefaultValue("0") @QueryParam("pageSize") int pageSize){
        return userDao.getAllAgents(pageNumber, pageSize);
    }

    @GET
    @Path("/agents/available")//возвращаем всех свободных агентов
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientHandler> getFreeAgents(
            @DefaultValue("0") @QueryParam("pageNumber") int pageNumber,
            @DefaultValue("0") @QueryParam("pageSize") int pageSize){
        return userDao.getFreeAgents(pageNumber, pageSize);
    }
    @GET
    @Path("/agents/size")//возвращаем число свободных агентов
    @Produces(MediaType.APPLICATION_JSON)
    public String getAgentsSize(){
        return userDao.getAgentsSize();
    }

    @GET
    @Path("/agents/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getAgentByID(@PathParam("id") long id){
        return userDao.getAgent(id);
    }

    @GET
    @Path("/clients")//возвращаем всех клиентов
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientHandler> getFreeClients(
            @DefaultValue("0") @QueryParam("pageNumber") int pageNumber,
            @DefaultValue("0") @QueryParam("pageSize") int pageSize){
        return userDao.getClients(pageNumber, pageSize);
    }

    @GET
    @Path("/clients/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getClientByID(@PathParam("id") long id){
        return userDao.getClient(id);
    }


    @GET
    @Path("/chats")//возвращаем все чаты
    @Produces(MediaType.APPLICATION_JSON)
    public List<Chat> getChats(
            @DefaultValue("0") @QueryParam("pageNumber") int pageNumber,
            @DefaultValue("0") @QueryParam("pageSize") int pageSize){
        return userDao.getChats(pageNumber, pageSize);
    }

    @GET
    @Path("/chats/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Chat getChatByID(@PathParam("id") long id){
        return userDao.getChatByID(id);
    }


    @GET
    @Path("/users/messages/{privateID}")//получить все сообщения
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMessages(
            @PathParam("privateID") String id,
            @DefaultValue("0") @QueryParam("pageNumber") int pageNumber,
            @DefaultValue("0") @QueryParam("pageSize") int pageSize){

        System.out.println(id+" "+pageNumber+" "+pageSize);
        return userDao.seeMessages(pageNumber, pageSize, id);
    }


    @PUT
    @Path("/register/{privateID}")//зарегистрироваться
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String createUser(@PathParam("privateID") String id,
                             @FormParam("name") String name,
                             @FormParam("status") String status,
                             @FormParam("numberOfChats") int numOfChats,
                             @Context HttpServletResponse servletResponse) throws IOException {

        String result = userDao.addUser(id, name, status, numOfChats);
        if(result!=null)
            return result;

        return FAILURE_RESULT;
    }
    @POST
    @Path("/users/messages/{privateID}")//отправить сообщение
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String updateUser(@PathParam("privateID") String id,
                             @FormParam("message") String message,
                             @Context HttpServletResponse servletResponse) throws IOException{

        System.out.println("ID= "+id+" message= "+message);
        String result = userDao.sendMessage(id, message);

        return result;
    }
    @DELETE
    @Path("/users/{privateID}")//удалить юзера
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@PathParam("privateID") String id){
        String result = userDao.deleteUser(id);

        return result;
    }
    @OPTIONS
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSupportedOperations(){
        return "<operations>GET, PUT, POST, DELETE</operations>";
    }

}
