package com.up.asset_holder_api.service.serviceImp;

import com.up.asset_holder_api.exception.NotFoundException;
import com.up.asset_holder_api.helper.FabricCaPemResolver;
import com.up.asset_holder_api.model.request.UserPassword;
import com.up.asset_holder_api.model.request.UserRegister;
import com.up.asset_holder_api.model.request.UserRequest;
import com.up.asset_holder_api.model.response.UserResponse;
import com.up.asset_holder_api.repository.EnrollmentRepository;
import com.up.asset_holder_api.service.EnrollmentService;
import com.up.asset_holder_api.utils.GetCurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pemFile}")
    private String pemFile;

    @Value("${fabric.ca.org1.url:https://localhost:7054}")
    private String fabricCaOrg1Url;

    @Value("${fabric.ca.admin.id:admin}")
    private String fabricCaAdminId;

    @Value("${fabric.ca.admin.password:adminpw}")
    private String fabricCaAdminPassword;

    public EnrollmentServiceImp(EnrollmentRepository enrollmentRepository, PasswordEncoder passwordEncoder) {
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserRegister registerUser(UserRegister user) {

        String resolvedPemFile = FabricCaPemResolver.resolvePemFilePath(pemFile);
        Properties props = new Properties();
        props.put("pemFile", resolvedPemFile);
        props.put("allowAllHostNames", "true");

        try {
            // Initialize HFCAClient
            HFCAClient caClient = HFCAClient.createNewInstance(fabricCaOrg1Url, props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Initialize wallet
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check if user identity already exists in the wallet
            if (wallet.get(user.getUsername()) != null) {
                String message = "An identity for the user \"" + user.getUsername() + "\" already exists in the wallet.";
                log.warn(message);
            }

            // Use CA registrar credentials for user registration.
            Enrollment adminEnrollment = caClient.enroll(fabricCaAdminId, fabricCaAdminPassword);
            org.hyperledger.fabric.sdk.User admin = new org.hyperledger.fabric.sdk.User() {
                @Override
                public String getName() {
                    return fabricCaAdminId;
                }

                @Override
                public java.util.Set<String> getRoles() {
                    return null;
                }

                @Override
                public String getAccount() {
                    return null;
                }

                @Override
                public String getAffiliation() {
                    return "org1.department1";
                }

                @Override
                public Enrollment getEnrollment() {
                    return adminEnrollment;
                }

                @Override
                public String getMspId() {
                    return "Org1MSP";
                }
            };

            // Register the user, enroll the user, and import the new identity into the wallet.
            RegistrationRequest registrationRequest = new RegistrationRequest(user.getUsername());
            registrationRequest.setAffiliation("org1.department1");
            registrationRequest.setEnrollmentID(user.getUsername());

            String enrollmentSecret = caClient.register(registrationRequest, admin);
            Enrollment enrollment = caClient.enroll(user.getUsername(), enrollmentSecret);
            String certificate = enrollment.getCert();
            Identity userEnroll = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put(user.getUsername(), userEnroll);

            //hash password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            enrollmentRepository.createUser(user,certificate);
            user.setPassword(null);
            return user;

        } catch (Exception e) {
            String errorMessage = "Error during enrollment: " + e.getMessage();
            log.error("Enrollment failed for user: {}", user.getUsername(), e);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public List<UserResponse> getAllUser(Integer size, Integer page) {
        return enrollmentRepository.findAllUser(size,page);
    }

    @Override
    public UserRequest updateUser(Integer id,UserRequest userRequest) {
        return enrollmentRepository.updateUser(id,userRequest);
    }

    @Override
    public UserResponse getUserById(Integer id) {
        return enrollmentRepository.findUserById(id);
    }

    @Override
    public UserResponse getProfile() {
        Integer userId = GetCurrentUser.currentId();
        return enrollmentRepository.findUserById(userId);
    }

    @Override
    public boolean updateProfile(UserRequest userRequest) {
        Integer userId = GetCurrentUser.currentId();
        log.debug("Updating profile for user: {}, profile image: {}", userId, userRequest.getProfile_img());
        return enrollmentRepository.updateProfile(userRequest, userId);
    }

    @Override
    public Boolean changePassword(UserPassword userPassword) {
        Integer userId = GetCurrentUser.currentId();
        String oldPassword = enrollmentRepository.findOldPassword(userId);

        if (!passwordEncoder.matches(userPassword.getOldPassword(), oldPassword)) {
            throw new NotFoundException("Old password does not match");
        }
        else if(!userPassword.getNewPassword().equals(userPassword.getConfirmPassword())) {
            throw new NotFoundException("New password does not match");
        }

        userPassword.setNewPassword(passwordEncoder.encode(userPassword.getNewPassword()));

        return enrollmentRepository.updateAdminPassword(userPassword,userId);
    }

    @Override
    public Boolean deleteUser(Integer id) {
        Integer adminId = GetCurrentUser.currentId();
        return enrollmentRepository.deleteUserById(id, adminId);
    }


}
