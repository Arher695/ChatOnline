package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String userName;
    private static final String CLIENT_LOGS = "src/main/resources/client_logs.log"; //Путь для логирования клиентов
    private static final String HOST = JsonParser.parseJson("host"); //Получаем адрес хоста из файла
    private static final int PORT = Integer.parseInt(JsonParser.parseJson("port")); //Получаем порт из файла

    private static final String CONNECTION_MESSAGE = "New connection to the server. Port: %d";
    private static final String SET_USER_NAME_MESSAGE = "Added the user's name";
    private static final String SEND_MESSAGE = "Sent a message: %s";
    private static final String USER_DISCONNECT_MESSAGE = "User %s has disconnected from the server (Port: %d) %n";

    private PrintWriter out;
    private BufferedReader in;
    private int serverPort;
    private final Scanner scanner = new Scanner(System.in);

    public Client() {
        try {
            Socket clientSocket = new Socket(HOST, PORT); //Подключаемся к серверу
            out = new PrintWriter(clientSocket.getOutputStream(), true); //Поток вывода
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //Входной поток

            serverPort = clientSocket.getPort(); //Получаем порт сервера
            Logger.log(CLIENT_LOGS, Server.getServerName(), String.format(CONNECTION_MESSAGE, serverPort)); //Логируем в файл клиента

            Thread readerThread = getReaderThread(); //Создаем поток для чтения с сервера
            readerThread.start(); //Запускаем

            Thread senderThread = getSenderThread(); //Создаем поток для отправки на сервер
            senderThread.start(); //Запускаем

        } catch (IOException e) {
            System.err.println("Couldn't connect to the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setUserName(String userName) {
        if (Client.userName == null) {
            Client.userName = userName;
        }
    }

    public static String getUserName() {
        return userName;
    }

    public Thread getReaderThread() { //Поток для чтения с сервера
        return new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                    if (msg.equals("Disconnecting from server")) {
                        break;
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    e.printStackTrace();
                }
            } finally {
                closeResources();
            }
        });
    }

    public Thread getSenderThread() { //Поток для отправки на сервер
        return new Thread(() -> {
            try {
                String name = scanner.nextLine();
                setUserName(name);
                Logger.log(CLIENT_LOGS, getUserName(), SET_USER_NAME_MESSAGE);
                out.println(getUserName());

                String msg;
                while ((msg = scanner.nextLine()) != null) {
                    out.println(msg);
                    Logger.log(CLIENT_LOGS, getUserName(), String.format(SEND_MESSAGE, msg));

                    if (msg.equalsIgnoreCase("/exit")) {
                        Logger.log(CLIENT_LOGS, "SERVER", String.format(USER_DISCONNECT_MESSAGE, getUserName(), serverPort));
                        break;
                    }
                }
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    e.printStackTrace();
                }
            } finally {
                closeResources();
            }
        });
    }

    private void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

