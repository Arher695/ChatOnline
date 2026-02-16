package Server;

import JsonParser.JsonParser;
import Logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;


public class Server {
    //Имя сервера
    private static final String SERVER_NAME = "ServerName";
    //Создаем логгер
    private static final Logger LOGGER = Logger.getInstance();
    //Путь для логгирования сервера
    private static final String SERVER_LOGS = "srs/main/java/resources/server_logs.log";
    //Получаем порт из файла
    private static final int PORT = Integer.parseInt(Objects.requireNonNull(JsonParser.parseJson("port")));
    //Сообщение о старте сервера
    private static final String STARTING_MESSAGE = "Сервер запустился. Ждите соединения";
    //Сообщение о новом подключении
    private static final String NEW_CONNECTION_MESSAGE = "Соединение установленно. Порт: %d %n";
    //Сообщение о новом пользователе (после добавлении имени)
    private static final String NEW_USER_MESSAGE = "Новый пользователь. Порт: %d";
    //Сообщение о разъединении
    private static final String USER_DISCONNECT_MESSAGE = "Пользователь %s (Port: %d) разъединён";

    public static String getServerName() {
        return SERVER_NAME;

    }

    public static void main(String[] args) {
        try {
            //Создаем сервер сокет
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println(STARTING_MESSAGE);
            //Логгируем в файл-сервера
            LOGGER.log(SERVER_LOGS, SERVER_NAME, STARTING_MESSAGE);
            //Ждем подключения в бесконечном цикле
            while (true) {
                //Подключаем клиента
                Socket clientSocket = serverSocket.accept();
                //Для каждого нового клиента создаем отдельный поток
                Thread newClientThread = getNewClientThread(clientSocket);
                //Запускаем поток клиента, а основной поток ждет нового клиента
                newClientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Thread getNewClientThread(Socket clientSocket) {
        return new Thread(() -> {
            //Поток вывода
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 //Входной поток
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                //Получаем порт клиента
                int clientPort = clientSocket.getPort();
                System.out.printf(NEW_CONNECTION_MESSAGE, clientPort);
                LOGGER.log(SERVER_LOGS, SERVER_NAME, String.format(NEW_CONNECTION_MESSAGE, clientPort));
                out.println("Соединение установленно. Напишите своё имя: ");
                //Получаем имя клиента
                final String USER_NAME = in.readLine();
                out.printf("Привет %s, твой порт: %d", USER_NAME, clientPort);
                LOGGER.log(SERVER_LOGS, USER_NAME, String.format(NEW_USER_MESSAGE, clientPort));

                while (!clientSocket.isClosed()) {
                    out.println("Для выхода напишите: exit");
                    //Получаем сообщения
                    String msgFromClient = in.readLine();
                    //Логгируем его
                    LOGGER.log(SERVER_LOGS, USER_NAME, String.format(NEW_USER_MESSAGE, clientPort));
                    // Если клиент ввел exit
                    if (msgFromClient.equalsIgnoreCase("/exit")) {
                        //Отдаем сообщение о разъединении
                        out.println("Сервер отключён");
                        //Закрываем входной поток
                        in.close();
                        //Закрываем поток вывода
                        out.close();
                        //Закрываем сокет клиента
                        clientSocket.close();
                        LOGGER.log(SERVER_LOGS, SERVER_NAME, String.format(USER_DISCONNECT_MESSAGE, USER_NAME, clientPort));
                        break;
                    }
                    out.println("Вы вышли из чата написав " + msgFromClient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
