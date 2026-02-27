import example.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {

    @Test
    @DisplayName("getNewClientThread должен корректно обрабатывать клиента")
    void getNewClientThread_ShouldHandleClient() throws Exception {
        // Arrange
        String clientInput = "TestUser\nHello\n/exit\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(clientInput.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Создаём "поддельный" сокет с нашими потоками
        Socket fakeSocket = new Socket() {
            {
                Object impl = null; // Чтобы не инициализировался настоящий сокет
            }

            @Override
            public InputStream getInputStream() {
                return inputStream;
            }

            @Override
            public OutputStream getOutputStream() {
                return outputStream;
            }

            @Override
            public int getPort() {
                return 50000;
            }
        };

        // Act
        Thread thread = Server.getNewClientThread(fakeSocket);
        thread.start();
        thread.join(5000); // Ждём завершения

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Connection successful! Write your name"));
        assertTrue(output.contains("Hi, TestUser, your port: 50000"));
        assertTrue(output.contains("Saved message: Hello"));
        assertTrue(output.contains("Disconnecting from server"));
    }
}