package com.cotf.server.controller

import com.cotf.server.dto.LoginRequest
import com.cotf.server.dto.RegisterRequest
import com.cotf.server.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var userRepository: UserRepository

    @BeforeEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    fun `register creates user and returns tokens`() {
        val body = objectMapper.writeValueAsString(RegisterRequest("hero", "forest123"))
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.username").value("hero"))

        assertNotNull(userRepository.findByUsername("hero"))
    }

    @Test
    fun `register rejects duplicate username`() {
        val body = objectMapper.writeValueAsString(RegisterRequest("twin", "forest123"))
        mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `signin returns tokens for correct credentials`() {
        val registerBody = objectMapper.writeValueAsString(RegisterRequest("login_user", "forest123"))
        mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody)
        ).andExpect(status().isCreated)

        val loginBody = objectMapper.writeValueAsString(LoginRequest("login_user", "forest123"))
        mockMvc.perform(
            post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(loginBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
    }

    @Test
    fun `signin rejects wrong password`() {
        val registerBody = objectMapper.writeValueAsString(RegisterRequest("real_user", "forest123"))
        mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody)
        ).andExpect(status().isCreated)

        val loginBody = objectMapper.writeValueAsString(LoginRequest("real_user", "wrong_password"))
        mockMvc.perform(
            post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content(loginBody)
        ).andExpect(status().isBadRequest)
    }
}
