package main;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@ApplicationScoped
public class WeatherService {

    private static final double KELVIN_OFFSET = 273.15;

    @RestClient
    WeatherClient weatherClient;

    @ConfigProperty(name = "openweather.city.id")
    String cityId;

    @ConfigProperty(name = "openweather.api.key")
    String apiKey;

    public WeatherData load() {
        var response = weatherClient.getWeather(cityId, apiKey);
        var condition = response.weather().get(0).main();
        var sunsetTime = Instant.ofEpochSecond(response.sys().sunset())
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        double tempCelsius = Math.round((response.mainBlock().temp() - KELVIN_OFFSET) * 10) / 10.0;
        return new WeatherData(condition, tempCelsius, sunsetTime);
    }

    public record WeatherData(String condition, double tempCelsius, LocalTime sunsetTime) {}
}
