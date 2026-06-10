package com.bachelor.service_desk;

import com.bachelor.service_desk.entity.RequestEntity;
import com.bachelor.service_desk.entity.RequestStatus;
import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.RequestRepository;
import com.bachelor.service_desk.repository.ReviewRepository;
import com.bachelor.service_desk.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndpointIntegrationTests {

    private static final String PASSWORD = "12345678";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity superAdmin;
    private UserEntity admin;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        superAdmin = saveUser("Malik", "Adminov", Role.SUPER_ADMIN, "+79000000001");
        admin = saveUser("Anna", "Operator", Role.ADMIN, "+79000000002");
        user = saveUser("Ivan", "Client", Role.USER, "+79000000003");
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numberPhone": "+79000000001",
                                  "password": "12345678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(superAdmin.getId()))
                .andExpect(jsonPath("$.numberPhone").value("+79000000001"));
    }

    @Test
    void loginPreflightAllowsVercelFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://service-desk-frontend-iota.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://service-desk-frontend-iota.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void usersEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void superAdminCanCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerFor(superAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Petr",
                                  "surname": "Petrov",
                                  "role": "USER",
                                  "password": "password1",
                                  "numberPhone": "+79000000004"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.numberPhone").value("+79000000004"));

        assertThat(userRepository.findByNumberPhone("+79000000004")).isPresent();
    }

    @Test
    void adminCannotCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Petr",
                                  "surname": "Petrov",
                                  "role": "USER",
                                  "password": "password1",
                                  "numberPhone": "+79000000005"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateRequest() throws Exception {
        mockMvc.perform(post("/api/requests")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Printer is offline",
                                  "description": "Office printer does not respond",
                                  "createdById": %d
                                }
                                """.formatted(user.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Printer is offline"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        assertThat(requestRepository.findAllByCreatedBy(user)).hasSize(1);
    }

    @Test
    void adminCanChangeRequestStatus() throws Exception {
        RequestEntity request = saveRequest(user, "VPN access", "Cannot connect to VPN");

        mockMvc.perform(patch("/api/requests/{requestId}/status", request.getId())
                        .header("Authorization", bearerFor(admin))
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        assertThat(requestRepository.findById(request.getId()))
                .get()
                .extracting(RequestEntity::getStatus)
                .isEqualTo(RequestStatus.IN_PROGRESS);
    }

    @Test
    void adminCanAssignAdminAsResponsibleForRequest() throws Exception {
        RequestEntity request = saveRequest(user, "Network issue", "Wi-Fi is unstable");

        mockMvc.perform(patch("/api/requests/{requestId}/responsible/{userId}", request.getId(), admin.getId())
                        .header("Authorization", bearerFor(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.responsible.id").value(admin.getId()))
                .andExpect(jsonPath("$.responsible.role").value("ADMIN"));

        assertThat(requestRepository.findById(request.getId()))
                .get()
                .extracting(savedRequest -> savedRequest.getResponsible().getId())
                .isEqualTo(admin.getId());
    }

    @Test
    void assigningNonAdminAsResponsibleReturnsConflict() throws Exception {
        RequestEntity request = saveRequest(user, "Laptop issue", "Battery drains quickly");

        mockMvc.perform(patch("/api/requests/{requestId}/responsible/{userId}", request.getId(), user.getId())
                        .header("Authorization", bearerFor(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Responsible user must have ADMIN role"));
    }

    @Test
    void superAdminCannotAssignResponsibleForRequest() throws Exception {
        RequestEntity request = saveRequest(user, "Account issue", "Cannot reset password");

        mockMvc.perform(patch("/api/requests/{requestId}/responsible/{userId}", request.getId(), admin.getId())
                        .header("Authorization", bearerFor(superAdmin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRequestStatusReturnsBadRequest() throws Exception {
        RequestEntity request = saveRequest(user, "Monitor problem", "Monitor flickers");

        mockMvc.perform(patch("/api/requests/{requestId}/status", request.getId())
                        .header("Authorization", bearerFor(admin))
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request status"));
    }

    @Test
    void cannotChangeCompletedRequestStatus() throws Exception {
        RequestEntity request = saveRequest(user, "Finished task", "Everything is done");
        request.changeStatus(RequestStatus.COMPLETED);
        requestRepository.save(request);

        mockMvc.perform(patch("/api/requests/{requestId}/status", request.getId())
                        .header("Authorization", bearerFor(admin))
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot change status of completed request"));

        assertThat(requestRepository.findById(request.getId()))
                .get()
                .extracting(RequestEntity::getStatus)
                .isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void userCanCreateReviewForRequest() throws Exception {
        RequestEntity request = saveRequest(user, "Keyboard replacement", "Broken key");

        mockMvc.perform(post("/api/reviews/{reviewOwner}", "GOOD")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Great work",
                                  "description": "The issue was resolved quickly",
                                  "owner": %d,
                                  "request": %d
                                }
                                """.formatted(user.getId(), request.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Great work"))
                .andExpect(jsonPath("$.ownerId").value(user.getId()))
                .andExpect(jsonPath("$.requestId").value(request.getId()));

        assertThat(reviewRepository.existsByRequestId(request.getId())).isTrue();
    }

    @Test
    void duplicateReviewForRequestReturnsBadRequest() throws Exception {
        RequestEntity request = saveRequest(user, "Mouse replacement", "Left button is broken");
        createReview(request);

        mockMvc.perform(post("/api/reviews/{reviewOwner}", "BAD")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Second review",
                                  "description": "Trying to review the same request again",
                                  "owner": %d,
                                  "request": %d
                                }
                                """.formatted(user.getId(), request.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Review for this request already exists"));
    }

    @Test
    void adminCanGetReviewsWithOwnerAndRequestIds() throws Exception {
        RequestEntity request = saveRequest(user, "Headset replacement", "Microphone does not work");
        createReview(request);

        mockMvc.perform(get("/api/reviews")
                        .header("Authorization", bearerFor(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("Initial review"))
                .andExpect(jsonPath("$[0].ownerId").value(user.getId()))
                .andExpect(jsonPath("$[0].requestId").value(request.getId()));
    }

    @Test
    void userCanGetReviewById() throws Exception {
        RequestEntity request = saveRequest(user, "Camera issue", "Laptop camera is blurry");
        createReview(request);
        Long reviewId = reviewRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", bearerFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.title").value("Initial review"));
    }

    @Test
    void superAdminCanSearchUsers() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", bearerFor(superAdmin))
                        .param("search", "Ivan Cli"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user.getId()))
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[0].surname").value("Client"));
    }

    private String bearerFor(UserEntity user) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numberPhone": "%s",
                                  "password": "%s"
                                }
                                """.formatted(user.getNumberPhone(), PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return "Bearer " + json.get("token").asText();
    }

    private UserEntity saveUser(String name, String surname, Role role, String numberPhone) {
        UserEntity user = new UserEntity();
        user.changeName(name);
        user.changeSurname(surname);
        user.changeRole(role);
        user.changeNumberPhone(numberPhone);
        user.changeEnabled(true);
        user.changePassword(passwordEncoder.encode(PASSWORD));
        return userRepository.save(user);
    }

    private RequestEntity saveRequest(UserEntity creator, String title, String description) {
        RequestEntity request = new RequestEntity(title, description);
        request.markAsActive();
        request.assignCreator(creator);
        return requestRepository.save(request);
    }

    private void createReview(RequestEntity request) throws Exception {
        mockMvc.perform(post("/api/reviews/{reviewOwner}", "GOOD")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Initial review",
                                  "description": "The first review is long enough",
                                  "owner": %d,
                                  "request": %d
                                }
                                """.formatted(user.getId(), request.getId())))
                .andExpect(status().isCreated());
    }
}
