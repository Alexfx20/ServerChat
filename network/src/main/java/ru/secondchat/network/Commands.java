package ru.secondchat.network;

/*
*All commands conditionally are divided into groups: "Command" and "Notification"
*"Command" - commands which are expected to be given by the user, e.g. user types "/exit" as the result all active chats with this user are closed as well as the client program
*"Notifications" - these commands are the part of the server behavior protocol and are invoked only by a server as the reaction on exceptions or user's commands from "Command" group
* Notification-like messages are forbidden to be sent by users, cause insufficient appearance of such messages could lead to unexpected behavior of the server.
* Any attempt to send message like "/endOfChat" etc. would simply be ignored by the server and nor of the chat participants would receive such message.
 */
public enum Commands {

    COMMAND_IDENTIFIER("/"),
    REGISTER("/register"),  //Notification invokes the registration of new user
    EXIT("/exit"),          //Command invokes user's exit of chat and program
    LEAVE("/leave"),        //Command invokes chat closing and putting the users on a waiting queue.
    LEAVE_ALL("/leaveAll"), //Command invokes agent's end of all chats with all customers simultaneously
    END_OF_CHAT("/endOfChat"),  //Notification. notifies that conversation is over and invokes ClientHandler thread to put the user in the waiting queue
    OUT("/out"),                //Notification. notifies that interlocutor has just exit the program.
    LEFT("/left"),              //Notification notifies that int
    ACCESS_DENIED("/access denied"), //Notification notifies that registration process has been failed
    TIME_OUT("/Timeout");       //Notification notifies that idle time has been exceeded and socket connection was interrupted by the server

    String command;

    Commands(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }



}
