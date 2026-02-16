package JsonParser;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JsonParser {
    //Путь к настройкам
    private static final String settingPath = "srs/main/java/resource/setting.json";

    public static String parseJson(String key) {
        try {
            //Парсим в Object
            Object object = new JSONParser().parse(new FileReader(settingPath));
            //преобразуем к JSONObject
            JSONObject jsonObject = (JSONObject) object;
            //Отдаем строку по ключу
            return (String) jsonObject.get(key);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
