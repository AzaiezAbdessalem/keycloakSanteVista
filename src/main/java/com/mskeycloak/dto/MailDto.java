package com.mskeycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailDto {

    private String typeMail;
    private String fullname;
    private String mailTo;
    private String cc;
    private String body;
    private String subject;
    private String username;
    private String password;
    private List<MultipartFile> files;


    public MailDto(String typeMail, String fullname, String mailTo, String subject, String username, String password, String body) {
        this.fullname = fullname;
        this.mailTo = mailTo;
        this.subject = subject;
        this.username = username;
        this.password = password;
        this.body = body;
        this.typeMail=typeMail;
    }
}
