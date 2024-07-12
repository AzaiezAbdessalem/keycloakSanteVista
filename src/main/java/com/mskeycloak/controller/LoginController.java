package com.mskeycloak.controller;

import com.mskeycloak.error.exception.BadRequestException;
import com.mskeycloak.utils.SecurityUtils;
import com.mskeycloak.dto.ChangePasswordRequest;
import com.mskeycloak.dto.LoginRequest;
import com.mskeycloak.dto.LoginResponse;
import com.mskeycloak.model.User;
import com.mskeycloak.service.ILoginService;
import com.mskeycloak.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keycloak/auth")
public class LoginController {

    private final ILoginService loginService;
    private final IUserService iUserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.debug("REST request to login {}", loginRequest.getUsername());
        final LoginResponse loginResponse = loginService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        final User loginResponse = iUserService.create(user);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable String userId) {
        log.debug("REST request to logout {}", userId);
        final String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            throw new BadRequestException("Vous ne pouvez pas déconnecter un autre utilisateur");
        }
        loginService.logout(userId);
        return ResponseEntity.ok("Déconnexion réussie, merci de votre confiance !");
    }



    @PostMapping("/findAccount/verificationCode")
    public ResponseEntity<String> checkVerificationCode(@RequestParam String email, @RequestParam String code) {
        System.out.println(email+code);
        log.info("REST request to check verification code {} of {}", code, email);
        loginService.checkVerificationCode(email, code);
        return ResponseEntity.ok("Code valide");
    }


    @PostMapping("/findAccount/restPassword")
    public ResponseEntity<Void> resetPasswordddd(@RequestParam String email, @RequestParam String password) {
        log.debug("REST request to reset password of {}", email);
        loginService.resetPassword(email, password);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/findAccount/{email}")
    public ResponseEntity<String> findAccount(@PathVariable String email ) {
        return ResponseEntity.status(200).body(loginService.findAccount(email));
    }
    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        log.debug("REST request to change password of {}", request.getUsername());
        final String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!currentUserEmail.equals(request.getUsername())) {
            throw new BadRequestException("Vous ne pouvez pas changer le mot de passe d'un autre utilisateur");
        }
        final String username = request.getUsername();
        final String currentPassword = request.getCurrentPassword();
        final String newPassword = request.getNewPassword();
        loginService.changePassword(username, currentPassword, newPassword);
        return ResponseEntity.ok().build();
    }

}
