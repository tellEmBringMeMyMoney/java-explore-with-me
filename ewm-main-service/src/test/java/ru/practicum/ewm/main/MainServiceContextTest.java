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

    @Test
    void commentsCanBeCreatedEditedAndReactedTo() throws Exception {
        long ownerId = create("admin/users", Map.of("name", "Comment Owner", "email", "comment-owner@example.org"));
        long guestId = create("admin/users", Map.of("name", "Comment Guest", "email", "comment-guest@example.org"));
        long categoryId = create("admin/categories", Map.of("name", "Comment test"));
        String date = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> event = Map.of("annotation", "A sufficiently long event annotation",
                "description", "A sufficiently long event description for comments integration test",
                "eventDate", date, "category", categoryId, "location", Map.of("lat", 55.75, "lon", 37.61),
                "title", "Comments integration event", "paid", false, "participantLimit", 0,
                "requestModeration", false);
        long eventId = create("users/" + ownerId + "/events", event);
        String commentResponse = mvc.perform(post("/users/{userId}/events/{eventId}/comments", guestId, eventId)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"Great event\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.text").value("Great event"))
                .andExpect(jsonPath("$.reactions.fire").value(0)).andReturn().getResponse().getContentAsString();
        long commentId = objectMapper.readTree(commentResponse).get("id").asLong();
        mvc.perform(get("/events/{eventId}/comments", eventId)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author.id").value(guestId));
        mvc.perform(put("/users/{userId}/comments/{commentId}/reaction", guestId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"reaction\":\"fire\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reactions.fire").value(1));
        mvc.perform(put("/users/{userId}/comments/{commentId}/reaction", guestId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"reaction\":\"ahh\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reactions.fire").value(0))
                .andExpect(jsonPath("$.reactions.ahh").value(1));
        mvc.perform(put("/users/{userId}/comments/{commentId}/reaction", guestId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"reaction\":\"ahh\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reactions.ahh").value(1));
        mvc.perform(put("/users/{userId}/comments/{commentId}/reaction", guestId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"reaction\":\"fire\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reactions.fire").value(1))
                .andExpect(jsonPath("$.reactions.ahh").value(0));
        mvc.perform(put("/users/{userId}/comments/{commentId}/reaction", ownerId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"reaction\":\"fire\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reactions.fire").value(2));
        mvc.perform(patch("/users/{userId}/comments/{commentId}", guestId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"Updated comment\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Updated comment"));
        mvc.perform(patch("/users/{userId}/comments/{commentId}", ownerId, commentId)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"Not mine\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/users/{userId}/events/{eventId}/comments", guestId, eventId)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users/{userId}/events/{eventId}/comments", guestId, 999999999L)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"Missing event\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/users/{userId}/comments/{commentId}", guestId, 999999999L)
                        .contentType(APPLICATION_JSON).content("{\"text\":\"Missing comment\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/events/{eventId}/comments", eventId)).andExpect(jsonPath("$[0].reactions.fire").value(2))
                .andExpect(jsonPath("$[0].reactions.ahh").value(0));
    }

    private long create(String path, Object body) throws Exception {
        String response = mvc.perform(post("/" + path).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
