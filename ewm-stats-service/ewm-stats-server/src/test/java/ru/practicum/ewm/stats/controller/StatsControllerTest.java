package ru.practicum.ewm.stats.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private StatsService service;

    @Test
    void createsHitWith201AndRejectsInvalidPayload() throws Exception {
        mvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"app":"main","uri":"/events/1","ip":"127.0.0.1","timestamp":"2025-01-01 12:00:00"}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"app\":\"\",\"uri\":\"/x\",\"ip\":\"127.0.0.1\",\"timestamp\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsStatsAndBindsQueryParameters() throws Exception {
        when(service.getStats(any(), any(), eq(List.of("/events")), eq(true)))
                .thenReturn(List.of(new ViewStats("main", "/events", 2L)));
        mvc.perform(get("/stats").param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-01-02 00:00:00").param("uris", "/events").param("unique", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].hits").value(2));
        verify(service).getStats(any(LocalDateTime.class), any(LocalDateTime.class), eq(List.of("/events")), eq(true));
    }
}
