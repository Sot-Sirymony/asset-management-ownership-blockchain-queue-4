package com.up.asset_holder_api.controller;

import com.google.protobuf.Api;
import com.up.asset_holder_api.helper.IdentityHelper;
import com.up.asset_holder_api.model.entity.User;
import com.up.asset_holder_api.model.request.UserPassword;
import com.up.asset_holder_api.model.request.UserRegister;
import com.up.asset_holder_api.model.request.UserRequest;
import com.up.asset_holder_api.model.response.ApiResponse;
import com.up.asset_holder_api.exception.NotFoundException;
import com.up.asset_holder_api.model.response.UserRequestResponse;
import com.up.asset_holder_api.model.response.UserResponse;
import com.up.asset_holder_api.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin
@AllArgsConstructor
public class EnrollmentController {


    private final EnrollmentService enrollmentService;

    @PostMapping("/admin/register_user")
    @Operation(summary = "enroll user")
    public ResponseEntity<ApiResponse<UserRegister>> registerUser(@Valid @RequestBody UserRegister user) {
        ApiResponse<UserRegister> res = ApiResponse.<UserRegister>builder()
                .message("Create user successfully")
                .payload(enrollmentService.registerUser(user))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/admin/getAllUser")
    @Operation(summary = "admin get all user")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUser(
            @RequestParam(defaultValue = "1") @Positive Integer size ,
            @RequestParam(defaultValue = "10") @Positive Integer page
    ) {
        ApiResponse<List<UserResponse>> res = ApiResponse.<List<UserResponse>>builder()
                .message("Get all user successfully")
                .payload(enrollmentService.getAllUser(size,page))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/admin/getUser/{id}")
    @Operation(summary = "Admin and user view user profile to update")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") Integer id) {
        UserResponse user = enrollmentService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User not found: " + id);
        }
        ApiResponse<UserResponse> res = ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully")
                .payload(user)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/admin/updateUser/{id}")
    @Operation(summary = "admin update user")
    public ResponseEntity<ApiResponse<UserRequest>> updateUser(@PathVariable Integer id , @Valid @RequestBody UserRequest userRequest) {
        ApiResponse<UserRequest> res = ApiResponse.<UserRequest>builder()
                .message("Update user successfully")
                .payload(enrollmentService.updateUser(id,userRequest))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @GetMapping("/getProfile")
    @Operation(summary = "Admin view their profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal User user) {
        ApiResponse<UserResponse> res = ApiResponse.<UserResponse>builder()
                .message("Update user successfully")
                .payload(enrollmentService.getProfile())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/updateProfile")
    @Operation(summary = "Admin and user update their profile")
    public ResponseEntity<ApiResponse<Boolean>> updateProfile(@RequestBody UserRequest userRequest) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Update profile successfully")
                .payload(enrollmentService.updateProfile(userRequest))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/changePassword")
    @Operation(summary = "Admin and use can change their password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@Valid @RequestBody UserPassword userPassword) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Update profile successfully")
                .payload(enrollmentService.changePassword(userPassword))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @DeleteMapping("/admin/deleteUser/{id}")
    @Operation(summary = "Admin delete user")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@PathVariable Integer id) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Delete user successfully")
                .payload(enrollmentService.deleteUser(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }
}
