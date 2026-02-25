

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private static final String SERVER_NAME = "ServerChatOnline";
    private static final Logger LOGGER = Logger.getInstance();
    private static final String SERVER_LOGS = "server_logs.log";
    private static final String CHAT_HISTORY = "chat_history.log";
    private static final int PORT = Integer.parseInt(
            java.util.Objects.requireNonNull(JsonParser.parseJson("port"))
    );

    private static final String START_MESSAGE = "Сервер запущен. Ожидание подключений...";
    static final String JOIN_MESSAGE = "[СИСТЕМА] %s присоединился к чату";
    private static final String LEAVE_MESSAGE = "[СИСТЕМА] %s покинул чат";
    private static final String FORMAT_MESSAGE = "[%s] %s: %s";

    static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final Set<String> usedNames = Collections.synchronizedSet(new HashSet<>());
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    public static String getServerName() {
        return SERVER_NAME;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(START_MESSAGE);
            LOGGER.log(SERVER_LOGS, SERVER_NAME, START_MESSAGE);
            logChat("[СИСТЕМА] Сервер запущен на порту " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("Подключение #%d с порта: %d%n", clientCounter.incrementAndGet(), socket.getPort());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void broadcast(String sender, String msg) {
        String formatted = String.format(FORMAT_MESSAGE, new Date(), sender, msg);
        logChat(formatted);
        LOGGER.log(SERVER_LOGS, sender, msg);
        for (ClientHandler client : clients) {
            client.sendMessage(formatted);
        }
    }

    public static boolean registerName(String name) {
        return usedNames.add(name);
    }

    public static void unregisterName(String name) {
        usedNames.remove(name);
    }

    public static void removeClient(ClientHandler client) {
        if (client.getUserName() != null) {
            String name = client.getUserName();
            unregisterName(name);
            String msg = String.format(LEAVE_MESSAGE, name);
            System.out.println(msg);
            broadcast("Сервер", msg);
        }
        clients.remove(client);
    }

    private static synchronized void logChat(String msg) {
        try (FileWriter w = new FileWriter(CHAT_HISTORY, true)) {
            w.write(msg + "\n");
        } catch (IOException e) {
            System.err.println("Не удалось записать в историю: " + e.getMessage());
        }
    }
}

    /*//Имя сервера
    private static final String SERVER_NAME = "ServerChatOnline";
    //Создаем логгер
    private static final Logger LOGGER = Logger.getInstance();
    //Путь для логгирования сервера
//    private static final String SERVER_LOGS = "srs/main/resources/server_logs.log";
    private static final String SERVER_LOGS = "server_logs.log";
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
}*/
