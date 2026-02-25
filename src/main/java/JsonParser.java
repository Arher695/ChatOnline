import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class JsonParser {
    //Путь к настройкам
    // private static final String settingPath = "srs/main/resource/setting.json";

    public static String parseJson(String key) {
        try {
            URL resource = JsonParser.class.getClassLoader().getResource("setting.json");
            if (resource == null) {
                System.err.println("Файл setting.json не найден в classpath");
                return null;
            }
            //Парсим в Object
            Object object = new JSONParser().parse(new FileReader(Objects.requireNonNull(resource).getFile()));
            //преобразуем к JSONObject
            JSONObject jsonObject = (JSONObject) object;
            //Отдаем строку по ключу
            return (String) jsonObject.get(key);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
