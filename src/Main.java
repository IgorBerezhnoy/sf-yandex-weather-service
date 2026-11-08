import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("YANDEX_ACCESS_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("API ключ не найден в .env (YANDEX_ACCESS_KEY)");
            return;
        }

        double lat = 55.75;
        double lon = 37.62;
        int limit = 5;

        String url = String.format(
                "https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d",
                lat, lon, limit);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP статус: " + response.statusCode());

        String body = response.body();
        System.out.println(body);

        JsonObject root = JsonParser.parseString(body).getAsJsonObject();

        int currentTemp = root.getAsJsonObject("fact").get("temp").getAsInt();
        System.out.println("Текущая температура (fact.temp): " + currentTemp + " °C");

        JsonArray forecasts = root.getAsJsonArray("forecasts");
        int days = Math.min(limit, forecasts.size());
        double sum = 0;

        for (int i = 0; i < days; i++) {
            double tempAvg = forecasts.get(i).getAsJsonObject()
                    .getAsJsonObject("parts")
                    .getAsJsonObject("day")
                    .get("temp_avg")
                    .getAsDouble();
            sum += tempAvg;
        }

        double average = sum / days;
        System.out.printf("Средняя температура за %d дней равна: %.1f °C%n",
                days, average);
    }
}
