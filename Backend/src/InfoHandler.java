import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class InfoHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        //Get request method
        String method = exchange.getRequestMethod();

        //get request headers
        String header = exchange.getRequestHeaders().toString();

        //Response message
        String response = "Request message: "+ method + "\nHeaders: "+ header;

        //send Response
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
