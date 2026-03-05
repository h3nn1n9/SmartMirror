package main;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * MicroProfile REST Client for the OpenWeatherMap API.
 * Base URL is configured via application.properties:
 *   quarkus.rest-client.openweather.url=https://api.openweathermap.org
 */
@RegisterRestClient(configKey = "openweather")
@Produces(MediaType.APPLICATION_JSON)
@Path("/data/2.5")
public interface WeatherClient {

    @GET
    @Path("/weather")
    WeatherResponse getWeather(@QueryParam("id") String cityId,
                               @QueryParam("APPID") String apiKey);

    // Response records — Jackson deserialises into these directly.
    // The JSON field "main" conflicts with our package name, so we use
    // @JsonProperty to map it to a differently-named record component.
    record WeatherResponse(
            List<Condition> weather,
            Sys sys,
            @JsonProperty("main") MainBlock mainBlock
    ) {
        record Condition(String main) {}
        record Sys(long sunset) {}
        record MainBlock(double temp) {}
    }
}
