package main;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * MicroProfile REST Client for the NewsAPI.
 * Base URL is configured via application.properties:
 *   quarkus.rest-client.newsapi.url=https://newsapi.org
 */
@RegisterRestClient(configKey = "newsapi")
@Produces(MediaType.APPLICATION_JSON)
@Path("/v2")
public interface NewsClient {

    @GET
    @Path("/top-headlines")
    NewsResponse getTopHeadlines(@QueryParam("country") String country,
                                 @QueryParam("apiKey") String apiKey);

    record NewsResponse(int totalResults, List<Article> articles) {
        record Article(String title, Source source) {}
        record Source(String name) {}
    }
}
