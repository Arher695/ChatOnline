package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final String SERVER_NAME = "SERVER"; //Имя сервера
    private static final String SERVER_LOGS = "src/main/resources/server_logs.log"; //Путь для логирования сервера
    private static final int PORT = Integer.parseInt(JsonParser.parseJson("port")); //Получаем порт из файла

    private static final String STARTING_MESSAGE = "Server is running. Waiting for connections"; //Сообщение о старте сервера
    private static final String NEW_CONNECTION_MESSAGE = "New connection accepted. Port: %d %n"; //Сообщение о новом подключении
    private static final String NEW_USER_MESSAGE = "New user on server. Port: %d"; //Сообщение о новом пользователе (после добавлении имени)
    private static final String USER_DISCONNECT_MESSAGE = "User %s (Port: %d) was disconnected %n"; //Сообщение о дисконнекте

    public static String getServerName() {
        return SERVER_NAME;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT); //Создаем сервер сокет
            System.out.println(STARTING_MESSAGE);
            Logger.log(SERVER_LOGS, SERVER_NAME, STARTING_MESSAGE); //Логируем в файл сервера

            while (true) { //Ждем подключения в бесконечном цикле
                Socket clientSocket = serverSocket.accept(); //Подключаем клиента
                Thread clientThread = getNewClientThread(clientSocket); //Для каждого нового клиента создаем отдельный поток
                clientThread.start(); //Запускаем поток клиента, а основной поток ждет нового клиента
            }
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Thread getNewClientThread(Socket clientSocket) {
        return new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                int clientPort = clientSocket.getPort(); //Получаем порт клиента
                System.out.printf(NEW_CONNECTION_MESSAGE, clientPort);
                Logger.log(SERVER_LOGS, SERVER_NAME, String.format(NEW_CONNECTION_MESSAGE, clientPort));
                out.println("Connection successful! Write your name\n");

                String userName = in.readLine();
                if (userName == null) return; //Получаем имя клиента
                out.printf("Hi, %s, your port: %d%n", userName, clientPort);
                Logger.log(SERVER_LOGS, userName, String.format(NEW_USER_MESSAGE, clientPort));

                while (true) { //Пока сокет клиента работает
                    out.println("\nPrint message for logging. Print to exit: /exit");
                    String msg = in.readLine(); //Получаем сообщения
                    Logger.log(SERVER_LOGS, userName, ("Send message: " + msg)); //И логируем

                    if (msg == null || msg.equalsIgnoreCase("/exit")) { //Если клиент ввел '/exit'
                        out.println("Disconnecting from server"); //Отдаем сообщение о дисконнекте
                        Logger.log(SERVER_LOGS, SERVER_NAME, String.format(USER_DISCONNECT_MESSAGE, userName, clientPort));
                        break;
                    }
                    Logger.log(SERVER_LOGS, userName, "Send message: " + msg);
                    out.println("Saved message: " + msg);
                }
            } catch (IOException e) {
                if (!clientSocket.isClosed()) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
