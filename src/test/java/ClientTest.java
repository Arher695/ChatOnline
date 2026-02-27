import example.Client;
import example.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {
    @AfterEach
    void resetUserName() throws Exception {
        Field userNameField = Client.class.getDeclaredField("userName");
        userNameField.setAccessible(true);
        userNameField.set(null, null);
    }

    @Test
    public void portTest() {
        // arrange
        int expected = 8089;

        // act
        int result = Integer.parseInt(JsonParser.parseJson("port"));

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void hostTest() {
        // arrange
        String expected = "127.0.0.1";

        // act
        String result = JsonParser.parseJson("host");

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void setNameTest() {
        // arrange
        Client.setUserName("Ivan");
        String expected = "Ivan";

        // act
        String result = Client.getUserName();

        // assert
        assertEquals(expected, result);
    }
}
