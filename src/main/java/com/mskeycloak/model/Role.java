package com.mskeycloak.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "roles")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[ A-zÀ-ÿ'-]+$")
    @Column(name = "name",  nullable = false, length = 50)
    private String name;


    @Size(min = 0, max = 255)
    @Pattern(regexp = "^[ A-zÀ-ÿ'-]*$")
    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "status")
    private boolean status = false;



}
