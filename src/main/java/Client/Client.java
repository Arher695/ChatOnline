package Client;

import JsonParser.JsonParser;
import Logger.Logger;
import Server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    private static String userName;
    //Создаем логгер
    private static final Logger LOGGER = Logger.getInstance();
    //Путь для логирования клиентов
    private static final String CLIENT_LOGS = "client_logs.log";
    //Получаем адрес хоста из файла
    private static final String HOST = JsonParser.parseJson("host");
    //Получаем порт из файла
    private static final int PORT = Integer.parseInt(Objects.requireNonNull(JsonParser.parseJson("port")));

    private static final String CONNECTION_MESSAGE = "Новое подключение к серверу. Порт: %d";
    private static final String SET_USER_NAME_MESSAGE = "Имя пользователя";
    private static final String SEND_MESSAGE = "Отправил сообщение: %s";
    private static final String USER_DISCONNECT_MESSAGE = "Пользователь %s отключился от сервера (Порт: %d) %n";

    private static PrintWriter out;
    private static BufferedReader in;
    private static int serverPort;
    private static final Scanner scanner = new Scanner(System.in);

    public Client() {
        try {
            //Подключаемся к серверу
            Socket clientSocket = new Socket(HOST, PORT);
            //Поток вывода
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            //Входной поток
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Получаем порт сервера
            serverPort = clientSocket.getPort();
            //Логгируем в файл клиента
            LOGGER.log (CLIENT_LOGS, Server.getServerName(), String.format(CONNECTION_MESSAGE, serverPort));
            //Создаем поток для чтения с сервера
            Thread readerThread = getReaderThread();
            //Запускаем
            readerThread.start();
            //Создаем поток для отправки на сервер
            Thread senderThread = getSenderThread();
            //Запускаем
            senderThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Thread getSenderThread() {
        //Поток для отправки на сервер
        return new Thread(() -> {
            //При первом обращении с сервером устанавливаем имя
            setUserName(scanner.nextLine());
            LOGGER.log(CLIENT_LOGS, getUserName(),SET_USER_NAME_MESSAGE);
            out.println(getUserName());
            //При первом обращении с сервером устанавливаем имя
            while (!Thread.currentThread().isInterrupted()) {
                //Читаем с консоли
                String msg = scanner.nextLine();
                //Отправляем на сервер
                out.println(msg);
                LOGGER.log(CLIENT_LOGS, getUserName(), String.format(SEND_MESSAGE, msg));
                //Если ввели /exit
                LOGGER.log(CLIENT_LOGS, Server.getServerName(), String.format(USER_DISCONNECT_MESSAGE, userName, serverPort));
                //Прерываем поток
                Thread.currentThread().interrupt();
            }
        });

    }

    public static Thread getReaderThread() {
        //Поток для чтения с сервера
        return new Thread(() -> {
            String msg;
            //Пока поток не прервали
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //Читаем с сервера
                    msg = in.readLine();
                    System.out.println(msg);
                    //если сообщение о разъединении
                    if (msg.equals("Сервер отключён")) {
                        //Прерываем поток
                        Thread.currentThread().interrupt();
                    }
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                    //Если прервали во время сна - выходим
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }

    public static void setUserName(String userName) {
        if (Client.userName == null) {
            Client.userName = userName;
        }
    }

    public static String getUserName() {
        return userName;
    }
}
