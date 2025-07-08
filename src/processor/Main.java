package processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        HighLoadProcessor highLoadProcessor = new HighLoadProcessor();
        for (int i = 0; i < 10; i++) {
            InputStream inputStream = new ByteArrayInputStream(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
            highLoadProcessor.processRequest(inputStream);
        }
    }
}
