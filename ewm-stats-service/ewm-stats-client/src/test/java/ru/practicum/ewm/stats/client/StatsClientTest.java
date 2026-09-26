package ru.practicum.ewm.stats.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StatsClientTest {
    @Test
    void sendsHitAndBuildsStatsRequest() {
        RestTemplate template = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(template).build();
        StatsClient client = new StatsClient("http://stats", template);
        server.expect(requestTo("http://stats/hit")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"app\":\"main\",\"uri\":\"/x\",\"ip\":\"127.0.0.1\",\"timestamp\":\"2025-01-01 00:00:00\"}"))
                .andRespond(withSuccess());
        server.expect(requestTo("http://stats/stats?start=2025-01-01%2000:00:00&end=2025-01-02%2000:00:00&unique=true&uris=/x"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"app\":\"main\",\"uri\":\"/x\",\"hits\":1}]", MediaType.APPLICATION_JSON));
        client.saveHit(new EndpointHit(null, "main", "/x", "127.0.0.1", "2025-01-01 00:00:00"));
        assertThat(client.getStats(LocalDateTime.parse("2025-01-01T00:00:00"),
                LocalDateTime.parse("2025-01-02T00:00:00"), List.of("/x"), true)).hasSize(1);
        server.verify();
    }
}
