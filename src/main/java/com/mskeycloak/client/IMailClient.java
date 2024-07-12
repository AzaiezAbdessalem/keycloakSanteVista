package com.mskeycloak.client;

import com.mskeycloak.dto.MailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "email-service")

public interface IMailClient {

    @PostMapping("/api/mail/creationAccount")
    ResponseEntity<Void> sendAddEmail(@RequestBody MailDto mailDto);

    @PostMapping("/api/mail/resetPassword")
    ResponseEntity<Void> sendResetEmail(@RequestBody MailDto mailDto);

}
