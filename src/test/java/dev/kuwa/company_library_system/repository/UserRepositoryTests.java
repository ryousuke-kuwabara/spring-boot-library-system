package dev.kuwa.company_library_system.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import dev.kuwa.company_library_system.entity.Role;
import dev.kuwa.company_library_system.entity.User;

@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleUser = new Role();
        roleUser.setName("ROLE_USER");
        entityManager.persist(roleUser);

        roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        entityManager.persist(roleAdmin);

        entityManager.flush();
    }

    @Test
    void save_単一ロールを持つユーザーを保存する() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setPasswordHash("password");
        newUser.setRoles(Set.of(roleUser));

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testUser");
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("ROLE_USER");
    }

    @Test
    void save_複数ロールを持つユーザーを保存する() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("adminUser");
        newUser.setPasswordHash("password");
        newUser.setRoles(Set.of(roleUser, roleAdmin));

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("adminUser");
        assertThat(savedUser.getRoles()).hasSize(2);
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void delete_ユーザーを削除してもロールは残る() {
        // Arrange
        User user = new User();
        user.setUsername("testUser");
        user.setPasswordHash("password");
        user.setRoles(Set.of(roleAdmin));
        User savedUser = entityManager.persistAndFlush(user);
        Long userId = savedUser.getId();
        Long roleId = roleAdmin.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        User deletedUser = entityManager.find(User.class, userId);
        assertThat(deletedUser).isNull();

        Role nonDeletedRole = entityManager.find(Role.class, roleId);
        assertThat(nonDeletedRole).isNotNull();
    }
}
