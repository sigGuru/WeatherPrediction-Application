import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import java.nio.charset.StandardCharsets;


public class WeatherServer {

    private static final String API_KEY =
            "YOUR_NEW_API_KEY";


    public static void main(String[] args)
            throws IOException {

        int port = 8080;


        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(
                                "0.0.0.0",
                                port
                        ),
                        0
                );


        server.createContext(
                "/weather",
                new WeatherHandler()
        );


        server.setExecutor(null);


        System.out.println(
                "Server running on http://localhost:"
                        + port
        );


        server.start();
    }


    static class WeatherHandler
            implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange)
                throws IOException {


            // ==============================
            // CORS
            // ==============================

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Origin",
                    "http://127.0.0.1:3000"
            );

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Methods",
                    "POST, OPTIONS"
            );

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Headers",
                    "Content-Type"
            );


            // ==============================
            // OPTIONS
            // ==============================

            if ("OPTIONS".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                exchange.sendResponseHeaders(
                        204,
                        -1
                );

                exchange.close();

                return;
            }


            // ==============================
            // POST /weather
            // ==============================

            if ("POST".equalsIgnoreCase(
                    exchange.getRequestMethod())
                    && "/weather".equals(
                    exchange.getRequestURI().getPath())) {

                try {

                    InputStream inputStream =
                            exchange.getRequestBody();


                    String requestBody =
                            new String(
                                    inputStream.readAllBytes(),
                                    StandardCharsets.UTF_8
                            );


                    System.out.println(
                            "Request: " + requestBody
                    );


                    JSONObject data =
                            new JSONObject(requestBody);


                    String city =
                            data.optString(
                                    "city",
                                    ""
                            ).trim();


                    if (city.isEmpty()) {

                        JSONObject error =
                                new JSONObject();

                        error.put(
                                "error",
                                "City name is required"
                        );


                        sendJsonResponse(
                                exchange,
                                400,
                                error.toString()
                        );

                        return;
                    }


                    // ==============================
                    // OpenWeather API
                    // ==============================

                    String urlString =
                            "https://api.openweathermap.org/data/2.5/weather"
                                    + "?q=" + city
                                    + "&appid=" + API_KEY
                                    + "&units=metric";


                    HttpURLConnection connection =
                            (HttpURLConnection)
                                    new URL(urlString)
                                            .openConnection();


                    connection.setRequestMethod("GET");


                    int responseCode =
                            connection.getResponseCode();


                    InputStream apiStream =
                            responseCode == 200
                                    ? connection.getInputStream()
                                    : connection.getErrorStream();


                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            apiStream,
                                            StandardCharsets.UTF_8
                                    )
                            );


                    StringBuilder builder =
                            new StringBuilder();


                    String line;

                    while ((line =
                            reader.readLine()) != null) {

                        builder.append(line);
                    }


                    reader.close();

                    connection.disconnect();


                    JSONObject weatherData =
                            new JSONObject(
                                    builder.toString()
                            );


                    // ==============================
                    // SUCCESS
                    // ==============================

                    if (responseCode == 200) {

                        JSONObject main =
                                weatherData.getJSONObject(
                                        "main"
                                );


                        double temperature =
                                main.getDouble(
                                        "temp"
                                );


                        int humidity =
                                main.getInt(
                                        "humidity"
                                );


                        String description =
                                weatherData
                                        .getJSONArray(
                                                "weather"
                                        )
                                        .getJSONObject(0)
                                        .getString(
                                                "description"
                                        );


                        JSONObject response =
                                new JSONObject();


                        response.put(
                                "City",
                                city
                        );


                        response.put(
                                "Temperature",
                                String.format(
                                        "%.2f°C",
                                        temperature
                                )
                        );


                        response.put(
                                "Humidity",
                                humidity + "%"
                        );


                        response.put(
                                "Description",
                                description
                        );


                        sendJsonResponse(
                                exchange,
                                200,
                                response.toString()
                        );


                    } else {

                        // ==============================
                        // API ERROR
                        // ==============================

                        String message =
                                weatherData.optString(
                                        "message",
                                        "Invalid city"
                                );


                        JSONObject error =
                                new JSONObject();


                        error.put(
                                "error",
                                message
                        );


                        sendJsonResponse(
                                exchange,
                                responseCode,
                                error.toString()
                        );
                    }


                } catch (Exception e) {

                    e.printStackTrace();


                    JSONObject error =
                            new JSONObject();


                    error.put(
                            "error",
                            "Internal Server Error"
                    );


                    sendJsonResponse(
                            exchange,
                            500,
                            error.toString()
                    );


                } finally {

                    exchange.close();
                }


            } else {

                JSONObject error =
                        new JSONObject();


                error.put(
                        "error",
                        "Endpoint not found"
                );


                sendJsonResponse(
                        exchange,
                        404,
                        error.toString()
                );
            }
        }


        // =================================================
        // Send JSON response
        // =================================================

        private static void sendJsonResponse(
                HttpExchange exchange,
                int statusCode,
                String response
        ) throws IOException {

            byte[] bytes =
                    response.getBytes(
                            StandardCharsets.UTF_8
                    );


            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );


            exchange.sendResponseHeaders(
                    statusCode,
                    bytes.length
            );


            try (OutputStream outputStream =
                         exchange.getResponseBody()) {

                outputStream.write(bytes);
            }
        }
    }
}