import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherServer {
    private static final String API_KEY = "YOUR_API_KEY"; // OpenWeatherMap API key

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/weather", new WeatherHandler());
        server.setExecutor(null);

        System.out.println("Server running on http://localhost:" + port);
        server.start();
    }

    static class WeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod()) && "/weather".equals(exchange.getRequestURI().getPath())) {
                try {
                    // Read request body
                    int contentLength = Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length"));
                    InputStream inputStream = exchange.getRequestBody();
                    byte[] requestData = inputStream.readNBytes(contentLength);
                    String requestBody = new String(requestData, StandardCharsets.UTF_8);

                    JSONObject data = new JSONObject(requestBody);
                    String city = data.optString("city", "");

                    // Fetch weather data from OpenWeatherMap API
                    String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
                    HttpURLConnection apiConnection = (HttpURLConnection) new URL(urlString).openConnection();
                    apiConnection.setRequestMethod("GET");

                    int responseCode = apiConnection.getResponseCode();
                    InputStream apiResponseStream = (responseCode == 200) ? apiConnection.getInputStream() : apiConnection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(apiResponseStream));

                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    JSONObject weatherData = new JSONObject(responseBuilder.toString());

                    if (responseCode == 200) {
                        JSONObject mainData = weatherData.getJSONObject("main");
                        double temperature = mainData.getDouble("temp"); // Extract temperature
                        int humidity = mainData.getInt("humidity"); // Extract humidity
                        String description = weatherData.getJSONArray("weather").getJSONObject(0).getString("description"); // Extract description

                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("City", city);
                        jsonResponse.put("Temperature", String.format("%.2f", temperature) + "°C");
                        jsonResponse.put("Humidity", humidity + "%");
                        jsonResponse.put("Description", description);

                        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                    } else {
                        String errorMessage = weatherData.optString("message", "Invalid city");
                        exchange.sendResponseHeaders(400, errorMessage.length());
                        exchange.getResponseBody().write(errorMessage.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    String errorMessage = "Internal Server Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, errorMessage.length());
                    exchange.getResponseBody().write(errorMessage.getBytes(StandardCharsets.UTF_8));
                } finally {
                    exchange.getResponseBody().close();
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
}

