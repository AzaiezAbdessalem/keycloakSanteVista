package com.mskeycloak.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    private String userId;
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
    private String username;
    //TODO: correct the email field in front end before correcting this
    private String Email;

}
