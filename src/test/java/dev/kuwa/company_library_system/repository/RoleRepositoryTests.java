package dev.kuwa.company_library_system.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import dev.kuwa.company_library_system.entity.Role;

@DataJpaTest
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_新しいロールを保存する() {

        // Arrange
        Role newRole = new Role();
        newRole.setName("ROLE_EDITOR");

        // Act
        Role savedRole = roleRepository.save(newRole);

        // Assert
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isPositive();
        assertThat(savedRole.getName()).isEqualTo("ROLE_EDITOR");
    }

    @Test
    void findById_既存のロールを取得する() {
        // Arrange
        Role role = new Role();
        role.setName("ROLE_VIEWER");
        Long roleId = entityManager.persistAndGetId(role, Long.class);

        // Act
        Optional<Role> foundRoleOptional = roleRepository.findById(roleId);

        // Assert
        assertThat(foundRoleOptional).isPresent();
        Role foundRole = foundRoleOptional.get();
        assertThat(foundRole.getId()).isEqualTo(roleId);
        assertThat(foundRole.getName()).isEqualTo("ROLE_VIEWER");
    }

    @Test
    void findById_存在しないIDで検索する() {
        // Arrange
        Long nonExistingId = 999L;

        // Act
        Optional<Role> foundRoleOptional = roleRepository.findById(nonExistingId);

        // Assert
        assertThat(foundRoleOptional).isEmpty();
    }

    @Test
    void save_既存のロールを更新する() {
        // Arrange
        Role originalRole = new Role();
        originalRole.setName("ROLE_TEST");
        entityManager.persist(originalRole);
        entityManager.flush();

        Role roleToUpdate = roleRepository.findByName("ROLE_TEST").orElseThrow();
        roleToUpdate.setName("ROLE_UPDATED");

        // Act
        Role updatedRole = roleRepository.save(roleToUpdate);

        // Assert
        assertThat(updatedRole.getId()).isEqualTo(roleToUpdate.getId());
        assertThat(updatedRole.getName()).isEqualTo("ROLE_UPDATED");
    }

    @Test
    void delete_既存のロールを削除する() {
        // Arrange
        Role roleToDelete = new Role();
        roleToDelete.setName("ROLE_DELETE");
        Long roleId = entityManager.persistAndGetId(roleToDelete, Long.class);

        // Act
        roleRepository.delete(roleToDelete);
        entityManager.flush();

        // Assert
        Optional<Role> foundOptional = roleRepository.findById(roleId);
        assertThat(foundOptional).isEmpty();
    }

    @Test
    void save_重複した名前で例外が発生する() {
        // Arrange
        Role existingRole = new Role();
        existingRole.setName("ROLE_EXISTING");
        entityManager.persistAndFlush(existingRole);

        Role duplicateRole = new Role();
        duplicateRole.setName("ROLE_EXISTING");

        // Act and Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            roleRepository.save(duplicateRole);
        });
    }
}
