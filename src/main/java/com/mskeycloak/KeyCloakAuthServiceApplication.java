package com.mskeycloak;

import com.mskeycloak.model.Role;
import com.mskeycloak.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Optional;
@EnableDiscoveryClient
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableFeignClients
public class KeyCloakAuthServiceApplication {
    private  final IRoleRepository iRoleRepository;

    public static void main(String[] args) {
        SpringApplication.run(KeyCloakAuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start() {
        return args -> {
            Role admin = new Role();
            admin.setName("Client");
            admin.setDescription("Client");
            saveRole(admin);
        };
    }
    private void saveRole(Role role) {
        Optional<Role> roleSearched = iRoleRepository.findByNameIgnoreCase(role.getName());
        if (roleSearched.isEmpty()) {
            role = iRoleRepository.save(role);
            log.info("The role with name '{}' SAVED.", role.getName());
        } else {
            log.info("The role with name '{}' FOUND.", role.getName());
        }
    }
}
