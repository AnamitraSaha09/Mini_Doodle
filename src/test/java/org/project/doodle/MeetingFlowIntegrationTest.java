package org.project.doodle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test: full HTTP -> service -> JPA -> H2 stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MeetingFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void testCreateUserSlotBookAndQueryAvailability() throws Exception {
        long userId = createUser("Adam Smith", "adam@example.com");
        long slotId = createSlot(userId, "2026-07-01T09:00:00Z", "2026-07-01T09:30:00Z");

        // Book the slot into a meeting -> slot = BUSY.
        mvc.perform(post("/api/meetings/users/{u}/slots/{s}", userId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"1:1 sync","description":"weekly","participants":["grace@example.com"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("1:1 sync"));

        // Availability for the window shows the slot as busy, not free.
        mvc.perform(get("/api/users/{u}/slots/availability", userId)
                        .param("from", "2026-07-01T08:00:00Z")
                        .param("to", "2026-07-01T18:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busy.length()").value(1))
                .andExpect(jsonPath("$.free.length()").value(0))
                .andExpect(jsonPath("$.busy[0].id").value((int) slotId));
    }

    @Test
    void testBookingAnAlreadyBookedSlotReturns409() throws Exception {
        long userId = createUser("Grace Hopper", "grace@example.com");
        long slotId = createSlot(userId, "2026-07-02T10:00:00Z", "2026-07-02T10:30:00Z");

        String body = """
                {"title":"first","participants":["adam@example.com"]}
                """;
        mvc.perform(post("/api/meetings/users/{u}/slots/{s}", userId, slotId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/meetings/users/{u}/slots/{s}", userId, slotId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    private long createUser(String name, String email) throws Exception {
        MvcResult res = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(res);
    }

    private long createSlot(long userId, String start, String end) throws Exception {
        MvcResult res = mvc.perform(post("/api/users/{u}/slots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startTime\":\"" + start + "\",\"endTime\":\"" + end + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FREE"))
                .andReturn();
        return idOf(res);
    }

    private long idOf(MvcResult res) throws Exception {
        JsonNode node = mapper.readTree(res.getResponse().getContentAsString());
        long id = node.get("id").asLong();
        assertThat(id).isPositive();
        return id;
    }
}
