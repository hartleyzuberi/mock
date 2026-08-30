package com.aceli.mock.web;

import com.aceli.mock.repository.FundingRequestRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FundingRequestControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired FundingRequestRepository repository;

    @BeforeEach
    void clearRequests() {
        repository.deleteAll();
    }

    @Test
    void createsEvaluatesAndRetrievesApprovedRequest() throws Exception {
        String body = mockMvc.perform(post("/api/v1/funding-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizationName": "Green Growers Ltd",
                                  "country": "Kenya",
                                  "requestedAmount": 50000.00,
                                  "organizationAgeYears": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.country").value("Kenya"))
                .andReturn().getResponse().getContentAsString();

        Number idNumber = JsonPath.read(body, "$.id");
        long id = idNumber.longValue();

        mockMvc.perform(post("/api/v1/funding-requests/{id}/evaluate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reasons").isEmpty());

        mockMvc.perform(get("/api/v1/funding-requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/funding-requests").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/funding-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizationName": "",
                                  "country": "Kenya",
                                  "requestedAmount": -1,
                                  "organizationAgeYears": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.organizationName").exists())
                .andExpect(jsonPath("$.details.requestedAmount").exists())
                .andExpect(jsonPath("$.details.organizationAgeYears").exists());
    }

    @Test
    void rejectsUnsupportedCountry() throws Exception {
        mockMvc.perform(post("/api/v1/funding-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizationName": "Example Org",
                                  "country": "Somalia",
                                  "requestedAmount": 1000,
                                  "organizationAgeYears": 5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void returns404ForMissingRequest() throws Exception {
        mockMvc.perform(get("/api/v1/funding-requests/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FUNDING_REQUEST_NOT_FOUND"));
    }
}
