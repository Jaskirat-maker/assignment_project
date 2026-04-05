package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.request.UserUpdateRequest;
import com.finance.dto.response.UserResponse;
import com.finance.entity.enums.Role;
import com.finance.security.JwtTokenProvider;
import com.finance.service.RefreshTokenService;
import com.finance.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.finance.config.RateLimitProperties rateLimitProperties;

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllUsers_shouldReturn200_forAdmin() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .role("ANALYST")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    @WithMockUser(username = "analyst", authorities = {"ANALYST"})
    void getAllUsers_shouldReturn403_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void updateUser_shouldAllowRoleAndStatusUpdate() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("jane")
                .email("jane@example.com")
                .password("newpassword123")
                .role(Role.VIEWER)
                .isActive(false)
                .build();
        UserResponse updated = UserResponse.builder()
                .id(2L)
                .username("jane")
                .email("jane@example.com")
                .role("VIEWER")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userService.updateUser(eq(2L), any(UserUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void patchStatus_shouldReturn200_forAdmin() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(3L)
                .username("alex")
                .email("alex@example.com")
                .role("ANALYST")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userService.updateUserStatus(3L, false)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/3/status")
                .param("active", "false")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
