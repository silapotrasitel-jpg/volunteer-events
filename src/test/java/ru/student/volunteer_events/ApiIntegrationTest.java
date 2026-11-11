package ru.student.volunteer_events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.student.volunteer_events.dto.LoginRequest;
import ru.student.volunteer_events.dto.SkillRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicEventsEndpoint_ShouldReturnOkWithoutToken() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void loginAndCreateSkill_AsCoordinator_ShouldSucceed() throws Exception {


        LoginRequest login = new LoginRequest();
        login.setUsername("coord1");
        login.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();

        SkillRequest skill = new SkillRequest();
        skill.setName("Фотосъёмка");

        mockMvc.perform(post("/api/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skill)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Фотосъёмка"));
    }

    @Test
    void createSkill_AsVolunteer_ShouldReturnForbidden() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("vol1");
        login.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        SkillRequest skill = new SkillRequest();
        skill.setName("Дизайн");

        mockMvc.perform(post("/api/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skill)))
                .andExpect(status().isForbidden());
    }
}
