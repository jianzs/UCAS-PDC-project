import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class Utils {
    private static final String HOST = "127.0.0.1";
    public static final String SEPARATOR = "!@#";
    public static final Integer PORT_INIT = 7000;

    public static void sendMsg(Integer id, int port, String msg) throws IOException {
        Socket client = new Socket(HOST, port);
        Writer writer = new OutputStreamWriter(client.getOutputStream());
        writer.write(id + SEPARATOR + msg);
        writer.flush();
        writer.close();
        client.close();
    }
}
