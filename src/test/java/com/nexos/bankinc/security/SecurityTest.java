package com.nexos.bankinc.security;

import com.nexos.bankinc.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureWebMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminShouldAccessGenerateCard() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/card/123456/number"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    public void clienteShouldNotAccessGenerateCard() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/card/123456/number"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"CAJERO"})
    public void cajeroShouldAccessRechargeBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/card/balance")
                .contentType("application/json")
                .content("{\"cardId\":\"1234567890123456\",\"balance\":100.00}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    public void supervisorShouldAccessBlockCard() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/card/1234567890123456"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
