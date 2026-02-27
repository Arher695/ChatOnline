package example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;

public class JsonParser {
    private static final String SETTINGS_PATH = "src/main/resources/settings.json"; //Путь к настройкам

    //Парсит JSON и возвращает значение по ключу.
    // Бросает RuntimeException, если ключ не найден или ошибка чтения.

    public static String parseJson(String key) {
        try {
            Object obj = new JSONParser().parse(new FileReader(SETTINGS_PATH)); //Парсим в Object
            JSONObject jsonObject = (JSONObject) obj; //Кастуем к JSONObject
            //Получаем строку по ключу
            String value = (String) jsonObject.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Ключ не найден в settings.json: " + key);
            }
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + SETTINGS_PATH, e);
        } catch (org.json.simple.parser.ParseException e) {
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }
}

