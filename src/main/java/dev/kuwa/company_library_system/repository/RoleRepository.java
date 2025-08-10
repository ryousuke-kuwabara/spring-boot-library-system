package dev.kuwa.company_library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kuwa.company_library_system.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
