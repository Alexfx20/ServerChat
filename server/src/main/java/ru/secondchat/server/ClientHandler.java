package ru.secondchat.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.secondchat.network.Connection;
import ru.secondchat.network.ConnectionListener;
import ru.secondchat.network.SocketConnection;
import ru.secondchat.user.Agent;
import ru.secondchat.user.Customer;
import ru.secondchat.user.User;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
//@XmlRootElement(name = "userhandler")
//@XmlType(propOrder = {"id", "user"})
public class ClientHandler implements Runnable, ConnectionListener {
      //тут начинается
      static final Logger clientLogger = LogManager.getLogger(ClientHandler.class);

        private Connection handlingUser;//ссылка на свое соединение

        private String firstMessage="";//сообщение от данного клиента
        private User user;// пользователь которым управляет поток

    private long id;

    public ClientHandler() {}

    public ClientHandler(Socket socket, long i){

            this.id = i;
            try {
                handlingUser = new SocketConnection(this, socket);// создаем соединение - ин аут потоки связанные с сокетом
            } catch (IOException e) {
                e.printStackTrace();//залогировать
            }
        }//конструктор для тестов

    public ClientHandler(Connection handlingUser, long i) {//Accomplishing Connection instance
            handlingUser.setEventListener(this);
        this.handlingUser = handlingUser;
        this.id = i;

    }

    @Override
        public void run() {
            //
            try{

                this.onConnectionReady(handlingUser);//вызываем метод подтверждающий что соединение с сервером установлено
                handlingUser.setSoTimeout(120000);//в режиме ожидания когда пользователь обдумывает свое первое сообщение устанавливаем тайм аут 2 мин для соединения
                this.onRegistration(handlingUser);// регистрируемся на сервере
                while(!user.isExit()) {//До тех пор пока пользователь не ввел команду /exit повторяем цикл
                    user.onHold(this);//вызываем метод, который ожидает пользовательского ввода первого сообщения, после чего помещаем пользователя в очередь
                    handlingUser.setSoTimeout(600000);// увеличиваем тайм аут до 10 минут на время коммуникации с агентом
                    while(user.isReadyToChat()) {
                        try{
                        this.onReciveMessage(handlingUser.recieveSingleMessage());}//ожидаем сообщения от пользователя в случае получения пересылаем его агенту
                        catch(SocketTimeoutException e){
                            System.out.println("ловим зедсь Exception");
                            if(user.isOnChat()){handlingUser.sendMessage("/Timeout" );
                            onException(handlingUser, e);
                            }
                        }
                    }
                    handlingUser.setSoTimeout(120000);
                }
            }
            catch(SocketTimeoutException e){
                System.out.println("Socket Exception");
                handlingUser.sendMessage("/Timeout" );
                this.onException(handlingUser, e);
            }
            catch (IOException e){
                System.out.println("incoming connection has just failed. Congratulations with: "+e);
                this.onException(handlingUser, e);
            }
            finally{this.onDisconnect(handlingUser);}//тут

        }

        public void setRecipient (ClientHandler recipientHandler, String ID){
            user.setRecipientHandler(recipientHandler, ID);
            /*this.recipient = recipient;
            this.recipientClient = recipientClient;
            if(ID!=null&&recipientClient!=null)
            recipientsMap.put(ID, recipientClient);//
            else  if(ID==null&&recipientsMap!=null)recipientsMap.clear();//ID = null, когда вызван метод onHold;
            else recipientsMap.remove(ID);*///если остальные значения обнуляются, а ID не null, то надо удалить этот элемент из списка
        }
   // @XmlElement
     public void setHandlingUser(Connection handlingUser) {
         this.handlingUser = handlingUser;
     }
     //   @XmlTransient
     public Connection getHandlingUser() {
         return handlingUser;
     }
    //@XmlTransient
     public Connection getRecipient() {
         return user.getRecipient();
     }//для тестов
   // @XmlAttribute
    public User getUser() {
        return user;
    }
   // @XmlElement(name = "id")
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
    //@XmlTransient
    public String getFirstMessage() {
         return firstMessage;
     }

    public void setFirstMessage(String firstMessage) {
        this.firstMessage = firstMessage;
    }

     public void processCommands(String value){
            if(value.startsWith("/leave")){
                user.leave(value);
                /*user.setReadyToChat(false);
                user.setPromptForRelax(true);
                if(recipientClient!=null){
                recipientClient.getUser().setReadyToChat(false);
                recipient.sendMessage(String.format("/left %s", user.getName()));}//String.format("%s has just left the chat./", name)
                clientLogger.info(user.getStatus()+" "+user.getName()+" has ended the chat");*/
            }
            else if(value.startsWith("/exit")){
                user.exit(value);
                /*user.setReadyToChat(false);
                if(recipientClient!=null){
                    recipientClient.getUser().setReadyToChat(false);
                recipient.sendMessage(String.format("/out %s", user.getName()));}*/
               handlingUser.sendMessage("/exit");
                user.setExit(true);
                //System.out.println(status+" "+name+" has exit the program at "+new Date());

                }
                else if(value.equals("/endOfChat")) System.out.println(value);

     }

     @Override
     public synchronized void onConnectionReady(Connection connection) {
         connection.sendMessage("Client connected: "+connection);
         clientLogger.info("Client connected: "+connection);

     }

     @Override
     public synchronized void onRegistration(Connection handlingUser) throws IOException {

            String name, status, maxConnections;
             handlingUser.sendMessage("REGISTRATION... ");
           String [] parameters = registrationParser(handlingUser.recieveSingleMessage());
             name = parameters[0];
             status=parameters[1];
             maxConnections=parameters[2];

            switch (status){
                case "agent":

                    user = maxConnections.equals("1")? new Agent(name, status) : new Agent(name, status, maxConnections);
                    StatisticsHolder.allAgents.put(id,this);

                    handlingUser.sendMessage("Registration completed. Good day "+name);
                       clientLogger.info("Status of the user: "+status+" name: "+name+" Registration completed");
                    break;
                case "client":
                    user = new Customer(name,status);
                    StatisticsHolder.allClients.put(id,this);
                    handlingUser.sendMessage("Registration completed. Good day "+name);
                       clientLogger.info("Status of the user: "+status+" name: "+name+" Registration completed");
                    break;
                default:
                    clientLogger.info("access denied. Wrong registration parameters "+ name+" "+ status);
                    handlingUser.sendMessage("/access denied");
                    user = new Customer(name, status);
                    user.setExit(true);
                    break;
            }
     }

     @Override
     public synchronized void onReciveMessage(String value) {
         firstMessage = value;
         /*try {
             Thread.sleep(30000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }*/
         if(value.startsWith("/")){
             processCommands(value);//данный метод обрабатывает команды с консоли
         }
            else
         sendToAllConnections(firstMessage);

     }

     @Override
     public synchronized void onDisconnect(Connection connection) {

         //sendToAllConnections(user.getStatus()+" "+user.getName()+" disconnected: "+connection.toString());
         if(user!=null){
         clientLogger.info(user.getStatus()+" "+user.getName()+" disconnected "+handlingUser);
         connection.disconnect();
         handlingUser = null;
         user.setExit(true);
         switch (user.getStatus()){
             case "agent":
                 StatisticsHolder.allAgents.remove(this.id);
                 break;
             case "client":
                 StatisticsHolder.allClients.remove(this.id);
                 StatisticsHolder.removeChat(this.id);
                 break;
         }
             /*try {
                 Thread.sleep(30000);
                 System.out.println("Ending Connection");
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }*/
         }//проверка на ноль user
        else {clientLogger.info("UNKNOWN user disconnected "+handlingUser);
         connection.disconnect();
             handlingUser = null;
         }

        }

     @Override
     public synchronized void onException(Connection connection, Exception e) {
            if(e.getClass().getSimpleName().equals("SocketTimeoutException")){
                clientLogger.info("TimeOut: waiting time has been exceeded "+handlingUser);
            }
            else {
         clientLogger.error(user.getStatus()+" "+user.getName()+" Exception occurred "+e);
         e.printStackTrace();}
         if(user!=null)//данная проверка необходима чтобы не воодить дополнитлеьную обработку исключения выкинутого в методе onRegistratio в случае когда превышено время ожидания до регистрации и инициализации USer
         user.setExit(true);

         //onDisconnect(connection); тут

     }

     private void sendToAllConnections(String message){
         System.out.println(user.getName()+" "+message);

         if(handlingUser!=null)
             handlingUser.sendMessage(user.getStatus()+" "+user.getName()+" : "+message);
             user.sendMessageByID(message);//проверка соединений на ноль осуществляется внутри метода

     }
    private String[] registrationParser(String registrationMessage){

            String[] tempParam = registrationMessage.split(" ");
            String[] param = new String[3];
            if(tempParam.length<3){
                param[0]="Dear user";}
                else  param[0]=tempParam[1];
            int statusVsConnections = tempParam[tempParam.length-1].lastIndexOf("$");
        System.out.println(statusVsConnections);
            if(statusVsConnections==-1){
                param[1]=tempParam[tempParam.length-1].toLowerCase();
                param[2]="1";
            }
            else{param[1]=tempParam[tempParam.length-1].substring(0, statusVsConnections);
            if(tempParam[tempParam.length-1].substring(statusVsConnections).length()>1){
            param[2] = tempParam[tempParam.length-1].substring(statusVsConnections+1);}
            else param[2]="1";
            }


        System.out.println("name "+param[0]);
        System.out.println("status "+param[1]);
        System.out.println("maxcon"+param[2]);
            return param;
    }

    @Override
    public String toString() {
            StringBuilder bd = new StringBuilder();
            String separator = System.getProperty("line.separator");
            bd.append("\"ClientHandler\":{"+separator);
            bd.append("\"id\": \""+id+"\""+separator);
            bd.append("\"user\": \""+user.getName()+"\""+separator);
            bd.append("}");
        return bd.toString();
    }

    void stopClientHandlersThread(){
            if(user!=null){
                user.setReadyToChat(false);
                user.setExit(true);
            }
            if(handlingUser!=null)
            handlingUser.setSoTimeout(1000);
    }


}
