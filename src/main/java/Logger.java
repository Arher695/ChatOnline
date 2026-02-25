import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

//Класс логгера - синглтона
public class Logger {
    //Ленивая реализация
    private static Logger INSTANCE = null;

    //Приватный конструктор, никто не сможет создавать объекты логгера через new Logger();
    private Logger() {
    }

    //Позволит создать логгер в единственном экземпляре
    public static Logger getInstance() {
        if (INSTANCE == null) {
            //Вторая проверка, чтобы не попасть в ловушку многопоточки и не создать два логгера
            synchronized (Logger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Logger();
                }
            }
        }
        return INSTANCE;
    }

    //Метод логирования в файл с указанием имени и текста сообщения
    public void log(String logPath, String userName, String msg) {
        String log = LocalDateTime.now() + "  " + userName + "  " + msg + "\n";
        try (FileWriter logWriter = new FileWriter(logPath, true)) {
            logWriter.write(log);
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
            ;
        }
    }
}
