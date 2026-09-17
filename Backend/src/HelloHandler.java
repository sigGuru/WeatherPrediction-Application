import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HelloHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Response message
        String response = "Welcome to the Wheater App!!";

        // Send response headers (200 OK) and length
        exchange.sendResponseHeaders(200, response.length());

        //send Response body

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
