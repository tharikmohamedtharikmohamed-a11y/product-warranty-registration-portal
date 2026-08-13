package com.warrantyportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.enums.UserRole;
import com.warrantyportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // TEST 1: Register a customer
    @Test
    void test1_RegisterCustomerSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Password123", "9876543210");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // TEST 2: Register the same email again -> 409 Conflict
    @Test
    void test2_RegisterDuplicateEmail_Returns409Conflict() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "duplicate@example.com", "Password123", "9876543210");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    // TEST 3: Register with invalid email -> 400 Bad Request
    @Test
    void test3_RegisterInvalidEmail_Returns400BadRequest() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "invalid-email-format", "Password123", "9876543210");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // TEST 4: Login with correct password -> Returns JWT
    @Test
    void test4_LoginSuccess_ReturnsJwt() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Password123", "9876543210");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("john@example.com", "Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    // TEST 5: Login with incorrect password -> 401 Unauthorized
    @Test
    void test5_LoginIncorrectPassword_Returns401Unauthorized() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Password123", "9876543210");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("john@example.com", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    // TEST 6: Access GET /api/health without JWT -> Success
    @Test
    void test6_AccessPublicHealthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    // TEST 7: Access a protected endpoint without JWT -> 401 Unauthorized
    @Test
    void test7_AccessProtectedEndpointWithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    // TEST 8: Access protected endpoint using valid JWT -> Success
    @Test
    void test8_AccessProtectedEndpointWithValidJwt_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "Password123", "9876543210");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("john@example.com", "Password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseString).get("token").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    // TEST 9: Use expired/invalid JWT -> 401 Unauthorized
    @Test
    void test9_AccessProtectedEndpointWithInvalidJwt_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    // TEST 10: Attempt to register with "role": "ADMIN" -> User must still become CUSTOMER
    @Test
    void test10_RegisterWithAdminRoleInPayload_IgnoresRoleAndAssignsCustomer() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Hacker", "hacker@example.com", "Password123", "9876543210");
        registerRequest.setRole("ADMIN");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("hacker@example.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));

        Optional<User> savedUser = userRepository.findByEmail("hacker@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals(UserRole.CUSTOMER, savedUser.get().getRole());
    }

    // TEST 11: Verify password is stored as BCrypt hash, NOT plain text
    @Test
    void test11_PasswordStoredAsBCryptHash() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "PlainPassword123", "9876543210");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmail("john@example.com").orElseThrow();
        assertNotEquals("PlainPassword123", user.getPassword());
        assertTrue(passwordEncoder.matches("PlainPassword123", user.getPassword()));
        assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"));
    }
}
