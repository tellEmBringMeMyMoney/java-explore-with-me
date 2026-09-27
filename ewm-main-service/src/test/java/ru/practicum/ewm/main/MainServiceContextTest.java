package ru.practicum.ewm.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MainServiceContextTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository users;
    @Autowired
    private CategoryRepository categories;
    @Autowired
    private EventRepository events;
    @Autowired
    private CompilationRepository compilations;
    @Autowired
    private RequestRepository requests;

    @Test
    void startsWithRepositoriesAndJpaMappings() {
        assertNotNull(users);
        assertNotNull(categories);
        assertNotNull(events);
        assertNotNull(compilations);
        assertNotNull(requests);
    }

    @Test
    void rejectsNonStringNamesInsteadOfCoercingJsonNumbers() throws Exception {
        mvc.perform(post("/admin/categories").contentType(APPLICATION_JSON).content("{\"name\":12}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value("BAD_REQUEST"));
        mvc.perform(post("/admin/users").contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Valid User\",\"email\":12}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void eventPublicationRequestsAndCategoryConstraintsWorkThroughHttp() throws Exception {
        long userId = create("admin/users", Map.of("name", "Owner User", "email", "owner@example.org"));
        long requesterId = create("admin/users", Map.of("name", "Guest User", "email", "guest@example.org"));
        long categoryId = create("admin/categories", Map.of("name", "Integration"));
        String date = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> event = Map.of("annotation", "This is a long enough event annotation",
                "description", "This is a long enough event description for API integration test",
                "eventDate", date, "category", categoryId, "location", Map.of("lat", 55.75, "lon", 37.61),
                "title", "Integration event", "paid", false, "participantLimit", 1,
                "requestModeration", true);
        long eventId = create("users/" + userId + "/events", event);

        mvc.perform(get("/events/{id}", eventId)).andExpect(status().isNotFound());
        mvc.perform(patch("/admin/events/{eventId}", eventId).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of("stateAction", "PUBLISH_EVENT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("PUBLISHED"));
        mvc.perform(get("/events").param("categories", String.valueOf(categoryId)).param("from", "0").param("size", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(eventId));
        mvc.perform(post("/users/{userId}/requests", requesterId).param("eventId", String.valueOf(eventId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(post("/users/{userId}/requests", requesterId).param("eventId", String.valueOf(eventId)))
                .andExpect(status().isConflict());
        mvc.perform(post("/users/{userId}/requests", userId).param("eventId", String.valueOf(eventId)))
                .andExpect(status().isConflict());
        mvc.perform(delete("/admin/categories/{catId}", categoryId)).andExpect(status().isConflict());
        mvc.perform(get("/events").param("from", "-1").param("size", "10"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    private long create(String path, Object body) throws Exception {
        String response = mvc.perform(post("/" + path).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
