package com.mskeycloak.client;

import com.mskeycloak.dto.MailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "mail-service", url = "http://localhost:8086")

public interface IMailClient {

    @PostMapping("/api/mail/activatedAccount")
    ResponseEntity<Void> activatedAccount(@RequestBody MailDto mailDto);

    @PostMapping("/api/mail/resetPassword")
    ResponseEntity<Void> sendResetEmail(@RequestBody MailDto mailDto);

}
