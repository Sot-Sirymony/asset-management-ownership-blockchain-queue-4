package com.up.asset_holder_api.service.serviceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.up.asset_holder_api.exception.NotFoundException;
import com.up.asset_holder_api.helper.GatewayHelperV1;
import com.up.asset_holder_api.model.response.UserRequestResponse;
import com.up.asset_holder_api.repository.UserRepository;
import com.up.asset_holder_api.service.AssetService;
import com.up.asset_holder_api.service.VerificationService;
import com.up.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of verification service for asset verification workflows.
 * Provides internal and external verification as per BRD requirements.
 */
@Slf4j
@Service
@AllArgsConstructor
public class VerificationServiceImp implements VerificationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AssetService assetService;
    private final UserRepository userRepository;

    /**
     * Internal verification - used by internal auditors and department managers.
     * Returns asset details with full ownership history.
     */
    @Override
    public JsonNode verifyAssetInternal(String assetId) {
        log.info("Internal verification requested for asset: {}", assetId);
        UserRequestResponse user = userRepository.findUserById(GetCurrentUser.currentId());
        if (user == null) {
            log.warn("Current user not found for verification, userId from context");
            throw new NotFoundException("Current user not found");
        }

        try {
            JsonNode asset = assetService.getAssetById(assetId);
            JsonNode history = assetService.getHistoryById(assetId);

            ObjectNode verificationResult = MAPPER.createObjectNode();
            verificationResult.set("asset", asset);
            verificationResult.set("history", history);
            verificationResult.put("verificationType", "INTERNAL");
            verificationResult.put("verifiedBy", user.getUsername());
            verificationResult.put("verifiedAt", System.currentTimeMillis());
            verificationResult.put("status", "VERIFIED");

            log.info("Internal verification completed for asset: {}", assetId);
            return verificationResult;

        } catch (NotFoundException e) {
            log.error("Asset not found for internal verification: {}", assetId);
            throw e;
        } catch (Exception e) {
            log.error("Failed internal verification for asset: {} - {}", assetId, e.getMessage(), e);
            throw new NotFoundException("Verification failed: " + e.getMessage());
        }
    }

    /**
     * External verification - used by external auditors and verifiers.
     * Returns asset details with transaction history for audit purposes.
     */
    @Override
    public JsonNode verifyAssetExternal(String assetId) {
        log.info("External verification requested for asset: {}", assetId);
        UserRequestResponse user = userRepository.findUserById(GetCurrentUser.currentId());
        if (user == null) {
            log.warn("Current user not found for verification, userId from context");
            throw new NotFoundException("Current user not found");
        }

        try {
            JsonNode asset = assetService.getAssetById(assetId);
            JsonNode history = assetService.getHistoryById(assetId);

            ObjectNode verificationResult = MAPPER.createObjectNode();
            verificationResult.set("asset", asset);
            verificationResult.set("transactionHistory", history);
            verificationResult.put("verificationType", "EXTERNAL");
            verificationResult.put("verifiedBy", user.getUsername());
            verificationResult.put("verifiedAt", System.currentTimeMillis());
            verificationResult.put("status", "VERIFIED");
            verificationResult.put("auditTrail", "Complete immutable transaction history from blockchain");

            log.info("External verification completed for asset: {}", assetId);
            return verificationResult;

        } catch (NotFoundException e) {
            log.error("Asset not found for external verification: {}", assetId);
            throw e;
        } catch (Exception e) {
            log.error("Failed external verification for asset: {} - {}", assetId, e.getMessage(), e);
            throw new NotFoundException("Verification failed: " + e.getMessage());
        }
    }

    /**
     * Gets complete verification trail including all transactions.
     */
    @Override
    public JsonNode getVerificationTrail(String assetId) {
        log.debug("Getting verification trail for asset: {}", assetId);

        try {
            JsonNode asset = assetService.getAssetById(assetId);
            JsonNode history = assetService.getHistoryById(assetId);

            ObjectNode trail = MAPPER.createObjectNode();
            trail.set("assetDetails", asset);
            trail.set("completeHistory", history);
            trail.put("trailGeneratedAt", System.currentTimeMillis());
            trail.put("source", "Hyperledger Fabric Blockchain");

            return trail;

        } catch (Exception e) {
            log.error("Failed to get verification trail for asset: {} - {}", assetId, e.getMessage(), e);
            throw new NotFoundException("Failed to retrieve verification trail: " + e.getMessage());
        }
    }
}
