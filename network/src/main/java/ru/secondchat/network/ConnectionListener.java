package ru.secondchat.network;

public interface ConnectionListener {// интерфейс определяет методы жизненного цикла входящего соединения

    void onConnectionReady(Connection connection);// оповещает юзера об успешно установленном соединении
    void onRegistration(Connection connection);//опрашивает имя, статус, если все ок регистирирует юзера в системе
    void onReciveMessage(Connection connection, String value);//общение юзеров проиходит в данном методе
    void onDisconnect(Connection connection);
    void onException(Connection connection, Exception e);
    void processCommands(String value);
}
