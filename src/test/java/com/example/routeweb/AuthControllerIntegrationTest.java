package com.example.routeweb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSuccessfulLogin() throws Exception {
            mockMvc.perform(post("/authorization")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "Nik")
                        .param("password", "factor2003"))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    public void testFailedLogin() throws Exception {
        mockMvc.perform(post("/authorization")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "wrongUser")
                        .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Login failed"));
    }

    // Пример с использованием аннотации @WithMockUser
    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    public void testAdminAccess() throws Exception {
        mockMvc.perform(post("/fileData"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin action performed"));
    }
}
