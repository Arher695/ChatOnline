import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Установка имени
            this.userName = requestValidName();
            if (userName == null) {
                out.println("Не удалось установить имя.");
                return;
            }

            Server.clients.add(this);
            String joinMsg = String.format(Server.JOIN_MESSAGE, userName);
            System.out.println(joinMsg);
            Server.broadcast("Сервер", joinMsg);

            // Приём сообщений
            String msg;
            while ((msg = in.readLine()) != null) {
                if ("/exit".equalsIgnoreCase(msg)) {
                    break;
                }
                Server.broadcast(userName, msg);
            }
        } catch (IOException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private String requestValidName() throws IOException {
        for (int i = 0; i < 3; i++) {
            out.println("Введите ваше имя:");
            String input = in.readLine();
            if (input == null || input.trim().isEmpty()) {
                out.println("Имя не может быть пустым.");
                continue;
            }
            String name = input.trim();

            if (Server.registerName(name)) {
                return name;
            } else {
                out.printf("Имя '%s' уже занято. Введите другое:%n", name);
            }
        }
        return null;
    }

    private void cleanup() {
        Server.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        }
    }

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
        }
    }

    public String getUserName() {
        return userName;
    }
}

