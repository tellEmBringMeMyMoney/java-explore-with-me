package ru.practicum.ewm.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this(serverUrl, new RestTemplate());
    }

    public StatsClient(String serverUrl, RestTemplate restTemplate) {
        this.serverUrl = serverUrl.replaceAll("/$", "");
        this.restTemplate = restTemplate;
    }

    public void saveHit(EndpointHit hit) {
        restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serverUrl + "/stats")
                .queryParam("start", FORMAT.format(start))
                .queryParam("end", FORMAT.format(end))
                .queryParam("unique", unique);
        if (uris != null) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }
        URI requestUri = builder.build().encode().toUri();
        ResponseEntity<ViewStats[]> response = restTemplate.exchange(requestUri, HttpMethod.GET,
                HttpEntity.EMPTY, ViewStats[].class);
        ViewStats[] body = response.getBody();
        return body == null ? List.of() : List.of(body);
    }
}
