package com.mskeycloak.service;

import com.mskeycloak.dto.LoginRequest;
import com.mskeycloak.dto.LoginResponse;

public interface ILoginService {

    void checkVerificationCode(String email, String code);


    void resetPassword(String email, String password);

    void changePassword(String username, String currentPassword, String newPassword);


    LoginResponse login(LoginRequest loginrequest);


    void logout(String userId);

    String findAccount(String email);
}
