package com.mskeycloak.controller;

import com.mskeycloak.model.Role;
import com.mskeycloak.service.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keycloak/roles")
public class RoleController {

    private final IRoleService roleService;


    @PostMapping("/")
    public ResponseEntity<Role> create(@RequestBody @Valid Role role) {
        log.debug("REST request to save Role : {}", role);
        Role roleCreated = roleService.create(role);
        return ResponseEntity.status(201).body(roleCreated);
    }


    @GetMapping("/")
    public List<Role> getAll() {
        log.debug("REST request to get all Roles");
        return roleService.getRoles();
    }

    @GetMapping("/statusFalse")
    public List<Role> getAllWithStatusFalse() {
        log.debug("REST request to get all Roles with status false");
        return roleService.getAllRolesWithStatusFalse();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        log.debug("REST request to get Role : {}", id);
        Role role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }


    @GetMapping("/byName/{name}")
    public ResponseEntity<Role> getByName(@PathVariable String name) {
        log.debug("REST request to get Role : {}", name);
        Role role = roleService.findByName(name);
        return ResponseEntity.ok(role);
    }



    @PostMapping("/{id}/update")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody @Valid Role role) {
        log.debug("REST request to update Role : {}", role);
        Role updatedRole = roleService.update(id, role);
        return ResponseEntity.ok(updatedRole);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
