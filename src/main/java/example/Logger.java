package example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

    //Статический метод для логирования сообщений.
    public static void log(String logPath, String userName, String msg) {
        String log = LocalDateTime.now() + "[" + userName + "]: " + msg + "\n";
        try (FileWriter logWriter = new FileWriter(logPath, true)) {
            logWriter.write(log);
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Ошибка при записи в лог: " + logPath);
            e.printStackTrace();
        }
    }
}
