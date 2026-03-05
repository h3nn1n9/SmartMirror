package main;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class NewsService {

    private static final int MAX_HEADLINES = 6;

    @RestClient
    NewsClient newsClient;

    @ConfigProperty(name = "news.api.key")
    String apiKey;

    @ConfigProperty(name = "news.country", defaultValue = "de")
    String country;

    public List<String> getHeadlines() {
        return newsClient.getTopHeadlines(country, apiKey)
                .articles()
                .stream()
                .limit(MAX_HEADLINES)
                .map(a -> a.title() + "  —  " + a.source().name())
                .toList();
    }
}
