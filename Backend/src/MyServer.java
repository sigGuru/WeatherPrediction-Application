import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyServer {
    public static void main(String[] args) throws IOException {

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000),0);

        httpServer.setExecutor(null);
        System.out.println("Server running at http://localhost:8000");
        httpServer.start();
    }
}
