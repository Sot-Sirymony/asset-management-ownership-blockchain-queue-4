package com.up.asset_holder_api.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.up.asset_holder_api.configuration.BeanConfig;
import com.up.asset_holder_api.configuration.SecurityConfig;
import com.up.asset_holder_api.exception.GlobalExceptionHandle;
import com.up.asset_holder_api.exception.NotFoundException;
import com.up.asset_holder_api.jwt.JwtAuthEntrypoint;
import com.up.asset_holder_api.jwt.JwtAuthFilter;
import com.up.asset_holder_api.jwt.JwtUtil;
import com.up.asset_holder_api.service.AppUserService;
import com.up.asset_holder_api.service.VerificationService;
import com.up.asset_holder_api.testsupport.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VerificationController.class)
@Import({SecurityConfig.class, BeanConfig.class, JwtAuthFilter.class, JwtAuthEntrypoint.class, GlobalExceptionHandle.class})
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationService verificationService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private AppUserService appUserService;

    private static ObjectNode samplePayload() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("assetId", "asset-1");
        node.put("verified", true);
        return node;
    }

    @BeforeEach
    void setupJwt() {
        SecurityTestSupport.mockJwtAsUser(jwtUtil, appUserService);
    }

    @Test
    void verifyAssetInternal_asUser_returnsOk() throws Exception {
        when(verificationService.verifyAssetInternal(eq("asset-1"))).thenReturn(samplePayload());

        mockMvc.perform(get("/api/v1/user/verifyAsset/{id}", "asset-1")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset verified successfully"))
                .andExpect(jsonPath("$.payload.assetId").value("asset-1"))
                .andExpect(jsonPath("$.payload.verified").value(true));
    }

    @Test
    void verifyAssetExternal_asUser_returnsOk() throws Exception {
        when(verificationService.verifyAssetExternal(eq("asset-2"))).thenReturn(samplePayload());

        mockMvc.perform(get("/api/v1/user/verifyAssetExternal/{id}", "asset-2")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("External verification completed"))
                .andExpect(jsonPath("$.payload.verified").value(true));
    }

    @Test
    void getVerificationTrail_asUser_returnsOk() throws Exception {
        when(verificationService.getVerificationTrail(eq("asset-3"))).thenReturn(samplePayload());

        mockMvc.perform(get("/api/v1/user/verificationTrail/{id}", "asset-3")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification trail retrieved successfully"))
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    void verifyAssetInternal_whenAssetNotFound_returns404() throws Exception {
        doThrow(new NotFoundException("Asset not found: bad-id")).when(verificationService).verifyAssetInternal(eq("bad-id"));

        mockMvc.perform(get("/api/v1/user/verifyAsset/{id}", "bad-id")
                        .header("Authorization", "Bearer " + SecurityTestSupport.GOOD_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Asset not found: bad-id"))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }
}
