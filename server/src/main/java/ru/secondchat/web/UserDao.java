package ru.secondchat.web;

import ru.secondchat.server.ClientHandler;
import ru.secondchat.server.StatisticsHolder;
import ru.secondchat.user.Chat;
import ru.secondchat.user.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private int start;
    private int end;
    private StatisticsHolder statistics = StatisticsHolder.getInstance();


    public List<ClientHandler> getAllAgents(int pageNumber, int pageSize) {


        List<ClientHandler> list = new ArrayList<>(statistics.getAgents().values());
        List<ClientHandler> result = chooseDateAccordingPagination(list, pageNumber, pageSize);
        System.out.println(result);
        return result;
    }

    public List<ClientHandler> getFreeAgents(int pageNumber, int pageSize) {

        ClientHandler[] clientHandler = statistics.getFreeagents().toArray(new ClientHandler[0]);
        List<ClientHandler> result = chooseDateAccordingPagination(clientHandler, pageNumber, pageSize);
        System.out.println(result);
        return result;
    }

    public String getAgentsSize() {

        String result = String.format("There are %d agents available", statistics.getFreeagents().size());
        return result;
    }

    public User getAgent(long id) {
        User user = statistics.getAgentByID(id);
        System.out.println(user+": name "+user.getName()+" status "+ user.getStatus());

        return user;
    }

    public List<ClientHandler> getClients(int pageNumber, int pageSize) {

        ClientHandler[] clientHandler = statistics.getFreeclients().toArray(new ClientHandler[0]);
        List<ClientHandler> result = chooseDateAccordingPagination(clientHandler, pageNumber, pageSize);

        return result;
    }

    public User getClient(long id) {

        return statistics.getClientByID(id);
    }

    public List<Chat> getChats(int pageNumber, int pageSize){
        List<Chat> result = new ArrayList(statistics.getChats().values());
        result = chooseDateAccordingPagination(result, pageNumber, pageSize);

        return result;
    }
    public Chat getChatByID(long id){

        return statistics.getChatByID(id);
    }

    //добавляем юзера
    public String addUser(String id, String name, String status, int numOfChats){
        if(id==null||status==null||name==null)
            return null;
        name = name.trim();
        status=status.trim();
        id=id.trim();
        if(!isParametersCorrect(id, name, status, numOfChats))
            return null;
        /*System.out.println("ID= "+id);
        System.out.println(" name= "+name);
        System.out.println(" status= "+status);
        System.out.println(" numOfChats= "+numOfChats);*/
       RestClientsHandler currentUser = new RestClientsHandler();
        String response=currentUser.register(id, name, status, numOfChats);


        return response;
    }

    public boolean isParametersCorrect(String id, String name, String status, int numOfChats){
        int slashInName = name.indexOf(" ");
        int slashInStatus = status.indexOf(" ");
        int ampersandInName = name.indexOf("$");
        int ampersandInStatus = name.indexOf("$");
        if(slashInName!=-1||slashInStatus!=-1||ampersandInName!=-1||ampersandInStatus!=-1||numOfChats>20||numOfChats<0)
            return false;
        if(!(status.toLowerCase().equals("agent"))&&(!status.toLowerCase().equals("client")))
            return false;

        return true;
    }

    public String deleteUser(String id){
        RestClientsHandler currentUser = RestClientsHandler.getInstance(id);
        if(currentUser==null)
            return null;
        String response = currentUser.quit();
        System.out.println(response);

        return response;
    }

    public String sendMessage(String id, String message){
       RestClientsHandler currentUser = RestClientsHandler.getInstance(id);
       if(currentUser==null) return null;
       String response = currentUser.sendMessage(message);
        return response;
    }

    public List<String> seeMessages(int pageNumber, int pageSize, String id){
        RestClientsHandler currentUser = RestClientsHandler.getInstance(id);
        if(currentUser==null) return null;
        String[] messages = currentUser.seeAllReceivedMessages();
        List<String> listOfMessages = new ArrayList<>();
        if(messages.length==0){
            listOfMessages.add(currentUser.response("no messages received", id));
            return listOfMessages;
        }
        listOfMessages = chooseDateAccordingPagination(messages, pageNumber, pageSize);

        return listOfMessages;
    }

    //метод обеспечивает поддержку пагинации
    private boolean selectPages(int pageNumber, int pageSize, int listSize) {
        System.out.println("pageSize= "+pageSize);
        System.out.println("pageNum= "+pageNumber);
        this.start = 0;
        this.end = listSize;
        if (pageSize != 0) {
            this.start = pageNumber * pageSize;
            if(start>=listSize)return false;        //show that asked page does't exist.
            this.end = start + pageSize;
            if(end>listSize) this.end=listSize;     //if the size of last page less than specified pageSize, nevertheless show all information of last page.
        }

        return true;
    }

    private <T> List<T> chooseDateAccordingPagination(List<T> list, int pageNumber, int pageSize){
        if(list.isEmpty())return null;
        System.out.println(list);
        if(!selectPages(pageNumber, pageSize, list.size())) return null;
        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {

            result.add(list.get(i));
        }
        return result;
    }

    private <T> List<T> chooseDateAccordingPagination(T[] array, int pageNumber, int pageSize){
        if(array.length==0)return null;
        if(!selectPages(pageNumber, pageSize, array.length)) return null;
        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(array[i]);
        }
        return result;
    }



}
