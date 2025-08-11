package dev.kuwa.company_library_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kuwa.company_library_system.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
